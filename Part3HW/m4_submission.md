package Module4.Part3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Server {
    int port = 3001;
    // connected clients
    public static List<ServerThread> clients = new ArrayList<ServerThread>();
    // mute lists
    public static HashMap<Long, ArrayList<Long>> muteList=new HashMap<>();
    
    void start(int port) {
        this.port = port;
        
        // server listening
        try (ServerSocket serverSocket = new ServerSocket(port);) {
            Socket incoming_client = null;
            System.out.println("Server is listening on port " + port);
            MuteListFileHandler.readMuteListFromFile();
            ServerInterface.appendLog("This is Number Guessing Game ");
            do {
            	ServerInterface.appendLog("waiting for next client");

                if (incoming_client != null) {
                	
                	
                    ServerThread sClient = new ServerThread(incoming_client, this);
                    ServerInterface.appendLog("Client connected [ID=+"+sClient.getId()+"+]");
                    clients.add(sClient);
                    
                    ServerInterface.appendClient();
                    sClient.start();
                    incoming_client = null;
                    
                   
                    
                    

                }
            } while ((incoming_client = serverSocket.accept()) != null);
        } catch (IOException e) {
        	ServerInterface.appendLog("Error accepting connection");
            e.printStackTrace();
        } finally {
        	ServerInterface.appendLog("closing server socket");
        }
    }

  
    
    protected synchronized void disconnect(ServerThread client) {
        long id = client.getId();
        client.disconnect();
        broadcast("Disconnected", id);
        for (ServerThread c : clients) {
            sendClientList(c);
        }
    }

    protected synchronized void broadcast(String message, long id) {
        if (processCommand(message, id)) {
        	
            return;
        }
        
        for (ServerThread client : clients) {
            sendClientList(client);
        }
        
        // let's temporarily use the thread id as the client identifier to
        // show in all client's chat. This isn't good practice since it's subject to
        // change as clients connect/disconnect
        message = String.format("User[%d]: %s", id, message);
        // end temp identifier

        Iterator<ServerThread> it = clients.iterator();
        while (it.hasNext()) {
            ServerThread client = it.next();
            boolean wasSuccessful = client.send(message);
            if (!wasSuccessful) {
            	ServerInterface.appendLog("Removing disconnected client["+client.getId()+"] from list");
                it.remove();
                ServerInterface.appendClient();
                broadcast("Disconnected", id);
            }
        }
        
        
        
    }

    private boolean processCommand(String message, long clientId) {
    	ServerInterface.appendLog("Checking command: " + message);
       
    	
    	
    	if (message.equalsIgnoreCase("disconnect")) {
            Iterator<ServerThread> it = clients.iterator();
            while (it.hasNext()) {
                ServerThread client = it.next();
                if (client.getId() == clientId) {
                    it.remove();
                    disconnect(client);
                    ServerInterface.appendClient();
                    break;
                }
            }
            return true;
        }
        return false;
    }

    
    public static void sendClientList(ServerThread client) {
        List<Long> clientIds = new ArrayList<>();
        for (ServerThread c : clients) {
            clientIds.add(c.getId());
        }
        client.send("ClientList:" + clientIds.toString());
    }
    
    public boolean muteClientWithId(long clientId, long id) {
    	try {
       	 	 if (!Server.muteList.containsKey(clientId)) 
       		 	Server.muteList.put(clientId, new ArrayList<>());
       		 
       	     Server.muteList.get(clientId).add(id);
             
       	 
       	     // Notify the muted client that they have been muted
       		 for (ServerThread client : Server.clients) {
       	         
       			 if(client.getId()==id) {
       	              	client.send("You have been muted by Client[" + clientId+"]");
       	              	MuteListFileHandler.writeMuteListToFile(Server.muteList);
       	              	return true;
       	            }
       	      }
       		 
             
       	
       } catch (Exception e) {
    	   ServerInterface.appendLog("Error muting the client: " + e.getMessage());
       }
		return false;
    }
    
    public boolean unmuteClientWithId(long clientId, long id) {
        try {
            if (Server.muteList.containsKey(clientId)) {
                ArrayList<Long> mutedClients = Server.muteList.get(clientId);
                if (mutedClients.contains(id)) {
                    mutedClients.remove(id);

                    // Notify the unmuted client that they have been unmuted
                    for (ServerThread client : Server.clients) {
                        if (client.getId() == id) {
                            client.send("You have been unmuted by Client[" + clientId + "]");
                            MuteListFileHandler.writeMuteListToFile(Server.muteList);
                            return true;
                        }
                    }

                    // If the unmuted client is not found in the Server.clients list
                    ServerInterface.appendLog("Client with ID[" + id + "] not found to unmute.");
                    return false;
                } else {
                    ServerInterface.appendLog("Client with ID[" + id + "] was not muted by Client[" + clientId + "].");
                    return false;
                }
            } else {
                ServerInterface.appendLog("Client with ID[" + clientId + "] is not muting any clients.");
                return false;
            }
        } catch (Exception e) {
            ServerInterface.appendLog("Error unmuting the client: " + e.getMessage());
            return false;
        }
    }

    
    /* if(!muteList.containsKey(sClient.getId()))
                    	muteList.put(sClient.getId(), new ArrayList<Long>());*/
   
}

