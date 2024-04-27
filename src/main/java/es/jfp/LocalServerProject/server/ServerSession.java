package es.jfp.LocalServerProject.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.log4j.Logger;

import es.jfp.LocalServerProject.server.enums.ClientAction;

public class ServerSession implements Runnable {
	
	
	private Socket clientSocket;
	private File rootPath;
	private final FileManager fileManager;
	private final UserAuthenticator userAuth;
	private String user;
	
	private InputStream is;
	private OutputStream os;
	
	public ServerSession(Socket socket, File rootPath) {
		this.clientSocket = socket;
		this.rootPath = rootPath;
		this.fileManager = FileManager.getInstance(rootPath);
		this.userAuth = UserAuthenticator.getInstance();
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
		case READ_DIRECTORY: fileManager.getDirectoryMap(is, os); 
			break;
		case CREATE_FOLDER: fileManager.createFolder(is); 
			break;
		case UPLOAD_FILE: fileManager.uploadFile(is); 
			break;
		case DELETE_FILE: fileManager.deleteFile(is);
			break;
		case DELETE_FOLDER:
			break;
		case DOWNLOAD_FILE: fileManager.downloadFile(is, os);
			break;
		case LOGIN: this.user = userAuth.loginUser(is, os);
			break;
		case LOGOFF:
			break;
		case REGISTER: this.user = userAuth.registerUser(is, os);
			break;
		case UPDATE_FILE:
			break;
		case UPDATE_FOLDER:
			break;
		case USER_EXISTS:
			break;
		}
	}
	
	private ClientAction getClientAction() throws IOException {
		int code = is.read();
		System.out.println(code);
		ClientAction action = ClientAction.getByCode(code);
		System.out.printf("El client %s vol fer %s%n", clientSocket, action);
		return action;
	}

}
