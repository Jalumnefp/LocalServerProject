package es.jfp.LocalServerProject.ui.server;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ServerFrame extends JFrame {

	private static final long serialVersionUID = 5L;
	private JPanel contentPane;
	private JTextArea textArea;

	
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
		contentPane.setLayout(new BorderLayout());

		this.textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		contentPane.add(scrollPane, BorderLayout.CENTER);

	}

	public void appendText(String txt) {
		textArea.append(txt + '\n');
	}

}
