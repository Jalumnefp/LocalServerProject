package es.jfp.LocalServerProject;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.sun.tools.javac.launcher.Main;

import es.jfp.LocalServerProject.server.ServerSetup;


public class App {
		
    public static void main( String[] args ){
    	List<String> argsList = Arrays.asList(args);
    	boolean nogui = argsList.contains("nogui");

    	Thread serverSetupThread = new Thread(new ServerSetup(nogui), "Thread-ServerSetup");
    	serverSetupThread.start();
    }
}