package Module4.Part3;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;

/**
 * A server-side representation of a single client
 */
public class ServerThread extends Thread {
    private Socket client;
    private boolean isRunning = false;
    private ObjectOutputStream out;// exposed here for send()
    private Server server;// ref to our server so we can call methods on it
    // more easily

    private void info(String message) {
    	ServerInterface.appendLog(String.format("Thread[%s]: %s", getId(), message));
    }

//    void sendClientId() {
//        try {
//            out.writeObject("ID:" + getId()); // Sending the client's ID to the client
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    
    public ServerThread(Socket myClient, Server server) {
        // info("Thread created");
        info("Choose any number between 1 to 10 :");
        // get communication channels to single client
        this.client = myClient;
        this.server = server;

    }

    public void disconnect() {
        info("Thread being disconnected by server");
        isRunning = false;
        ServerInterface.appendClient();
        cleanup();
    }

    public boolean send(String message) {
        // added a boolean so we can see if the send was successful
        try {
            out.writeObject(message);
            return true;
        } catch (IOException e) {
            info("Error sending message to client (most likely disconnected)");
            // comment this out to inspect the stack trace
            // e.printStackTrace();
            cleanup();
            return false;
        }
    }

    @Override
    public void run() {
        info("Thread starting");
        try (ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());) {
            this.out = out;
            isRunning = true;
            for (ServerThread client : Server.clients) {
                Server.sendClientList(client);
            }
            send("ID:" + getId());
            String fromClient;
            while (isRunning && // flag to let us easily control the loop
                    (fromClient = (String) in.readObject()) != null // reads an object from inputStream (null would
                                                                    // likely mean a disconnect)
            ) {

            	if (fromClient.startsWith("Muted-")) {
                    // Extract muterId and mutedId from the message
                    String[] parts = fromClient.split("-");
                    if (parts.length >= 3) {
                        long muterId = Long.parseLong(parts[1]);
                        long mutedId = Long.parseLong(parts[2]);
                        if(server.muteClientWithId(muterId, mutedId)) {
                        	send("You have muted client with id: "+mutedId);
                        }else
                        	send("Error while muting client with id: "+mutedId);
                        server.broadcast("Client["+muterId+"] has muted Client["+mutedId+"]", this.getId());
                        continue;
                    }
            	}
            	
            	if (fromClient.startsWith("Unmuted-")) {
                    // Extract muterId and mutedId from the message
                    String[] parts = fromClient.split("-");
                    if (parts.length >= 3) {
                        long unmuterId = Long.parseLong(parts[1]);
                        long unmutedId = Long.parseLong(parts[2]);
                        if(server.unmuteClientWithId(unmuterId, unmutedId)) {
                        	send("You have unmuted client with id: "+unmutedId);
                        }else
                        	send("Error while unmuting client with id: "+unmutedId);
                        server.broadcast("Client["+unmuterId+"] has unmuted Client["+unmutedId+"]", this.getId());
                        continue;
                    }
            	}
            	
            	int fromClientValue = Integer.parseInt(fromClient);
                // info("Received from client: " + fromClient);
                if (fromClientValue <= 10 && fromClientValue >= 1)
                    info("Congratulation, you guess the number");
                else
                    info("You did't guess the number, try again");
                server.broadcast(fromClient, this.getId());
            } // close while loop
        } catch (Exception e) {
            // happens when client disconnects
            //e.printStackTrace();
            info("Client disconnected");
            
            Iterator<ServerThread> iterator = Server.clients.iterator();
            while (iterator.hasNext()) {
                ServerThread client = iterator.next();
                if (client.getId() == getId()) {
                    iterator.remove();
                    break;
                }
            }
            
            ServerInterface.appendClient();
        } finally {
            isRunning = false;
            info("Exited thread loop. Cleaning up connection");
            cleanup();
            ServerInterface.appendClient();
        }
    }

    private void cleanup() {
        info("Thread cleanup() start");
        
        try {
            client.close();
            ServerInterface.appendClient();
        } catch (IOException e) {
            info("Client already closed");
        }
        info("Thread cleanup() complete");
        ServerInterface.appendClient();
    }
}

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

