package es.jfp.LocalServerProject.server.services;

import es.jfp.LocalServerProject.server.FileManager;
import es.jfp.LocalServerProject.server.Server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Clase que se encarga de actualizar en tiempo real la estructura de directorios de todos los clientes del servidor
 * @author Jaume Ferrà Pérez
 * @version 1.0
 * @see WatchService
 * @see Files
 */
public class DirectoryListener implements Runnable {

    private final long fileId = 0;
    private final File rootDirectory;
    private final WatchService watchService;
    private Map<String, List<String[]>> directorMap;
	private final FileManager fm;

    /**
     * Constructor de DirectoryListener
     */
    public DirectoryListener(File rootDirectory) throws IOException {
        this.rootDirectory = rootDirectory;
        this.fm = FileManager.getInstance(rootDirectory);
        //fm.mapDirectory(rootDirectory, 0, rootDirectory.getName());
        this.watchService = fm.watchService;
    }

    
    @Override
    public void run() {

        Server.writeConsole(String.format("[%s] Escuchando directorio raíz [%s]",
                Thread.currentThread().getName(), rootDirectory.getName()));

        try {

            handleDirectoryListener((watchEvent, context) -> {

                Server.writeConsole(String.format("[%s] Cambio detectado [Event: %s, File: %s, Context: %s]",
                        Thread.currentThread().getName(), watchEvent.kind().name(), watchEvent.context(), context));
                File contextFile = new File(context + File.separator + watchEvent.context());
                System.out.println(contextFile);
                fm.updateDirectoryMap();
                
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }

    /**
     * Método que envia a todos los sockets actuales del servidor el mapa de directorios
     */
    private void broadcastDirectoryMap() {
        List<Socket> currentSockets = Server.getCurrentSockets();
        //System.err.println(currentSockets);
        currentSockets.forEach(socket -> {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(directorMap);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    
    public Map<String, List<String[]>> getCurrentDirectoryMap() {
    	return this.directorMap;
    }

    /**
     * Método que escucha los eventos de WatchService y, en caso de que surja uno, ejecuta la función pasada como parámetro
     */
    private void handleDirectoryListener(BiConsumer<WatchEvent<Path>, Watchable> consumer) throws InterruptedException {
        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                consumer.accept((WatchEvent<Path>) event, key.watchable());
            }
            key.reset();
        }
    }

}