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

		Server.writeConsole(String.format("[%s] [User=%s] Sesión creada", Thread.currentThread().getName(), user));

		boolean sessionClosed = false;
		while(!sessionClosed) {
			Server.writeConsole(String.format("[%s] [User=%s] Esperando peticiones...", Thread.currentThread().getName(), user));
			try {
				ClientAction action = getClientAction();
				sessionClosed = doClientAction(action);
			} catch (IOException e) {
				Server.writeConsole(String.format("[%s] [User=%s] Salida forzada.", Thread.currentThread().getName(), user));
				Server.getCurrentSockets().remove(clientSocket);
				break;
			}
		}

		Server.writeConsole(String.format("[%s] Cerrando sessión...", Thread.currentThread().getName()));

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
				Server.writeConsole(String.format("[%s] Usuario adueñado: %s", Thread.currentThread().getName(), user));
				break;
			}
			case LOGOFF:  {
				Server.writeConsole(String.format("[%s] Usuario abandonado: %s", Thread.currentThread().getName(), user));
				this.user = null;
				break;
			}
			case REGISTER:  this.user = userAuth.registerUser(is, os); break;
			case CLOSE_SESSION:  {
				return true;
			}
			case PING: Server.writeConsole(String.format("[%s] [Usuario: %s] PING", Thread.currentThread().getName(), user));
			default: Server.writeConsole(
					String.format("[%s] [Usuario: %s] Ha solicitado una acción no registrada", Thread.currentThread().getName(), user)
			);
		}
		return false;
	}
	
	private ClientAction getClientAction() throws IOException {
		int code = is.read();
		Server.writeConsole(String.format("[%s] [User=%s] Request code: %d", Thread.currentThread().getName(), user, code));
		ClientAction action = ClientAction.getByCode(code);
		Server.writeConsole(String.format("[%s] [User=%s] Acción solicitada: %s", Thread.currentThread().getName(), user, action));
		return action;
	}

}
