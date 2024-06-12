package es.jfp.LocalServerProject.server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.function.BiFunction;

public final class UserAuthenticator {
	
	private static UserAuthenticator instance;
	private Connection connection;
	
	
	private UserAuthenticator() {
		try {
			Server.writeConsole(String.format("[%s] Conectando a la base de datos...", Thread.currentThread().getName()));

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
		Server.writeConsole(String.format("[%s] Comprobando tablas de la base de datos...", Thread.currentThread().getName()));
		String query = "CREATE TABLE IF NOT EXISTS users ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "name VARCHAR(50) NOT NULL,"
				+ "sha256_password VARCHAR(64),"
				+ "UNIQUE(name)"
				+ ")";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean insertUser(String username, String password) {
		Server.writeConsole(String.format("[%s] Insertando nuevo usuario: {name: %s, sha256_password: %s}",
				Thread.currentThread().getName(), username, password));
		String query = "INSERT INTO users(name, sha256_password) VALUES (?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setString(2, password);
			
			int rowsInserted = pstmt.executeUpdate();
			boolean successful = rowsInserted > 0;

			if (successful) {
				Server.writeConsole("[UserAuthenticator] Usuario insertado correctamente");
			}

			return successful;
			
		} catch (SQLException e) {
			Server.writeConsole(String.format("[%s] Error al insertar el nuevo usuario: {name: %s, sha256_password: %s}: %s\n",
					Thread.currentThread().getName(), username, password, e));
			return false;
		}
	}
	
	private boolean verifyUser(String username, String password) {
		Server.writeConsole(String.format("[%s] Verificando nuevo usuario: {name: %s, sha256_password: %s}\n",
				Thread.currentThread().getName(), username, password));
		String query = "SELECT COUNT(*) AS matches FROM users WHERE name = ? AND sha256_password = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setString(2, password);
			
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				int matches = rs.getInt("matches");
				boolean successful = matches > 0;
				if (successful) {
					Server.writeConsole("[UserAuthenticator] Usuario verificado");
				}
				return successful;
			}
			
		} catch (SQLException e) {
			Server.writeConsole(String.format("[%s] Error al verificar el usuario: {name: %s, sha256_password: %s}: %s\n",
					Thread.currentThread().getName(), username, password, e));
			return false;
		}
		return false;
	}

	private String processUser(InputStream is, OutputStream os, BiFunction<String, String, Boolean> actionFunction) {
		Server.writeConsole(String.format("[%s] Procesando usuario...\n", Thread.currentThread().getName()));
		DataInputStream dis = new DataInputStream(is);
		DataOutputStream dos = new DataOutputStream(os);
		try {
			String username = dis.readUTF();
			String password = dis.readUTF();
			boolean successfully = actionFunction.apply(username, password);
			Server.writeConsole("[User Authenticator] Proceso de usuario: " + successfully);
			dos.writeBoolean(successfully);
			dos.flush();
			return successfully ? username : null;
		} catch (IOException e) {
			Server.writeConsole(String.format("[%s] Error al procesar el usuario: %s\n", Thread.currentThread().getName(), e));
			return null;
		}
	}

}
