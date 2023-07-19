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
