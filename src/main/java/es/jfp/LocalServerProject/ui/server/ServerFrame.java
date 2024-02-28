package es.jfp.LocalServerProject.ui.server;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;

public class ServerFrame extends JFrame {

	private static final long serialVersionUID = 5L;
	private JPanel contentPane;

	
	/**
	 * Create the frame.
	 */
	public ServerFrame() {
		setVisible(true);
		setTitle("Main Window");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(1, 0, 0, 0));
	}

}
