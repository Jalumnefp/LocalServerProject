package es.jfp.LocalServerProject.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.SwingUtilities;

import es.jfp.LocalServerProject.ui.server.ServerFrame;

public class Server implements Runnable {
	
	private boolean nogui;
	
	private InetAddress ipv4;
	private int port;
	private File rootStorage;
	
	public Server(boolean nogui, InetAddress ipv4, int port, File rootStorage) {
		this.nogui = nogui;
		this.ipv4 = ipv4;
		this.port = port;
		this.rootStorage = rootStorage;
	}

	@Override
	public void run() {

		System.out.println("Server start!");
		
		if (!nogui) {
			SwingUtilities.invokeLater(() -> {
				new ServerFrame();
			});
		}
		
		startListening();
		
	}
	
	private void startListening() {
		try (ServerSocket svsocket = new ServerSocket(this.port)) {
			
			Socket socket = svsocket.accept();
			
			new Thread(new ServerSession(socket, rootStorage)).start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
