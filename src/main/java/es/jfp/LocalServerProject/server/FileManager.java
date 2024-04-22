package es.jfp.LocalServerProject.server;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.tree.TreeNode;

public final class FileManager {
	
	private static FileManager instance;
	
	private File rootDirectory;
	
	private long fileId = 0;
	
	
	private FileManager(File rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
	
	
	public static FileManager getInstance(File rootDirectory) {
		synchronized (FileManager.class) {
			if (instance==null) {
				instance = new FileManager(rootDirectory);
			}
			return instance;
		}
	}

	public void getDirectoryMap(InputStream socketInputStream, OutputStream socketOutputStream) {
		
		try {
			ObjectOutputStream oos = new ObjectOutputStream(socketOutputStream);
			Map<String, List<String[]>> directoryTree = mapDirectory(rootDirectory, 0, rootDirectory.getName());
			oos.writeObject(directoryTree);
			oos.flush();
			System.out.println("Mapa de directorios enviado.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	private Map<String, List<String[]>> mapDirectory(File file, int level, String parent) {
		Map<String, List<String[]>> tree = new HashMap<>();
		if (level==0) {
			List<String[]> rootList = new LinkedList<>();
		    rootList.add(new String[] {rootDirectory.getName(), "r"});
		    tree.put("ROOT", rootList);
		}
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(file.toPath())) {
        	for (Path p: ds) {
                String child = file.toPath().relativize(p).toString() + '?' + fileId++;
                if (!tree.containsKey(parent)) {
                    tree.put(parent, new LinkedList<>());
                }
                tree.get(parent).add(new String[] { child, (p.toFile().isDirectory() ? ("d") : "f")});
                if(p.toFile().isDirectory()) {
                    tree.putAll(mapDirectory(p.toFile(), level + 1, child));
                }
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }
        return tree;
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
			System.out.println("Creando nueva carpeta: " + rootDirectory + folderPaht);
		    Files.createDirectory(folderPaht);
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public void deleteFolder() {
		// EN CASO DE QUE EL DIRECTORIO TENGA HIJOS PREGUNTAR
	}
	
	public void downloadFile(OutputStream socketOutputStream) {
		try (InputStream is = Files.newInputStream(Path.of(rootDirectory.getAbsolutePath()), StandardOpenOption.READ)) {
			
			byte[] buffer = new byte[2048];
			int bytes;
			while ((bytes=is.read(buffer))!=-1) {
				socketOutputStream.write(buffer, 0, bytes);
			}
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
			Path filePath = Path.of(getStringHeader(sockInputStream));
			Files.delete(filePath);
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
