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

public class NetworkConfigPanel extends JPanel {
	
	private String ipv4 = "";
	private String port = "";
	
	private final String descr = "<html>Este apartado está destinado a las configuraciones de la red.</html>";
	private final JLabel networkDescrLabel = new JLabel(descr);
	private final JTextField ipv4TextField = new JTextField(ipv4);
	private final JTextField portTextField = new JTextField(port);
	private final JLabel ipv4FieldLabel = new JLabel("<html>Ipv4 del servidor</html>");
	private final JLabel portFieldLabel = new JLabel("<html>Puerto del servidor</html>");
	private final JLabel separatorLabel = new JLabel("");
	
	private List<JComponent> gridElements = new LinkedList<>();
	
	public NetworkConfigPanel() {
		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(0, 20, 0, 20));
		
		this.gridElements.add(networkDescrLabel);
		networkDescrLabel.setBorder(new EmptyBorder(0, 0, 25, 0));
		
		separatorLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
				
		this.gridElements.add(ipv4FieldLabel);
		this.gridElements.add(ipv4TextField);
		this.gridElements.add(separatorLabel);
		
		this.gridElements.add(portFieldLabel);
		this.gridElements.add(portTextField);

		
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
	
	public String getIpv4Value() {
		return ipv4TextField.getText();
	}
	
	public String getPortValue() {
		return portTextField.getText();
	}
	
}
