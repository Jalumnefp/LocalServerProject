package es.jfp.LocalServerProject.ui.config;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import es.jfp.LocalServerProject.server.ServerSetup;
import es.jfp.LocalServerProject.utils.FileManager;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JToolBar;
import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JTabbedPane;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

public class ConfigFrame extends JFrame {
		
	private JPanel contentPane;
	private ConfigSplitPanel splitPane;
	private final ButtonsPanel buttonsPanel;
	
	public ConfigFrame() {
		
		contentPane = new JPanel();
		splitPane = new ConfigSplitPanel();
		buttonsPanel = new ButtonsPanel();
		
		setTitle("Configuration Window");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 650, 400);
		setContentPane(contentPane);
		
		setUpContentPanel();
				
	}
	
	private void setUpContentPanel() {
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.add(splitPane);
		contentPane.add(buttonsPanel, BorderLayout.SOUTH);
	}
	
	public String getFormatedConfData() {
		return splitPane.getFormatedConfigData();
	}
	
}