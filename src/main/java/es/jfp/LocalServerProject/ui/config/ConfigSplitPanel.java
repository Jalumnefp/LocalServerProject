package es.jfp.LocalServerProject.ui.config;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

public class ConfigSplitPanel extends JSplitPane {
	
	private final JList<String> optionsList;
	private final WelcomeConfigPanel welcomeConfigPanel;
	private final NetworkConfigPanel networkConfigPanel;
	private final LoginConfigPanel loginConfigPanel;
	private final StorageConfigPanel storageConfigPanel;
	
	
	public ConfigSplitPanel() {
		
		optionsList = new JList<String>();
		welcomeConfigPanel = new WelcomeConfigPanel();
		networkConfigPanel = new NetworkConfigPanel();
		loginConfigPanel = new LoginConfigPanel();
		storageConfigPanel = new StorageConfigPanel();
		
		setResizeWeight(0.3);
		setLeftComponent(optionsList);
		setRightComponent(welcomeConfigPanel);
		
		setUpOptionsList();
		
		optionsList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
                String listSelectedValue = (String) optionsList.getSelectedValue();
                switch (listSelectedValue) {
	                case "Inicio": updateConfigPanel(welcomeConfigPanel); break;
	                case "Red": updateConfigPanel(networkConfigPanel); break;
	                case "Autenticación": updateConfigPanel(loginConfigPanel); break;
	                case "Almacentamiento": updateConfigPanel(storageConfigPanel); break;
                }
            }
		});
		
	}
	
	private void updateConfigPanel(JPanel panel) {
		setRightComponent(panel);
		revalidate();
		repaint();
	}
	
	private void setUpOptionsList() {
		optionsList.setValueIsAdjusting(true);
		optionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		optionsList.setModel(new AbstractListModel() {
			String[] values = new String[] {"Inicio", "Red", "Autenticación", "Almacentamiento"};
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
	}
	
	public String getFormatedConfigData() {
		String ipv4 = networkConfigPanel.getIpv4Value();
		String port = networkConfigPanel.getPortValue();
		String pass = loginConfigPanel.getPasswordValue();
		String path = storageConfigPanel.getStoragePath();
		return String.format("IPV4=%s\nPORT=%s\nPASSWORD=%s\nROOT_DIRECTORY=%s", ipv4, port, pass, path);
	}

}
