package Module4.Part3;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JButton;

public class ClientInterface extends JFrame {

	private JPanel contentPane;
	static JTextField muteTxt;
	static JTextField cmdTxt;
	static JTextArea logsTextArea,usersTextArea;
	static JButton btnSend;
	
	static Client client;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
		
		 ClientInterface frame = new ClientInterface();
		 frame.setVisible(true);
		 client = new Client();
		 try {
	            // if start is private, it's valid here since this main is part of the class
	            client.start();
	     } catch (IOException e) {
	            e.printStackTrace();
	     }
		
	}

	/**
	 * Create the frame.
	 */
	public ClientInterface() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 625, 415);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBackground(Color.GRAY);
		panel.setBounds(10, 11, 584, 349);
		contentPane.add(panel);
		
		JLabel lblClientside = new JLabel("Client-Side");
		lblClientside.setForeground(new Color(128, 255, 0));
		lblClientside.setFont(new Font("Times New Roman", Font.PLAIN, 15));
		lblClientside.setBounds(252, 11, 96, 27);
		panel.add(lblClientside);
		
		JScrollPane logsScrollPane = new JScrollPane();
		logsScrollPane.setBounds(273, 68, 301, 178);
		panel.add(logsScrollPane);
		
		logsTextArea = new JTextArea();
		logsTextArea.setBounds(273, 85, 301, 239);
		//panel.add(logsTextArea);
		logsScrollPane.setViewportView(logsTextArea);
		
		JLabel lblNewLabel_1 = new JLabel("Current Users");
		lblNewLabel_1.setBounds(72, 43, 96, 14);
		panel.add(lblNewLabel_1);
		
		JScrollPane userScrollPane = new JScrollPane();
		userScrollPane.setBounds(10, 68, 235, 178);
		panel.add(userScrollPane);
		
		usersTextArea = new JTextArea();
		usersTextArea.setBounds(10, 68, 235, 178);
		//panel.add(usersTextArea);
		userScrollPane.setViewportView(usersTextArea);
		
		JLabel lblNewLabel_1_1 = new JLabel("Chat Logs");
		lblNewLabel_1_1.setBounds(396, 43, 72, 14);
		panel.add(lblNewLabel_1_1);
		
		muteTxt = new JTextField();
		muteTxt.setToolTipText("");
		muteTxt.setBounds(10, 267, 106, 20);
		panel.add(muteTxt);
		muteTxt.setColumns(10);
		
		JButton btnMute = new JButton("Mute");
		btnMute.setBounds(115, 266, 66, 23);
		panel.add(btnMute);
		
		btnMute.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        String clientToMuteIdStr = muteTxt.getText().trim();
		        if(!clientToMuteIdStr.equals(""))
		        try {
		            long clientToMuteId = Long.parseLong(clientToMuteIdStr);
		            long ownClientId = Client.clientId;

		            if (clientToMuteId != ownClientId) {
		            	client.sendMuteMessage(ownClientId,clientToMuteId);
		            	appendLog("Requested to mute Client["+clientToMuteId+"]");
		            
		            } else {
		                appendLog("You cannot mute yourself.");
		            }
		        } catch (NumberFormatException ex) {
		            // Handle the case where an invalid client ID is entered (optional step)
		            appendLog("Invalid client ID: " + clientToMuteIdStr);
		        }
		    }
		});

		
		JButton btnNewButton_1 = new JButton("Export");
		btnNewButton_1.setBounds(485, 39, 89, 23);
		panel.add(btnNewButton_1);
		
		btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveLogsToFile();
            }
        });
		
		cmdTxt = new JTextField();
		cmdTxt.setToolTipText("");
		cmdTxt.setColumns(10);
		cmdTxt.setBounds(283, 267, 199, 20);
		panel.add(cmdTxt);
		
		btnSend = new JButton("Send");
		btnSend.setBounds(492, 266, 82, 23);
		panel.add(btnSend);
		
		btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // When the "Send" button is clicked, notify the input thread to proceed
            	 synchronized (Client.inputLock) {
                     Client.inputLock.notify();
                 }
            }
        });
		
		JLabel lblNewLabel = new JLabel("Enter Client Id To mute");
		lblNewLabel.setForeground(new Color(255, 255, 255));
		lblNewLabel.setBounds(10, 252, 127, 14);
		panel.add(lblNewLabel);
		
		JLabel lblEnterCommand = new JLabel("Enter Command");
		lblEnterCommand.setForeground(Color.WHITE);
		lblEnterCommand.setBounds(283, 252, 127, 14);
		panel.add(lblEnterCommand);
		
		JButton btnUnmute = new JButton("Unmute");
		btnUnmute.setBounds(184, 266, 74, 23);
		panel.add(btnUnmute);
		
		btnUnmute.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        String clientToUnMuteIdStr = muteTxt.getText().trim();
		        if(!clientToUnMuteIdStr.equals(""))
		        try {
		            long clientToUnMuteId = Long.parseLong(clientToUnMuteIdStr);
		            long ownClientId = Client.clientId;

		            if (clientToUnMuteId != ownClientId) {
		            	client.sendUnMuteMessage(ownClientId,clientToUnMuteId);
		            	appendLog("Requested to unmute Client["+clientToUnMuteId+"]");
		            
		            } else {
		                appendLog("You cannot unmute yourself.");
		            }
		        } catch (NumberFormatException ex) {
		            // Handle the case where an invalid client ID is entered (optional step)
		            appendLog("Invalid client ID: " + clientToUnMuteIdStr);
		        }
		    }
		});

		
		
	}
	
	
	public static void appendLog(String message) {
        logsTextArea.append(message + "\n");
        logsTextArea.setCaretPosition(logsTextArea.getDocument().getLength());
    }
	
	public static void appendClient(List<Long> clients) {
		SwingUtilities.invokeLater(() -> {
        	usersTextArea.setText("");
            for (Long client : clients) {
                if(client !=Client.clientId)
                	usersTextArea.append("Client[" + client + "]\n");
                else
                	usersTextArea.append("Client[" + client + "] (YOU) \n");
            }
        });
    }
	
	private void saveLogsToFile() {
        String filePath = "chatLogs.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(logsTextArea.getText());
            appendLog("Logs saved to file: " + filePath);
        } catch (IOException ex) {
            appendLog("Error saving logs to file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
	

	
}
