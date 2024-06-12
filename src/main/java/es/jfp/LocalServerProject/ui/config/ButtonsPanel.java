package es.jfp.LocalServerProject.ui.config;

import java.awt.BorderLayout;
import java.nio.file.Path;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import es.jfp.LocalServerProject.server.ServerSetup;
import es.jfp.LocalServerProject.utils.FileManager;

public class ButtonsPanel extends JPanel {
	
	private final FileManager fileManager;
	private final JButton saveButton;
	private final JButton exitButton;
	
	public ButtonsPanel() {
		
		saveButton = new JButton("SAVE");
		exitButton = new JButton("EXIT");
		fileManager = FileManager.getInstance();
		
		setLayout(new BorderLayout(0, 0));
		add(saveButton, BorderLayout.CENTER);
		add(exitButton, BorderLayout.WEST);
		
		exitButton.addActionListener(action -> {
			String message = "¿Seguro que quieres salir?";
			String title = "Atención";
			int response = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
			if (response == JOptionPane.OK_OPTION) {
				System.exit(0);
			}
		});
				
		saveButton.addActionListener(action -> {
			ConfigFrame parent = (ConfigFrame) SwingUtilities.getAncestorOfClass(ConfigFrame.class, ButtonsPanel.this);
			String config = parent.getFormatedConfData();
			fileManager.writeFileBytes(Path.of("files/conf/config.txt"), config.getBytes());
			synchronized (ServerSetup.class) {
				ServerSetup.class.notify();
			}
			parent.dispose();
		});
		
	}

}
