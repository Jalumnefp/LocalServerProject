package es.jfp.LocalServerProject.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class FileManager {
	
	private static FileManager instance = null;
	
	private FileManager() {}
	
	public static FileManager getInstance() {
		synchronized (FileManager.class) {
			if (instance == null) {
				instance = new FileManager();
			}
			return instance;
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

}
