package es.jfp.LocalServerProject.server;

import es.jfp.LocalServerProject.server.services.DirectoryListener;
import es.jfp.LocalServerProject.ui.server.ServerFrame;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {
	
	private static List<Socket> currentSockets = new ArrayList<>();
	
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

		System.out.printf("[%s] Inicializando servidor...%n", Thread.currentThread().getName());
		
		initialize();
		
		System.out.printf("[%s] Escuchando clientes...%n", Thread.currentThread().getName());
		
		startListening();
		
	}
	
	private void initialize() {
		
		try {
			new Thread(new DirectoryListener(rootStorage), "DirectoryListener").start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (!nogui) {
			SwingUtilities.invokeLater(() -> {
				new ServerFrame();
			});
		} else {
			
		}
	}
	
	private void startListening() {
		try (ServerSocket serverSocket = new ServerSocket(this.port)) {
			
			while (true) {
				Socket socket = serverSocket.accept();

				System.out.printf("[%s] Se ha conectado un cliente: %s\n",
						Thread.currentThread().getName(), socket);

				currentSockets.add(socket);
				ServerSession serverSession = new ServerSession(socket, rootStorage);
				Thread sessionThread = new Thread(serverSession, "Session{" + socket + '}');
				sessionThread.start();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<Socket> getCurrentSockets() {
		return Server.currentSockets;
	}

}
