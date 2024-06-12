package es.jfp.LocalServerProject.server;

import es.jfp.SerialFile;
import es.jfp.SerialMap;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
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
				rootDirectory.isDirectory(),
				rootDirectory.length()
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
			Server.writeConsole(String.format("[%s] [User=%s] Mapa de directorios enviado", Thread.currentThread().getName(), username));

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
			e.printStackTrace();
		}
		directoryMap.forEachSerialFiles(serialFile -> {
			Path path = Path.of(rootDirectory.getAbsolutePath(), serialFile.getDirectory());
			if (path.toFile().isDirectory()) {
				try {
					path.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
				} catch (IOException e) {
					e.printStackTrace();
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
					path.toFile().isDirectory(),
					path.toFile().length()
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
			e.printStackTrace();
		}
		return children;
	}

	
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
			Server.writeConsole(String.format("[FileManager] [%s] Iniciando descarga", Thread.currentThread().getName()));
			byte[] buffer = new byte[2048];
			int bytes;
			while ((bytes=is.read(buffer))!=-1) {
				socketOutputStream.write(buffer, 0, bytes);
				socketOutputStream.flush();
			}
			socketOutputStream.write(-1);
			socketOutputStream.flush();
			Server.writeConsole(String.format("[FileManager] [%s] Final de la descarga", Thread.currentThread().getName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Obtiene el archivo enviado por el cliente y lo guarda
	 **/
	public synchronized void uploadFile(InputStream socketInputStream) {
		try (OutputStream os = Files.newOutputStream(Path.of(rootDirectory.getAbsolutePath() + File.separator + getStringHeader(socketInputStream)),
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
			Server.writeConsole(String.format("[FileManager] [%s] Iniciar carga", Thread.currentThread().getName()));
			byte[] buffer = new byte[2048];
			int bytes;
			while ((bytes=socketInputStream.read(buffer))!=-1) {
				if (bytes == 1 && buffer[0] == -1 || bytes < buffer.length) {
					break;
				}
				os.write(buffer, 0, bytes);
			}
			os.flush();
			Server.writeConsole(String.format("[FileManager] [%s] Final de la carga", Thread.currentThread().getName()));
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
