package es.jfp.LocalServerProject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import es.jfp.LocalServerProject.utils.FileManager;
import es.jfp.LocalServerProject.server.ServerSetup;


public class App {
		
    public static void main( String[] args ){
    	List<String> argsList = Arrays.asList(args);
    	boolean nogui = argsList.contains("nogui");
		boolean reconfigure = argsList.contains("configure");

		if (reconfigure) {
			Path confFile = Path.of("files/conf/config.txt");
			if (Files.exists(confFile)) {
				FileManager.getInstance().writeFileBytes(confFile, "RECONFIGURE".getBytes());
			}
		}

    	ServerSetup serverSetup = new ServerSetup(nogui);
    	Thread serverSetupThread = new Thread(serverSetup, "ServerSetup");
    	serverSetupThread.start();
    }
}