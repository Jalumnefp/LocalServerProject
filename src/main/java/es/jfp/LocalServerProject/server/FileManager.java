package es.jfp.LocalServerProject.server;

import es.jfp.SerialFile;
import es.jfp.SerialMap;
import org.apache.commons.lang3.SerializationUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchService;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

public final class FileManager {
	
	private static FileManager instance;
	
	private File rootDirectory;
	public WatchService watchService;
	public SerialMap directoryMap;
	//private long fileId = 0;
	
	
	private FileManager(File rootDirectory) {
		this.rootDirectory = rootDirectory;
		this.directoryMap = new SerialMap(new SerialFile(
				rootDirectory.getName(),
				Path.of(rootDirectory.getName()).toString(),
				rootDirectory.isDirectory()
		));
		try {
			this.watchService = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateDirectoryMap();
	}
	
	
	public static FileManager getInstance(File rootDirectory) {
		synchronized (FileManager.class) {
			if (instance==null) {
				instance = new FileManager(rootDirectory);
			}
			return instance;
		}
	}

	public void getDirectoryMap(OutputStream socketOutputStream, String username) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(socketOutputStream);

			oos.writeObject(removeNotOwnerFolder(directoryMap, username));
			oos.flush();
			System.out.printf("[%s] Mapa de directorios enviado.\n", Thread.currentThread().getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void addDirectoryListener() {
		Path rootPath = Path.of(rootDirectory.getAbsolutePath());
		try {
			rootPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		directoryMap.forEachSerialFiles(serialFile -> {
			Path path = Path.of(rootDirectory.getAbsolutePath(), serialFile.getDirectory());
			if (path.toFile().isDirectory()) {
				try {
					path.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	public SerialMap removeNotOwnerFolder(SerialMap directoryMap, String username) {
		SerialFile rootClone = SerializationUtils.clone(directoryMap.getRootFile());
		SerialMap newSerialMap = new SerialMap(rootClone);
		List<SerialFile> filteredSerialFiles = directoryMap
				.filterSerialFiles(serialFile -> serialFile.getOwner() == null || serialFile.getOwner().equals(username));
		newSerialMap.getRootFile().setChildren(filteredSerialFiles);
		return newSerialMap;
	}

	public void updateDirectoryMap() {
		List<SerialFile> directoryStructure = mapDirectory(rootDirectory, null);
		directoryMap.getRootFile().setChildren(directoryStructure);
		addDirectoryListener();
	}

	public List<SerialFile> mapDirectory(File parent, SerialFile serialFileParent) {
		List<SerialFile> children = new LinkedList<>();
		try (Stream<Path> directoryWalker = Files.list(parent.toPath())) {
			directoryWalker.forEach(path -> {
				SerialFile newFile = new SerialFile(
						path.getFileName().toString(),
						this.rootDirectory.toPath().relativize(path).toString(),
						path.toFile().isDirectory()
				);
				if (newFile.getFileName().equals("OWNER.txt")) {
					Path filePath = Path.of(rootDirectory.getAbsolutePath(), newFile.getDirectory());
					String owner = new String(readFileBytes(filePath), StandardCharsets.UTF_8);
					serialFileParent.setOwner(owner);
				} else {
					if (newFile.isFolder()) {
						newFile.appendChildren(mapDirectory(path.toFile(), newFile));
					}
					children.add(newFile);
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return children;
	}


	/*public synchronized Map<String, List<String[]>> mapDirectory(File file, int level, String parent) throws IOException {
		if (level==0) {
			System.out.printf("[%s] Mapeando directorios...\n", Thread.currentThread().getName());
			List<String[]> rootList = new LinkedList<>();
		    rootList.add(new String[] {rootDirectory.getName(), "r", null});
		    file.toPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE);
		    directoryMap.put("ROOT", rootList);
		}
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(file.toPath())) {
        	for (Path p: ds) {
                if (p.getFileName().toString().equals("OWNER.txt") && p.toFile().isFile()) {
					String owner = new String(readFileBytes(p), StandardCharsets.UTF_8);
					for (Map.Entry<String, List<String[]>> e: directoryMap.entrySet()) {
						for (String[] s: e.getValue()) {
							if (s[0].equals(parent)) {
								s[2] = owner;
							}
						}
					}
				} else {
					String child = file.toPath().relativize(p).toString() + '?' + fileId++ + ':' + p;
					if (!directoryMap.containsKey(parent)) {
						directoryMap.put(parent, new LinkedList<>());
					}
					directoryMap.get(parent).add(new String[] { child, (p.toFile().isDirectory() ? ("d") : "f"), null});
					if(p.toFile().isDirectory()) {
						p.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
						directoryMap.putAll(mapDirectory(p.toFile(), level + 1, child));
					}
				}
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }
        return directoryMap;
	}*/
/*
	public synchronized Map<String, List<String[]>> applyChanges(File file, String event) {
		System.out.println("Aplicando cambios");
		for (Map.Entry<String, List<String[]>> entry: directoryMap.entrySet()) {
			if (!entry.getKey().equals("ROOT")) {
				String keyPath;
				if (entry.getKey().equals(rootDirectory.getName())) {
					keyPath = rootDirectory.getAbsolutePath();
				} else {
					keyPath = entry.getKey().substring(entry.getKey().indexOf(':') + 1);
				}
				if (event.equals(ENTRY_CREATE.name())) {
					System.out.println("Creando...");
					String newDirectoryName = file.getName() + '?' + fileId++ + ':' + file;
					System.out.println(file.getParent().equals(keyPath));
					if (file.getParent().equals(keyPath)) {
						System.out.println(true);
						directoryMap.get(entry.getKey()).add(new String[] { newDirectoryName, (file.isDirectory() ? ("d") : "f")});
						if (file.isDirectory()) {
							try {
								file.toPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE);
								directoryMap.put(newDirectoryName, new LinkedList<>());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						return directoryMap;
					}
				}
				if (event.equals(ENTRY_DELETE.name())) {
					System.out.println("Eliminando...");
					if (file.toPath().toString().equals(keyPath)) {
						directoryMap.remove(entry.getKey());
						return directoryMap;
					} else {
						for (String[] directoryData: entry.getValue()) {
							String valuePath = directoryData[0].substring(directoryData[0].indexOf(':') + 1);
							if (valuePath.equals(file.toString())) {
								entry.getValue().remove(directoryData);
								return directoryMap;
							}
						}
					}
				}
			}
		}
		return null;
	}
*/

	
	public void updateCurrentDirectory(InputStream socketInputStream) {
		try {
			Path newCurrentPath = Path.of(getStringHeader(socketInputStream));
			this.rootDirectory = newCurrentPath.toFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createFolder(InputStream socketInputStream, String user) {
		try {
			DataInputStream dis = new DataInputStream(socketInputStream);
			Path folderPaht = Path.of(rootDirectory + File.separator + dis.readUTF());
			System.out.println("Creando nueva carpeta: " + folderPaht);
		    Files.createDirectory(folderPaht);
			if (Files.exists(folderPaht) && user != null) {
				writeFileBytes(folderPaht.resolve("OWNER.txt"), user.getBytes());
			}
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void writeFileBytes(Path filePath, byte[] content) {
		try (OutputStream os = Files.newOutputStream(filePath,
				StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			os.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] readFileBytes(Path confPath) {
		byte[] confFileData = null;
		try (InputStream is = Files.newInputStream(confPath, StandardOpenOption.READ)) {
			confFileData = is.readAllBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return confFileData;
	}
	
	public void downloadFile(InputStream socketInputStream, OutputStream socketOutputStream) {
		try (InputStream is = Files.newInputStream(Path.of(rootDirectory.getAbsolutePath() + File.separator + getStringHeader(socketInputStream)), 
				StandardOpenOption.READ)) {
			System.out.println("Start download");
			byte[] buffer = new byte[2048];
			int bytes;
			while ((bytes=is.read(buffer))!=-1) {
				socketOutputStream.write(buffer, 0, bytes);
				socketOutputStream.flush();
			}
			socketOutputStream.write(-1);
			socketOutputStream.flush();
			System.out.println("End download");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Obtiene el archivo enviado por el cliente y lo guarda
	 * */
	public void uploadFile(InputStream socketInputStream) {
		try (OutputStream os = Files.newOutputStream(Path.of(rootDirectory.getAbsolutePath() + File.separator + getStringHeader(socketInputStream)),
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
			System.out.println("Start upload");
			byte[] buffer = new byte[2048];
			int bytes;
			while ((bytes=socketInputStream.read(buffer))!=-1) {
				if (bytes == 1 && buffer[0] == -1) {
					break;
				}
				os.write(buffer, 0, bytes);
			}
			os.flush();
			System.out.println("End upload");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteFile(InputStream sockInputStream) {
		try {
			DataInputStream dis = new DataInputStream(sockInputStream);
			Path filePath = Path.of(rootDirectory + File.separator + dis.readUTF());
			System.out.println("Eliminando archivo: " + filePath);
		    Files.deleteIfExists(filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deleteFolder(InputStream socketInputStream) {
		try {
			DataInputStream dis = new DataInputStream(socketInputStream);
			Path filePath = Path.of(rootDirectory + File.separator + dis.readUTF());
			System.out.println("Eliminando carpeta: " + filePath);
			try (Stream<Path> walker = Files.walk(filePath)) {
				walker.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Lee el tamaño de un archivo y luego su nombre
	private String getStringHeader(InputStream socketInputStream) throws IOException {
		int stringLength = socketInputStream.read();
		byte[] buffer = new byte[stringLength];
		int stringBytes = socketInputStream.read(buffer);
	    return new String(buffer, 0, stringBytes, StandardCharsets.UTF_8);
	}

}
