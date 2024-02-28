package es.jfp.LocalServerProject.ui.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class LoginConfigPanel extends JPanel {
		
	private final String descr = "<html>Este apartado contiene las configuraciones relacionadas con el inicio de sesión.</html>";
	private final JLabel loginDescrLabel = new JLabel(descr);
	private final JTextField passwordTextField = new JTextField();
	private final JLabel passwordFieldLabel = new JLabel("<html>Contraseña de inicio de sesión</html>");
	
	private List<JComponent> gridElements = new LinkedList<>();
	
	public LoginConfigPanel() {
		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(0, 20, 0, 20));
		
		this.gridElements.add(loginDescrLabel);
		loginDescrLabel.setBorder(new EmptyBorder(0, 0, 25, 0));
		
				
		this.gridElements.add(passwordFieldLabel);
		this.gridElements.add(passwordTextField);

		
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		
		for (int y = 0; y < gridElements.size(); y++) {
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = y;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 0.0;
			this.add(gridElements.get(y), gridBagConstraints);
		}
		
	}
	
	public String getPasswordValue() {
		return this.passwordTextField.getText();
	}

}
