package es.jfp.LocalServerProject.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import es.jfp.LocalServerProject.cli.ConfigCLI;
import es.jfp.LocalServerProject.ui.config.ConfigFrame;
import es.jfp.LocalServerProject.utils.FileManager;

public class ServerSetup implements Runnable {

	private final FileManager fileManager;
	private final ConfigFrame configFrame;
	private final ConfigCLI configCLI;
	private boolean nogui = false;
	private InetAddress ipv4;
	private int port;
	private String password;
	private File rootStorage;
	
	
	public ServerSetup(boolean nogui) {
		this.nogui = nogui;
		this.fileManager = FileManager.getInstance();
		this.configFrame = new ConfigFrame();
		this.configCLI = new ConfigCLI();

		requiredFilesExists();
	}
	

	@Override
	public void run() {
		System.out.printf("[%s] Configurando servidor\n", Thread.currentThread().getName());
		boolean configurationSuccessful = configureServer();
		if (configurationSuccessful) {
			if (login()) {
				Server server = new Server(this.nogui, this.ipv4, this.port, this.rootStorage);
				Thread serverThread = new Thread(server, "Server");
				serverThread.start();
			}
		} else {
			System.err.printf("[%s] Configuracion fallida\n", Thread.currentThread().getName());
		}
	}

	private void requiredFilesExists() {
		Arrays.asList(
				Path.of("files/conf/config.txt"),
				Path.of("files/db/database.db")
		).forEach(this::createFileIfNotExists);
	}

	private void createFileIfNotExists(Path path) {
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path.getParent());
				Files.createFile(path);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	
	/**
	 * Obtiene los datos de configuración, comprueba que sean correctos y los aplica.
	 * En caso contrario preguntará al usuario los datos de forma gráfica o por consola.
	 * */
	private boolean configureServer() {
		boolean successful = false;
		while (true) {
			String formatedConfig = getFormatedConfig();
			if (configFileIsOk(formatedConfig)) {
				Map<String, String> config = getConfig(formatedConfig);
				setConfig(config);
				successful = true;
				break;
			} else {
				if (nogui) {
					configCLI.start();
				} else {
					setupConfigurationGui();
				}
			}
		}
		return successful;
	}
	
	/**
	 * Verifica la contraseña de la configuración
	 * */
	private boolean login() {
		final Scanner sc = new Scanner(System.in);
		boolean pass = false;
		String passwordInput = null;
		if (nogui) {
			System.out.print("Contraseña: ");
			passwordInput = sc.nextLine();
			sc.close();
		} else {
			passwordInput = JOptionPane.showInputDialog("Contraseña");
		}
		if (passwordInput.equals(password)) {
			pass = true;
		} else {
	    	if (nogui) {
	    		System.err.println("[ERROR] CONTRASEÑA INCORRECTA");
	    	} else {
	    		JOptionPane.showMessageDialog(null, "CONTRASEÑA INCORRECTA", "Error en el login", JOptionPane.ERROR_MESSAGE);
	    	}
		}
		return pass;
	}
	
	/**
	 * Prepara una iu para que el usuario pueda configurar el servidor
	 * */
	private void setupConfigurationGui() {
		SwingUtilities.invokeLater(() -> {
			configFrame.setVisible(true);		
		});
		synchronized (ServerSetup.class) {
			try {
				ServerSetup.class.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Aplica los datos de configuración obtenidos
	 * */
	private void setConfig(Map<String, String> config) {
		try {
			this.ipv4 = InetAddress.getByName(config.get("IPV4"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.port = Integer.valueOf(config.get("PORT"));
		this.password = config.get("PASSWORD");
		this.rootStorage = new File(config.get("ROOT_DIRECTORY"));
	}
	
	/**
	 * Comprueba el formato string obtenido
	 * */
	private boolean configFileIsOk(String config) {
		String regex = "^IPV4=([0-9]{1,3}(\\.[0-9]{1,3}){3})\\nPORT=(\\d{1,5})\\nPASSWORD=([a-zA-Z0-9]+)\\nROOT_DIRECTORY=([\\/\\\\a-zA-Z0-9:]+)$";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(config);
		return matcher.matches();
	}
	
	/**
	 * Retorna un string con los datos del fichero
	 * */
	private String getFormatedConfig() {
		String config = "";
		byte[] configBytes = fileManager.readFileBytes(Path.of("files/conf/config.txt"));
		if (configBytes != null) {
			config = new String(configBytes, StandardCharsets.UTF_8);
		}
		return config;
	}
	
	/**
	 * Obtiene el string formateado y lo transforma en un map
	 * */
	private Map<String, String> getConfig(String formatedConfig) {
		Map<String, String> config = new HashMap<String, String>();
		for (String confItem: formatedConfig.split("\n")) {
			String[] item = confItem.split("=");
			config.put(item[0], item[1]);
		}
		return config;
	}

}
