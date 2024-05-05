package es.jfp.LocalServerProject;

import java.util.Arrays;
import java.util.List;

import es.jfp.LocalServerProject.server.ServerSetup;


public class App {
		
    public static void main( String[] args ){
    	List<String> argsList = Arrays.asList(args);
    	boolean nogui = argsList.contains("nogui");

    	ServerSetup serverSetup = new ServerSetup(nogui);
    	Thread serverSetupThread = new Thread(serverSetup, "ServerSetup");
    	serverSetupThread.start();
    }
}