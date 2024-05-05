package es.jfp.LocalServerProject.server;

import es.jfp.LocalServerProject.server.enums.ClientAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static es.jfp.LocalServerProject.server.enums.ClientAction.CLOSE_SESSION;

public class ServerSession implements Runnable {
	
	
	private final Socket clientSocket;
	private final FileManager fileManager;
	private final UserAuthenticator userAuth;
	private String user;

	private InputStream is;
	private OutputStream os;
	
	public ServerSession(Socket socket, File rootPath) {
		this.clientSocket = socket;
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

		System.out.printf("[%s] Sesión creada\n", Thread.currentThread().getName());

		boolean sessionClosed = false;
		while(!sessionClosed) {
			try {
				ClientAction action = getClientAction();
				sessionClosed = doClientAction(action);
			} catch (IOException e) {
				System.err.printf("[%s] Salida forzada.\n", Thread.currentThread().getName());
				Server.getCurrentSockets().remove(clientSocket);
				break;
			}
		}

		System.out.printf("[%s] Cerrando sessión...", Thread.currentThread().getName());
		
	}
	
	private boolean doClientAction(ClientAction action) {
		switch (action) {
			case READ_DIRECTORY -> fileManager.getDirectoryMap(os);
			case CREATE_FOLDER -> fileManager.createFolder(is);
			case UPLOAD_FILE -> fileManager.uploadFile(is);
			case DELETE_FILE -> fileManager.deleteFile(is);
			case DELETE_FOLDER -> fileManager.deleteFolder();
			case DOWNLOAD_FILE -> fileManager.downloadFile(is, os);
			case LOGIN -> this.user = userAuth.loginUser(is, os);
			case LOGOFF -> this.user = null;
			case REGISTER -> this.user = userAuth.registerUser(is, os);
			case UPDATE_FILE -> fileManager.updateCurrentDirectory(null);
			case UPDATE_FOLDER -> fileManager.updateCurrentDirectory(null);
			case USER_EXISTS -> fileManager.updateCurrentDirectory(null);
			case CLOSE_SESSION -> {
				return true;
			}
		}
		return false;
	}
	
	private ClientAction getClientAction() throws IOException {
		int code = is.read();
		System.out.printf("[%s] Request code: %d", Thread.currentThread().getName(), code);
		ClientAction action = ClientAction.getByCode(code);
		System.out.printf("[%s] Acción solicitada: %s\n", Thread.currentThread().getName(), action);
		return action;
	}

}
