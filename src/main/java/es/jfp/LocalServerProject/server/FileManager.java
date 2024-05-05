package es.jfp.LocalServerProject.server;

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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

public final class FileManager {
	
	private static FileManager instance;
	
	private File rootDirectory;
	public WatchService watchService;
	public Map<String, List<String[]>> directoryMap;
	private long fileId = 0;
	
	
	private FileManager(File rootDirectory) {
		this.rootDirectory = rootDirectory;
		this.directoryMap = new HashMap<>();
		try {
			this.watchService = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static FileManager getInstance(File rootDirectory) {
		synchronized (FileManager.class) {
			if (instance==null) {
				instance = new FileManager(rootDirectory);
			}
			return instance;
		}
	}

	public void getDirectoryMap(OutputStream socketOutputStream) {

		try {
			ObjectOutputStream oos = new ObjectOutputStream(socketOutputStream);
			/*directoryTree.entrySet().forEach(e -> {
	            System.out.print(e.getKey() + "=> [");
	            e.getValue().forEach(a -> {
	                for (String s: a) {
	                    System.out.print(s);
	                }
	            });
	            System.out.println("]");
	        });*/
			oos.writeObject(directoryMap);
			oos.flush();
			System.out.println("Mapa de directorios enviado.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public synchronized Map<String, List<String[]>> mapDirectory(File file, int level, String parent) throws IOException {
		if (level==0) {
			System.out.printf("[%s] Mapeando directorios...\n", Thread.currentThread().getName());
			List<String[]> rootList = new LinkedList<>();
		    rootList.add(new String[] {rootDirectory.getName(), "r"});
		    file.toPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE);
		    directoryMap.put("ROOT", rootList);
		}
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(file.toPath())) {
        	for (Path p: ds) {
                String child = file.toPath().relativize(p).toString() + '?' + fileId++ + ':' + p;
                if (!directoryMap.containsKey(parent)) {
					directoryMap.put(parent, new LinkedList<>());
                }
				directoryMap.get(parent).add(new String[] { child, (p.toFile().isDirectory() ? ("d") : "f")});
                if(p.toFile().isDirectory()) {
                	p.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
					directoryMap.putAll(mapDirectory(p.toFile(), level + 1, child));
                }
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }
        return directoryMap;
	}

	public synchronized Map<String, List<String[]>> applyChanges(File file, String event) {
		for (Map.Entry<String, List<String[]>> entry: directoryMap.entrySet()) {
			if (!entry.getKey().equals("ROOT")) {
				String keyPath;
				if (entry.getKey().equals(rootDirectory.getName())) {
					keyPath = rootDirectory.getAbsolutePath();
				} else {
					keyPath = entry.getKey().substring(entry.getKey().indexOf(':') + 1);
				}
				if (event.equals(ENTRY_CREATE.name())) {
					String newDirectoryName = file.getName() + '?' + fileId++ + ':' + file;
					if (file.getParent().equals(keyPath)) {
						directoryMap.get(entry.getKey()).add(new String[] { newDirectoryName, (file.isDirectory() ? ("d") : "f")});
						if (file.isDirectory()) {
							directoryMap.put(newDirectoryName, new LinkedList<>());
						}
						return directoryMap;
					}
				}
				if (event.equals(ENTRY_DELETE.name())) {
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


	
	public void updateCurrentDirectory(InputStream socketInputStream) {
		try {
			Path newCurrentPath = Path.of(getStringHeader(socketInputStream));
			this.rootDirectory = newCurrentPath.toFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createFolder(InputStream socketInputStream) {
		try {
			DataInputStream dis = new DataInputStream(socketInputStream);
			Path folderPaht = Path.of(rootDirectory + File.separator + dis.readUTF());
			System.out.println("Creando nueva carpeta: " + folderPaht);
		    Files.createDirectory(folderPaht);
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public void deleteFolder() {
		// EN CASO DE QUE EL DIRECTORIO TENGA HIJOS PREGUNTAR
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
				os.write(buffer, 0, bytes);
			}
			os.flush();
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
			Files.deleteIfExists(filePath);
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