package Module4.Part3;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

    Socket server = null;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    final String ipAddressPattern = "connect\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{3,5})";
    final String localhostPattern = "connect\\s+(localhost:\\d{3,5})";
    boolean isRunning = false;
    private Thread inputThread;
    private Thread fromServerThread;
    static long clientId=-1;
    
    public Client() {
        System.out.println("");
    }

    public boolean isConnected() {
        if (server == null) {
            return false;
        }
        // https://stackoverflow.com/a/10241044
        // Note: these check the client's end of the socket connect; therefore they
        // don't really help determine
        // if the server had a problem
        return server.isConnected() && !server.isClosed() && !server.isInputShutdown() && !server.isOutputShutdown();

    }

    /**
     * Takes an ip address and a port to attempt a socket connection to a server.
     * 
     * @param address
     * @param port
     * @return true if connection was successful
     */
    private boolean connect(String address, int port) {
        try {
            server = new Socket(address, port);
            // channel to send to server
            out = new ObjectOutputStream(server.getOutputStream());
            // channel to listen to server
            in = new ObjectInputStream(server.getInputStream());
            ClientInterface.appendLog("Client connected");
            listenForServerMessage();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isConnected();
    }

    /**
     * <p>
     * Check if the string contains the <i>connect</i> command
     * followed by an ip address and port or localhost and port.
     * </p>
     * <p>
     * Example format: 123.123.123:3000
     * </p>
     * <p>
     * Example format: localhost:3000
     * </p>
     * https://www.w3schools.com/java/java_regex.asp
     * 
     * @param text
     * @return
     */
    private boolean isConnection(String text) {
        // https://www.w3schools.com/java/java_regex.asp
        return text.matches(ipAddressPattern)
                || text.matches(localhostPattern);
    }

    private boolean isQuit(String text) {
        return text.equalsIgnoreCase("quit");
    }

    /**
     * Controller for handling various text commands.
     * <p>
     * Add more here as needed
     * </p>
     * 
     * @param text
     * @return true if a text was a command or triggered a command
     */
    private boolean processCommand(String text) {
        if (isConnection(text)) {
            // replaces multiple spaces with single space
            // splits on the space after connect (gives us host and port)
            // splits on : to get host as index 0 and port as index 1
            String[] parts = text.trim().replaceAll(" +", " ").split(" ")[1].split(":");
            connect(parts[0].trim(), Integer.parseInt(parts[1].trim()));
            return true;
        } else if (isQuit(text)) {
            isRunning = false;
            return true;
        }
        return false;
    }

    
    // Flag to control the input thread's execution
    private boolean isInputThreadRunning = false;
    // Object to synchronize the input thread and the "Send" button click
    public static Object inputLock = new Object();

    public void listenForKeyboard() {
    if (!isInputThreadRunning) {
    	inputThread = new Thread() {
            @Override
            public void run() {
            	ClientInterface.appendLog("Listening for input");
            	
                try  {
                    String line = "";
                    isRunning = true;
                    isInputThreadRunning = true;
                    while (isRunning) {
                    	
                    	ClientInterface.appendLog("Waiting for input");
                    	
                    	 synchronized (inputLock) {
                             inputLock.wait();
                        }
                    	 
                        try {
                        	
                            line = ClientInterface.cmdTxt.getText().trim();
                            ClientInterface.cmdTxt.setText("");
                            if ( line!= "" && !processCommand(line)) {
                                if (isConnected()) {
                                    out.writeObject(line);

                                } else {
                                	ClientInterface.appendLog("Not connected to server");
                                }
                            }
                        } catch (Exception e) {
                        	ClientInterface.appendLog("Connection dropped");
                            break;
                        }
                    }
                    ClientInterface.appendLog("Exited loop");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    close();
                    isInputThreadRunning = false;
                }
            }
        };
        inputThread.start();
    }
    }

    public void sendMuteMessage(long muterId, long mutedId) {
        try {
            if (isConnected()) {
                String message = "Muted-" + muterId + "-" + mutedId;
                out.writeObject(message);
            } else {
                ClientInterface.appendLog("Not connected to server");
            }
        } catch (IOException e) {
            ClientInterface.appendLog("Error sending muted message: " + e.getMessage());
        }
    }
    
    public void sendUnMuteMessage(long unmuterId, long unmutedId) {
        try {
            if (isConnected()) {
                String message = "Unmuted-" + unmuterId + "-" + unmutedId;
                out.writeObject(message);
            } else {
                ClientInterface.appendLog("Not connected to server");
            }
        } catch (IOException e) {
            ClientInterface.appendLog("Error sending unmute message: " + e.getMessage());
        }
    }
    
    
    private void listenForServerMessage() {
        fromServerThread = new Thread() {
            @Override
            public void run() {
                try {
                    String fromServer;

                    // while we're connected, listen for strings from server
                    while (!server.isClosed() && !server.isInputShutdown()
                            && (fromServer = (String) in.readObject().toString()) != null) {

                    	if (fromServer.startsWith("ID:")) {
                            // Extract and store the client's ID
                            String idStr = fromServer.substring("ID:".length());
                            clientId = Long.parseLong(idStr);
                            ClientInterface.appendLog("Received client ID from server: " + clientId);
                            continue;
                        }
                    	
                    	if (fromServer.startsWith("ClientList:")) {
                            // Extract and handle the client list
                            String clientListStr = fromServer.substring("ClientList:".length());
                            handleClientList(clientListStr);
                            continue;
                        }
                    	
                    	long muterId = extractMuterId(fromServer);
                    	if(muterId==clientId) {
                    		continue;
                    	}
                    	
                    	long mutedId = extractMutedId(fromServer);
                    	if(mutedId==clientId) {
                    		continue;
                    	}
                    	
                    	ClientInterface.appendLog(fromServer);
                    	
                    }
                    ClientInterface.appendLog("Loop exited");
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!server.isClosed()) {
                    	ClientInterface.appendLog("Server closed connection");
                    } else {
                    	ClientInterface.appendLog("Connection closed");
                    }
                } finally {
                    close();
                    ClientInterface.appendLog("Stopped listening to server input");
                }
            }
        };
        fromServerThread.start();// start the thread

    }

    private void handleClientList(String clientListStr) {
        String[] clientIdsStr = clientListStr.split(",");
        List<Long> clientIds = new ArrayList<>();
        for (String idStr : clientIdsStr) {
        	idStr = idStr.replaceAll("[\\[\\]]", "");
        	clientIds.add(Long.parseLong(idStr.trim()));
        }
        ClientInterface.appendClient(clientIds);

    }
    
    public void start() throws IOException {
    	listenForKeyboard();
    }

    private void close() {
        try {
            inputThread.interrupt();
        } catch (Exception e) {
        	ClientInterface.appendLog("Error interrupting input");
            e.printStackTrace();
        }
        try {
            fromServerThread.interrupt();
        } catch (Exception e) {
        	ClientInterface.appendLog("Error interrupting listener");
            e.printStackTrace();
        }
        try {
        	ClientInterface.appendLog("Closing output stream");
            out.close();
        } catch (NullPointerException ne) {
        	ClientInterface.appendLog("Server was never opened so this exception is ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
        	ClientInterface.appendLog("Closing input stream");
            in.close();
        } catch (NullPointerException ne) {
        	ClientInterface.appendLog("Server was never opened so this exception is ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
        	ClientInterface.appendLog("Closing connection");
            server.close();
            ClientInterface.appendLog("Closed socket");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException ne) {
        	ClientInterface.appendLog("Server was never opened so this exception is ok");
        }
    }

    private static long extractMuterId(String message) {
        Pattern pattern = Pattern.compile("Client\\[(\\d+)\\]");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String muterIdStr = matcher.group(1);
            return Long.parseLong(muterIdStr);
        }
        return -1; // If no muterId is found in the message
    }
    
    private static long extractMutedId(String message) {
        Pattern pattern = Pattern.compile("Client\\[(\\d+)\\] has muted Client\\[(\\d+)\\]");
        Pattern pattern2 = Pattern.compile("Client\\[(\\d+)\\] has unmuted Client\\[(\\d+)\\]");
        Matcher matcher = pattern.matcher(message);
        Matcher matcher2 = pattern.matcher(message);
        if (matcher.find() || matcher2.find()) {
            String mutedIdStr = matcher.group(2);
            return Long.parseLong(mutedIdStr);
        }
        return -1; // If no Id is found in the message
    }

}


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

package Module4.Part3;

import java.io.*;
import java.util.*;

public class MuteListFileHandler {

	private static final String MUTE_LIST_FILE_PATH = "muteList.txt";

    public static void writeMuteListToFile(HashMap<Long, ArrayList<Long>> muteList) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(MUTE_LIST_FILE_PATH))) {
            outputStream.writeObject(muteList);
            System.out.println("Mute list has been written to file: " + MUTE_LIST_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error writing mute list to file: " + e.getMessage());
        }
    }

    public static HashMap<Long, ArrayList<Long>> readMuteListFromFile() {
        HashMap<Long, ArrayList<Long>> muteList = new HashMap<>();
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(MUTE_LIST_FILE_PATH))) {
            muteList = (HashMap<Long, ArrayList<Long>>) inputStream.readObject();
            System.out.println("Mute list has been read from file: " + MUTE_LIST_FILE_PATH);
        } catch (FileNotFoundException e) {
            System.err.println("Mute list file not found. Creating a new one.");
            // If the file is not found, return an empty HashMap
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading mute list from file: " + e.getMessage());
        }
        return muteList;
    }
}




