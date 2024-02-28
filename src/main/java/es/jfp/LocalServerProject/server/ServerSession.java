package es.jfp.LocalServerProject.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerSession implements Runnable {
	
	private Socket clientSocket;
	private File rootPath;
	
	private InputStream is;
	private OutputStream os;
	
	public ServerSession(Socket socket, File rootPath) {
		this.clientSocket = socket;
		this.rootPath = rootPath;
	}

	@Override
	public void run() {
		
		try {
			is = new BufferedInputStream(clientSocket.getInputStream());
			os = Files.newOutputStream(Path.of(rootPath.getAbsolutePath() + "/new2.iso"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(true) {
			try {
				byte[] buffer = new byte[1024];
				int bytes;
				while ((bytes=is.read(buffer))!=-1) {
					os.write(buffer, 0, bytes);
				}
				os.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
