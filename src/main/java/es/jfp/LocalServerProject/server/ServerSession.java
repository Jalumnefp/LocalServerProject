package es.jfp.LocalServerProject.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import es.jfp.LocalServerProject.server.enums.ClientAction;

public class ServerSession implements Runnable {
	
	private Socket clientSocket;
	private File rootPath;
	private final FileManager fileManager;
	
	private InputStream is;
	private OutputStream os;
	
	public ServerSession(Socket socket, File rootPath) {
		this.clientSocket = socket;
		this.rootPath = rootPath;
		this.fileManager = FileManager.getInstance(rootPath);
		try {
			this.is = clientSocket.getInputStream();
			this.os = clientSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		while(true) {
			try {
				System.out.println("Esperant petició ...");
				ClientAction action = getClientAction();
				doClientAction(action);
			} catch (IOException e) {
				System.err.printf("El client %s ha fet una eixida forçada.", clientSocket);
				break;
			}
		}
		
	}
	
	private void doClientAction(ClientAction action) {
		switch (action) {
		case READ_DIRECTORY: fileManager.getDirectoryMap(is, os); break;
		case CREATE_FOLDER: fileManager.createFolder(is); break;
		case UPLOAD_FILE: fileManager.uploadFile(is); break;
		}
	}
	
	private ClientAction getClientAction() throws IOException {
		int mainByte = is.read();
		ClientAction action = ClientAction.values()[mainByte];
		System.out.printf("El client %s vol fer %s%n", clientSocket, action);
		return action;
	}

}
