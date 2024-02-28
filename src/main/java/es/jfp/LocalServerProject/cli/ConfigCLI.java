package es.jfp.LocalServerProject.cli;

import java.nio.file.Path;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.jfp.LocalServerProject.utils.FileManager;
import es.jfp.LocalServerProject.utils.FormatManager;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.TEXT_COLOR;;

public class ConfigCLI {
	
	private final FormatManager formatManager;
	private final Scanner sc;
	private String ipv4;
	private String port;
	private String password;
	private String storagePath;
	
	
	public ConfigCLI() {
		this.formatManager = FormatManager.getInstance();
		this.sc = new Scanner(System.in);
	}
	
	
	public void start() {
		
		welcomeConfMessage();
		networkConfRequest();
		loginConfRequest();
		storageConfRequest();
		saveConfiguration();
		
	}
	
	private void welcomeConfMessage() {
		String liArrow = colorize("=> ", TEXT_COLOR(226));
		String welcomeMessage = colorize("¡BIENVENID@ AL MENÚ DE CONFIGURACIÓN!", TEXT_COLOR(208));
		String titleList = colorize("La ventana de configuración se abre en los siguientes casos", TEXT_COLOR(226));
		String option1 = "Es la primera vez que enciendes el servidor y tienes que configurarlo";
		String option2 = "El archivo config.txt ha sido dañado y el servidor necesita crearlo de nuevo";
		String option3 = "Has decidido cambiar la configuración por voluntad propia";
		System.out.printf("%s%n%n%s%n\t%s%s%n\t%s%s%n\t%s%s%n%n%n", 
				welcomeMessage, titleList, liArrow, option1, liArrow, option2, liArrow, option3);
	}
	
	private void networkConfRequest() {
		String title = "Paso 1: Configuración de la red";
		String descr = "Este apartado está destinado a las configuraciones de la red.";
		System.out.printf("%s%n%s%n", title, descr);
		ipv4 = requestValue("Introducir ipv4 del servidor: ", formatManager.validateIpv4Format());
		port = requestValue("Introducir puerto del servidor: ", formatManager.validatePortFormat());
		
	}
	
	private void loginConfRequest() {
		String title = "Paso 2: Configuración de inicio de sesión";
		String descr = "Este apartado está destinado a las configuraciones del inicio de sesión";
		System.out.printf("%s%n%s%n", title, descr);
		password = requestValue("Introducir contraseña: ", formatManager.validatePasswordFormat());
	}
	
	private void storageConfRequest() {
		String title = "Paso 3: Configuración de almacenamiento";
		String descr = "Este apardado tal y tal";
		System.out.printf("%s%n%s%n", title, descr);
		storagePath = requestValue("Introducir ruta raíz de almacenamiento: ", formatManager.validatePathFormat());
	}
	
	private void saveConfiguration() {
		FileManager fm = FileManager.getInstance();
		String config = formatManager.getFormatedConfigData(ipv4, port, password, storagePath);
		fm.writeFileBytes(Path.of("config.txt"), config.getBytes());
	}
	
	private String requestValue(String message, Pattern pattern) {
		String value;
		Matcher matcher;
		do {
			System.out.print(message);
			value = sc.nextLine();
			matcher = pattern.matcher(value);
			if (!matcher.matches()) {
				System.err.println("[ERROR] Comprueba que la información introducida es correcta.");
			}
		} while (!matcher.matches());
		return value;
	}
	
	
}
