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
