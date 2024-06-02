package es.jfp.LocalServerProject.server;

import es.jfp.LocalServerProject.server.enums.ClientAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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

		System.out.printf("[%s] [User=%s] Sesión creada\n", Thread.currentThread().getName(), user);

		boolean sessionClosed = false;
		while(!sessionClosed) {
			System.out.printf("[%s] [User=%s] Esperando peticiones...\n", Thread.currentThread().getName(), user);
			try {
				ClientAction action = getClientAction();
				sessionClosed = doClientAction(action);
			} catch (IOException e) {
				System.err.printf("[%s] [User=%s] Salida forzada.\n", Thread.currentThread().getName(), user);
				Server.getCurrentSockets().remove(clientSocket);
				break;
			}
		}

		System.out.printf("[%s] Cerrando sessión...", Thread.currentThread().getName());
		
	}
	
	private boolean doClientAction(ClientAction action) {
		switch (action) {
			case READ_DIRECTORY: fileManager.getDirectoryMap(os, user); break;
			case CREATE_FOLDER: fileManager.createFolder(is, user); break;
			case UPLOAD_FILE:  fileManager.uploadFile(is); break;
			case DELETE_FILE:  fileManager.deleteFile(is); break;
			case DELETE_FOLDER:  fileManager.deleteFolder(is); break;
			case DOWNLOAD_FILE:  fileManager.downloadFile(is, os); break;
			case LOGIN: {
				this.user = userAuth.loginUser(is, os);
				System.out.printf("[%s] Usuario adueñado: %s\n", Thread.currentThread().getName(), user);
				break;
			}
			case LOGOFF:  {
				System.out.printf("[%s] Usuario abandonado: %s\n", Thread.currentThread().getName(), user);
				this.user = null;
				break;
			}
			case REGISTER:  this.user = userAuth.registerUser(is, os); break;
			/*case UPDATE_FILE:  fileManager.updateCurrentDirectory(null); break;
			case UPDATE_FOLDER:  fileManager.updateCurrentDirectory(null); break;
			case USER_EXISTS:  fileManager.updateCurrentDirectory(null); break;*/
			case CLOSE_SESSION:  {
				return true;
			}
		}
		return false;
	}
	
	private ClientAction getClientAction() throws IOException {
		int code = is.read();
		System.out.printf("[%s] [User=%s] Request code: %d\n", Thread.currentThread().getName(), user, code);
		ClientAction action = ClientAction.getByCode(code);
		System.out.printf("[%s] [User=%s] Acción solicitada: %s\n", Thread.currentThread().getName(), user, action);
		return action;
	}

}
