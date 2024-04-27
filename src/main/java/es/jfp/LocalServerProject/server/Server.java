package es.jfp.LocalServerProject.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

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
		} else {
		}
		
		startListening();
		
	}
	
	private void startListening() {
		try (ServerSocket svsocket = new ServerSocket(this.port)) {
			
			while (true) {
				Socket socket = svsocket.accept();
				
				new Thread(new ServerSession(socket, rootStorage)).start();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
