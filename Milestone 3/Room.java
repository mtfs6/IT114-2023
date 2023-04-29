package server;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

//import client.Player;

public class Room implements AutoCloseable {
    private static SocketServer server;// used to refer to accessible server functions
    private String name;
    private final static Logger log = Logger.getLogger(Room.class.getName());
    
    private List<ServerThread> clients = new ArrayList<ServerThread>();
    
    // Commands
    private final static String COMMAND_TRIGGER = "/";
    private final static String CREATE_ROOM = "createroom";
    private final static String JOIN_ROOM = "joinroom";
    private final static String FLIP = "flip";
    private final static String ROLL = "roll";
    private final static String MUTE = "mute";
    private final static String UNMUTE = "unmute";
    
    public Room(String name) {
	this.name = name;

    }

    public static void setServer(SocketServer server) {
	Room.server = server;
    }

    public String getName() {
	return name;
    }

    protected synchronized void addClient(ServerThread client) {
	client.setCurrentRoom(this);
	boolean exists = false;
	// since we updated to a different List type, we'll need to loop through to find
	// the client to check against
	Iterator<ServerThread> iter = clients.iterator();
	while (iter.hasNext()) {
	    ServerThread c = iter.next();
	    if (c == client) {
		exists = true;
		}
		break;
	}
	if (exists) {
	    log.log(Level.INFO, "Attempting to add a client that already exists");
	}
	else {
	    clients.add(client);
	    if (client.getClientName() != null) {
		client.sendClearList();
		sendConnectionStatus(client, true, "joined the room " + getName());
		updateClientList(client);
	    }
	}
    }

    private void updateClientList(ServerThread client) {
	Iterator<ServerThread> iter = clients.iterator();
	while (iter.hasNext()) {
	    ServerThread c = iter.next();
	    if (c != client) {
		client.sendConnectionStatus(c.getClientName(), true, null);
	    }
	}
    }

    protected synchronized void removeClient(ServerThread client) {
    	Iterator<ServerThread> iter = clients.iterator();
    	while (iter.hasNext()) {
    	    ServerThread c = iter.next();
    	    if (c == client) {
    		iter.remove();
    		log.log(Level.INFO, "Removed client " + c.getClientName() + " from " + getName());
    	    }
    	}
	if (clients.size() > 0) {
	    // sendMessage(client, "left the room");
	    sendConnectionStatus(client, false, "left the room " + getName());
	}
	else {
	    cleanupEmptyRoom();
	}
    }

