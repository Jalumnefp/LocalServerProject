package es.jfp.LocalServerProject.ui.config;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class WelcomeConfigPanel extends JPanel {
	
	private final String welcomeMessage = "<html><h4>Bienvenidos al menú de configuración!</h4><p>Esta ventana se muestra en los siguientes casos:</p><ul><li>Es la primera vez que ejecutas el servidor y tienes que configurarlo</li><li>El archivo config.txt ha sido dañado y el servidor necesita crearlo de nuevo</li><li>Has decidido cambiar la configuración por voluntad propia</li></ul></html>";
	private final JLabel confDescrLabel = new JLabel(welcomeMessage);
	
	public WelcomeConfigPanel() {
		this.setLayout(new BorderLayout(0, 0));
		this.setBorder(new EmptyBorder(0, 20, 0, 20));
		this.add(confDescrLabel, BorderLayout.CENTER);
	}

}
