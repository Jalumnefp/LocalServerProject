package es.jfp.LocalServerProject.ui.config;

import java.util.regex.Matcher;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import es.jfp.LocalServerProject.utils.FormatManager;

public class ConfigSplitPanel extends JSplitPane {
	
	private final FormatManager formatManager;
	private final JList<String> optionsList;
	private final WelcomeConfigPanel welcomeConfigPanel;
	private final NetworkConfigPanel networkConfigPanel;
	private final LoginConfigPanel loginConfigPanel;
	private final StorageConfigPanel storageConfigPanel;
	
	
	public ConfigSplitPanel() {
		
		formatManager = FormatManager.getInstance();
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
		reloadFrame();
	}
	
	private void reloadFrame() {
		revalidate();
		repaint();
	}
	
	private void setUpOptionsList() {
		optionsList.setValueIsAdjusting(true);
		optionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		optionsList.setModel(new AbstractListModel() {
			String[] values = new String[] {"Inicio", "Red", "Autenticación", "Almacentamiento", "Guía"};
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
	}
	
	public String getFormatedConfigData() {
		String formatedData = null;
		String ipv4 = networkConfigPanel.getIpv4Value();
		String port = networkConfigPanel.getPortValue();
		String pass = loginConfigPanel.getPasswordValue();
		String path = storageConfigPanel.getStoragePath();
		if (testConfigData(ipv4, port, pass, path)) {			
			formatedData = String.format("IPV4=%s\nPORT=%s\nPASSWORD=%s\nROOT_DIRECTORY=%s", ipv4, port, pass, path);
		}
		return formatedData;
	}
	
	private boolean testConfigData(String ipv4, String port, String pass, String path) {
		boolean ipv4Matches = formatManager.validateIpv4Format().matcher(ipv4).matches();
		boolean portMatches = formatManager.validatePortFormat().matcher(port).matches();
		boolean passMatches = formatManager.validatePasswordFormat().matcher(pass).matches();
		
		networkConfigPanel.setIpv4FormatErrorVisible(!ipv4Matches);
		networkConfigPanel.setPortFormatErrorVisible(!portMatches);
		loginConfigPanel.setPasswordFormatErrorVisible(!passMatches);
		
		storageConfigPanel.setStorageErrorLabelVisible(path == null);
		
		
		
		if (ipv4Matches || portMatches || passMatches) {
			reloadFrame();
		}
		return ipv4Matches && portMatches && passMatches;
	}
	


}