    private void cleanupEmptyRoom() {
	// If name is null it's already been closed. And don't close the Lobby
	if (name == null || name.equalsIgnoreCase(SocketServer.LOBBY)) {
	    return;
	}
	try {
	    log.log(Level.INFO, "Closing empty room: " + name);
	    close();
	}
	catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    protected void joinRoom(String room, ServerThread client) {
	server.joinRoom(room, client);
    }

    protected void joinLobby(ServerThread client) {
	server.joinLobby(client);
    }
    
    protected void createRoom(String room, ServerThread client) {
	if (server.createNewRoom(room)) {
	    sendMessage(client, "Created a new room");
	    joinRoom(room, client);
	}
    }
    
    //flip and roll methods
    Random flipRoll = new Random();
    protected synchronized void roll(ServerThread client) {
    	int result = flipRoll.nextInt(7);
    	sendMessage(client, "<b><i><font color=purple> rolled a " + result + "</font></i></b>");
    }
     
    protected synchronized void flip(ServerThread client) {
    	int result = flipRoll.nextInt(2);
    	String message;
    	if(result == 0)
    		message = " flipped heads";
    	else 
    		message = " flipped tails";
    	sendMessage(client, "<b><i><font color=gray>" + message + "</font></i></b>");
    }
    
    /***
     * Helper function to process messages to trigger different functionality.
     * 
     * @param message The original message being sent
     * @param client  The sender of the message (since they'll be the ones
     *                triggering the actions)
     */
    
    private boolean processCommands(String message, ServerThread client) {
	boolean wasCommand = false;
	try {
		if (message.indexOf(COMMAND_TRIGGER) > -1) {
		String[] comm = message.split(COMMAND_TRIGGER, 2);
		
		log.log(Level.INFO, message);
		String part1 = comm[1];
		String[] comm2 = part1.split(" ");
		String command = comm2[0];
		if (command != null) {
		    command = command.toLowerCase();
		}
		
		String roomName;
		switch (command) {
		case CREATE_ROOM:
		    roomName = comm2[1];
		    if (server.createNewRoom(roomName)) {
			joinRoom(roomName, client);
		    }
		    wasCommand = true;
		    break;
		case JOIN_ROOM:
		    roomName = comm2[1];
		    joinRoom(roomName, client);
		    wasCommand = true;
		    break;
	    //added "flip" and "roll" as potential cases
		case FLIP:
		    flip(client);
		    wasCommand = true;
		    break;
	    case ROLL:
		    roll(client);
		    wasCommand = true;
		    break;
		  //added "mute" and "unmute" and potential cases
	    case MUTE: 
	    	String[] muted = comm2[1].split(", ");
	    	List<String> muteList = new ArrayList<String>();
	    	// can mute multiple clients separated by comma
	    	for (String user : muted) {
	    		if (!client.isMuted(user)) {
		    		client.mute(user);
		    		muteList.add(user);
		    	}
	    	}
	    	sendPrivateMessage(client, " muted you", muteList);
	   
	    	wasCommand = true;
	    	break;
	    case UNMUTE:
	    	String[] unmuted = comm2[1].split(", ");
	    	List<String> unmuteList = new ArrayList<String>();
	    	// can unmute multiple clients separated by comma
	    	for (String user : unmuted) {
	    		if (client.isMuted(user)) {
	    			client.unmute(user);
	    			unmuteList.add(user);
		    	}
	    	}
	    	sendPrivateMessage(client, " unmuted you", unmuteList);
	    	
	    	wasCommand = true;
	    	break;
		}
		} 
		// private message functionality
		// message will be sent to every user specified with an "@" 
		//ex. "@user1 @user2 This message will be sent to user1 and user2."
		else if (message.indexOf("@") > -1) {
			String command = "";
			String[] comm = message.split("@", 2);
	
			String part1 = comm[1];
			String[] comm2 = part1.split(" @");
			List<String> users = new ArrayList<String>();
				// get list of intended users 
			for (String user : comm2) {
				if(!user.equals(comm2[comm2.length-1])) {
					users.add(user.toLowerCase());
				}
				else {	// get message
					String[] pm = user.split(" ", 2);
					String last = pm[0];
					users.add(last.toLowerCase());
					command = pm[1];
				}
			}
			users.add(client.getClientName());
			sendPrivateMessage(client, "<b> /pm </b> " + command, users);
	
			wasCommand = true;
		}
		// change text functionality
		else {
			String command = message;
			//BOLD
			//makes sure there is at least one pair of "bold" symbols i.e *bold*
			if (command.matches("(.*)\\*(.+)\\*(.*)")) {
				int count = 0;
				String changeText = "";
			ArrayList<String> tags = new ArrayList<String>();
			for (int i = 0; i < command.length(); i++) {
				if (Character.toString(command.charAt(i)).equals("*")) {
					count++;
					if (count %2 != 0) {
						tags.add("<b>");
					}
					else {
						tags.add("</b>");
					}
					
				}
			}
			String [] bold = command.split("\\*");
			
			//accounts for "***" as a potential text option
			if (bold.length == 0) {
				changeText += "<b>*</b>";
						if (command.length() > 3)
					changeText += command.substring(3);
				
			}
			for (int i = 0; i < bold.length; i++) {
				
				// accounts for odd number of "*"
				//  and also two "*" in a row
				if (tags.size() == 1 && tags.get(tags.size()-1).contains("<b>") || (bold[i].equals("") && (bold[i+1].equals("")))) {
					changeText += bold[i] + "*";
					tags.remove(0);
				}
				// convet "**" pairs to "<b></b>"
				else if (tags.size() > 1 || (tags.size() == 1 && tags.get(tags.size()-1).contains("</b>")) ){
					changeText += bold[i] + tags.get(0);
					tags.remove(0);
				}
				else changeText += bold[i];
				}
	    	wasCommand = true;
	    	
	    	// makes it so that conditions stack; 
	    	// can have more than one type of function applied on the same line
	    	if (changeText != "") {
				command = changeText;
			}
	    }
			// ITALICS
			// same logic as bold
			if (command.matches("(.*)_(.+)_(.*)")) {
				int count = 0;
				String changeText = "";
				ArrayList<String> tags = new ArrayList<String>();
				for (int i = 0; i < command.length(); i++) {
					if (Character.toString(command.charAt(i)).equals("_")) {
						count++;
						if (count %2 != 0) {
							tags.add("<i>");
						}
						else {
							tags.add("</i>");
						}
					}
				}
				String [] italics = command.split("_");
				if (italics.length == 0) {
					changeText += "<i>_</i>";
							if (command.length() > 3)
						changeText += command.substring(3);
					
				}
				for (int i = 0; i < italics.length; i++) {
					if (tags.size() == 1 && tags.get(tags.size()-1).contains("<i>") || (italics[i].equals("") && (italics[i+1].equals("")))) {
						changeText += italics[i] + "_";
						tags.remove(0);
					}
					else if (tags.size() > 1 || (tags.size() == 1 && tags.get(tags.size()-1).contains("</i>")) ){ //if (i % 2 == 0) {
						changeText += italics[i] + tags.get(0);
						tags.remove(0);
					}
					else changeText += italics[i];
					}
		    	wasCommand = true;
		    	if (changeText != "") {
					command = changeText;
				}
	    }
			// UNDERLINE
			// same logic as bold
			if (command.matches("(.*)~(.+)~(.*)")) {
				int count = 0;
				String changeText = "";
				ArrayList<String> tags = new ArrayList<String>();
				for (int i = 0; i < command.length(); i++) {
					if (Character.toString(command.charAt(i)).equals("~")) {
						count++;
						if (count %2 != 0) {
							tags.add("<u>");
						}
						else {
							tags.add("</u>");
						}
						
					}
				}
				String [] underline = command.split("~");
				if (underline.length == 0) {
					changeText += "<b>~</b>";
							if (command.length() > 3)
						changeText += command.substring(3);
					
				}			
				for (int i = 0; i < underline.length; i++) {
					if (tags.size() == 1 && tags.get(tags.size()-1).contains("<u>") || (underline[i].equals("") && (underline[i+1].equals("")))) {
						changeText += underline[i] + "~";
						tags.remove(0);
					}
					else if (tags.size() > 1 || (tags.size() == 1 && tags.get(tags.size()-1).contains("</i>")) ){ //if (i % 2 == 0) {
						changeText += underline[i] + tags.get(0);
						tags.remove(0);
					}
					else changeText += underline[i];
					}
		    	wasCommand = true;
		    	if (changeText != "") {
					command = changeText;
				}
	    }
	
		 // COLOR
		 // change color by declaring a color between two pound signs
			// e.x #red# this text would be red
			// to change back to black/default type '##'
			// color will stay black if declared color does not exist 
				//e.x #null# this will stay black
			if (command.matches("(.*)#(.+)#(.*)")) {
				String changeText = "";
				String colorString = "black";;
				String [] color = command.split("#", -1);
				System.out.println(Arrays.toString(color));
				if (color.length == 0) {
					changeText += command;	
				}	
					for (int i = 0; i < color.length; i++) {
						
						if (i % 2 != 0 && (!color[i].contains(" "))){
							// accounts for odd number of #
							if (i == color.length-1) {
								changeText += "#" + color[i];
							}
							else { 
								//if text is between two pound signs declare that as color variable
								colorString = color[i];
								if (colorString.equals("")) { colorString = "black"; }
							}
						}
						// will not work if whitespace between pound signs i.e. # #
						else if (i % 2 != 0 && (color[i].contains(" "))){
							changeText += "#" + color[i] + "#";
						}
						// append message
						else { 
							changeText += "<font color="+colorString+">" + color[i] + "</font>";
							colorString="black";
						}
					}
				wasCommand = true;
				if (changeText != "") {
					command = changeText;
				}
				
			}
	   
	    	if (wasCommand == true) {
		    	sendMessage(client, command);
		}

		}
	}
	//catch (EOFException e) {
		   // ... this is fine
	//	}
	catch (Exception e) {
	    e.printStackTrace();
	}
	return wasCommand;
    }

    // TODO changed from string to ServerThread
    protected void sendConnectionStatus(ServerThread client, boolean isConnect, String message) {
	Iterator<ServerThread> iter = clients.iterator();
	while (iter.hasNext()) {
	    ServerThread c = iter.next();
	    boolean messageSent = c.sendConnectionStatus(client.getClientName(), isConnect, message);
	    if (!messageSent) {
			iter.remove();
			log.log(Level.INFO, "Removed client " + c.getId());
	    }
	}
    }

    /***
     * Takes a sender and a message and broadcasts the message to all clients in
     * this room. Client is mostly passed for command purposes but we can also use
     * it to extract other client info.
     * 
     * @param sender  The client sending the message
     * @param message The message to broadcast inside the room
     */
    protected void sendMessage(ServerThread sender, String message) {
	log.log(Level.INFO, getName() + ": Sending message to " + clients.size() + " clients");
	if (processCommands(message, sender)) {
	    // it was a command, don't broadcast
	    return;
	}
	Iterator<ServerThread> iter = clients.iterator();
	while (iter.hasNext()) {
	    ServerThread client = iter.next();
	    	// send message if sender not muted
	    if (!client.isMuted(sender.getClientName())){
	    	boolean messageSent = client.send(sender.getClientName(), message);
		    if (!messageSent) {
		    	iter.remove();
		    }
	    }
	}
    }
    
    protected void sendPrivateMessage(ServerThread sender, String message, List<String> users) {
	log.log(Level.INFO, getName() + ": Sending message to " + users.size() + " clients");
	if (processCommands(message, sender)) {
	    // it was a command, don't broadcast
	    return;
	}
	Iterator<ServerThread> iter = clients.iterator();
	while (iter.hasNext()) {
	    ServerThread client = iter.next();
	    	// send message if sender not muted
	    if(users.contains(client.getClientName().toLowerCase())) {
	    	if (!client.isMuted(sender.getClientName())){
	    		boolean messageSent = client.send(sender.getClientName(), message);
			    if (!messageSent) {
			    	iter.remove();
			    }
	    	}
	    }
	}
    }
    
    
    public List<String> getRooms(String search) {
    	return server.getRooms(search);
        }
    /***
     * Will attempt to migrate any remaining clients to the Lobby room. Will then
     * set references to null and should be eligible for garbage collection
     */
    @Override
    public void close() throws Exception {
	int clientCount = clients.size();
	if (clientCount > 0) {
	    log.log(Level.INFO, "Migrating " + clients.size() + " to Lobby");
	    Iterator<ServerThread> iter = clients.iterator();
	    Room lobby = server.getLobby();
	    while (iter.hasNext()) {
			ServerThread client = iter.next();
			lobby.addClient(client);
			iter.remove();
	    }
	    log.log(Level.INFO, "Done Migrating " + clients.size() + " to Lobby");
	}
	server.cleanupRoom(this);
	name = null;
	// should be eligible for garbage collection now
    }

}
