package es.jfp.LocalServerProject.server;

import java.io.*;
import java.sql.*;
import java.util.function.BiFunction;

public class UserAuthenticator {
	
	private static UserAuthenticator instance;
	private Connection connection;
	
	
	private UserAuthenticator() {
		try {
			System.out.printf("[%s] Conectando a la base de datos...\n", Thread.currentThread().getName());
			Class.forName("org.sqlite.JDBC");
			this.connection = DriverManager.getConnection("jdbc:sqlite:files/db/database.db");
			createUsersTableIfNotExists();
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static UserAuthenticator getInstance() {
		synchronized (UserAuthenticator.class) {
			if (instance==null) {
				instance = new UserAuthenticator();
			}
			return instance;
		}
	}
	
	
	public String loginUser(InputStream is, OutputStream os) {
		return processUser(is, os, this::verifyUser);
	}
	
	public String registerUser(InputStream is, OutputStream os) {
		return processUser(is, os, this::insertUser);
	}
	
	private void createUsersTableIfNotExists() {
		System.out.printf("[%s] Comprobando tablas de la base de datos...\n", Thread.currentThread().getName());
		String query = "CREATE TABLE IF NOT EXISTS users ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "name VARCHAR(50) NOT NULL,"
				+ "sha256_password VARCHAR(64)"
				+ ");";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean insertUser(String username, String password) {
		System.out.printf("Insertando nuevo usuario: {name: %s, sha256_password: %s}\n", username, password);
		String query = "INSERT INTO users(name, sha256_password) VALUES (?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setString(2, password);
			
			int rowsInserted = pstmt.executeUpdate();
			
			return rowsInserted > 0;
			
		} catch (SQLException e) {
			System.err.printf("[%s] Error al insertar el nuevo usuario: {name: %s, sha256_password: %s}: %s\n",
					Thread.currentThread().getName(), username, password, e);
		}
		return false;
	}
	
	private boolean verifyUser(String username, String password) {
		System.out.printf("Verificando nuevo usuario: {name: %s, sha256_password: %s}\n", username, password);
		String query = "SELECT COUNT(*) AS matches FROM users WHERE name = ? AND sha256_password = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setString(2, password);
			
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				int matches = rs.getInt("matches");
				return matches > 0;
			}
			
		} catch (SQLException e) {
			System.err.printf("[%s] Error al verificar el usuario: {name: %s, sha256_password: %s}: %s\n",
					Thread.currentThread().getName(), username, password, e);
		}
		return false;
	}
	
	private String processUser(InputStream is, OutputStream os, BiFunction<String, String, Boolean> actionFunction) {
		System.out.printf("[%s] Procesando usuario...", Thread.currentThread().getName());
		DataInputStream dis = new DataInputStream(is);
		DataOutputStream dos = new DataOutputStream(os);
		try {
			String username = dis.readUTF();
			String password = dis.readUTF();
			dos.writeBoolean(actionFunction.apply(username, password));
			return username;
		} catch (IOException e) {
			System.err.printf("[%s] Error al procesar el usuario: %s\n", Thread.currentThread().getName(), e);
		}
		return null;
	}

}
