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