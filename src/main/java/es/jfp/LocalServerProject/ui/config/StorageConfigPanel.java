package es.jfp.LocalServerProject.ui.config;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class StorageConfigPanel extends JPanel {
	
	private String storagePath = null;
	
	private final String descr = "<html>Aquí haremos las configuraciones relacionadas con el almacenamiento.</html>";
	private final JLabel loginDescrLabel = new JLabel(descr);
	private final JFileChooser fileChooser = new JFileChooser();
	private final JButton storagePathButton = new JButton("Seleccionar directorio");
	private final JLabel storageResult = new JLabel();
	private final JLabel storageDescrLabel = new JLabel("<html>Selecciona el directorio donde quieres que opere el servidor</html>");
	private final JLabel storageFormatErrorLabel = new JLabel("<html>Este campo es obligatorio!</html>");
	
	private List<JComponent> gridElements = new LinkedList<>();
	
	public StorageConfigPanel() {
		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(0, 20, 0, 20));
		
		this.gridElements.add(loginDescrLabel);
		loginDescrLabel.setBorder(new EmptyBorder(0, 0, 25, 0));
		
				
		this.gridElements.add(storageDescrLabel);
		this.gridElements.add(storagePathButton);
		this.gridElements.add(storageResult);
		this.gridElements.add(storageFormatErrorLabel);
		storageFormatErrorLabel.setForeground(Color.RED);
		storageFormatErrorLabel.setVisible(false);

		
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		
		for (int y = 0; y < gridElements.size(); y++) {
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = y;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 0.0;
			this.add(gridElements.get(y), gridBagConstraints);
		}
		
		this.fileChooser.setDialogTitle("Explorador de archivos");
		this.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		storagePathButton.addActionListener(a -> {
			fileChooser.showDialog(this, "Seleccionar");
			storagePath = fileChooser.getSelectedFile().getAbsolutePath();
			storageResult.setText("<html>Selección: "+this.storagePath+"</html>");
		});
		
	}
	
	public String getStoragePath() {
		return this.storagePath;
	}
	
	public void setStorageErrorLabelVisible(boolean visible) {
		this.storageFormatErrorLabel.setVisible(visible);
	}

}
