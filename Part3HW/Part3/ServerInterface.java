package Module4.Part3;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import java.awt.Color;
import javax.swing.JButton;

public class ServerInterface extends JFrame {

	private JPanel contentPane;
	static JTextArea logsTextArea,usersTextArea;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		
		ServerInterface frame = new ServerInterface();
		frame.setVisible(true);
		ServerInterface.appendLog("Starting Server");
        Server server = new Server();
        int port = 3001;
        try {
        	server.start(port);
        } catch (Exception e) {
        	System.out.println(e);
        	ServerInterface.appendLog("Exception while stating Server : "+e);
        }
        
        ServerInterface.appendLog("Server Stopped");
	}

	/**
	 * Create the frame.
	 */
	public ServerInterface() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 388);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(128, 128, 128));
		panel.setBounds(0, 0, 584, 349);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Server-Side");
		lblNewLabel.setForeground(new Color(128, 255, 0));
		lblNewLabel.setFont(new Font("Times New Roman", Font.PLAIN, 15));
		lblNewLabel.setBounds(243, 11, 96, 27);
		panel.add(lblNewLabel);
		
		JScrollPane logsScrollPane = new JScrollPane();
        logsScrollPane.setBounds(273, 85, 301, 239);
        panel.add(logsScrollPane);
		
		logsTextArea = new JTextArea();
		logsTextArea.setBounds(273, 85, 301, 239);
		//panel.add(logsTextArea);
		logsScrollPane.setViewportView(logsTextArea);
		
		JLabel lblNewLabel_1 = new JLabel("Current Users");
		lblNewLabel_1.setBounds(95, 62, 96, 14);
		panel.add(lblNewLabel_1);
		
		JScrollPane userScrollPane = new JScrollPane();
		userScrollPane.setBounds(52, 85, 197, 239);
		panel.add(userScrollPane);
		
		usersTextArea = new JTextArea();
		usersTextArea.setBounds(52, 85, 197, 239);
		//panel.add(usersTextArea);
		userScrollPane.setViewportView(usersTextArea);
		
		JLabel lblNewLabel_1_1 = new JLabel("Chat Logs");
		lblNewLabel_1_1.setBounds(388, 60, 72, 14);
		panel.add(lblNewLabel_1_1);
		
		
		
	}
	
	public static void appendLog(String message) {
        logsTextArea.append(message + "\n");
        logsTextArea.setCaretPosition(logsTextArea.getDocument().getLength());
    }
	
	public static void appendClient() {
		
		SwingUtilities.invokeLater(() -> {
        	usersTextArea.setText("");
            for (ServerThread client : Server.clients) {
                usersTextArea.append("Client[" + client.getId() + "]\n");
            }
        });
    }
	
	
}
