package server;	
import java.util.ArrayList;	
import java.util.Iterator;	
import java.util.List;	
import java.util.logging.Level;	
import java.util.logging.Logger;	
import java.util.Random;	
public class Room implements AutoCloseable {	
    private static SocketServer server; // used to refer to accessible server functions	
    private String name;	
    private final static Logger log = Logger.getLogger(Room.class.getName());	
    // Commands	
    private final static String COMMAND_TRIGGER = "/";	
    private final static String CREATE_ROOM = "createroom";	
    private final static String JOIN_ROOM = "joinroom";	
    private final static String ROLL = "roll";	
    private final static String FLIP = "flip";
    
    // milestone 3 features
    private final static String PM = "@";
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
    
    private List < ServerThread > clients = new ArrayList < ServerThread > ();	
    
    
    protected synchronized void addClient(ServerThread client) {	
        client.setCurrentRoom(this);
        //newline
        client.loadMute();
        if (clients.indexOf(client) > -1) {	
            log.log(Level.INFO, "Attempting to add a client that already exists");	
        } else {	
            clients.add(client);	
            if (client.getClientName() != null) {	
                client.sendClearList();	
                sendConnectionStatus(client, true, "joined the room " + getName());	
                updateClientList(client);	
            }	
        }	
    }	
    private void updateClientList(ServerThread client) {	
        Iterator < ServerThread > iter = clients.iterator();	
        while (iter.hasNext()) {	
            ServerThread c = iter.next();	
            if (c != client) {	
                boolean messageSent = client.sendConnectionStatus(c.getClientName(), true, null);	
            }	
        }	
    }	
    protected synchronized void removeClient(ServerThread client) {	
        clients.remove(client);	
        if (clients.size() > 0) {	
            // sendMessage(client, "left the room");	
            sendConnectionStatus(client, false, "left the room " + getName());	
        } else {	
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
        } catch (Exception e) {	
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
    Random rand = new Random();	
    /***	
     * Helper function to process messages to trigger different functionality.	
     * 	
     * @param message The original message being sent	
     * @param client  The sender of the message (since they'll be the ones	
     *                triggering the actions)	
     */	
    private String processCommands(String message, ServerThread client) {	
        // boolean wasCommand = false;	
        String response = null;	
        try {	
            if (message.indexOf(COMMAND_TRIGGER) > -1) {	
                String[] comm = message.split(COMMAND_TRIGGER);	
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
                        break;
                        
                    case JOIN_ROOM:	
                        roomName = comm2[1];	
                        joinRoom(roomName, client);		
                        break;
                        
                    case ROLL:	
					response = "<u><b style=\"color: black;\">" + Integer.toString(rand.nextInt(6) + 1) + "</b></u>";
                        break;
                        
                    case FLIP:	
                        String[] coin = {	
                            "HEADS",	
                            "TAILS"	
                        };	
                        response = "<u><b style=\"color: red;\">" + coin[rand.nextInt(coin.length)] + "</b></u>";	
                        break;
                        
                    case MUTE:
    					String[] MUser = comm2[1].split("!");
    					String MUser1 = MUser[1];
    					// added if statement to prevent duplicate issues
    					if (!client.mutedList.contains(MUser1)) {
    						client.mutedList.add(MUser1);
    					}
    					// add a notification that the user was muted
    					muteStatus(client, MUser1);
    					client.saveMutes();
    					response = message;
    					break;
    					
    				case UNMUTE:
    					String[] UMUser = comm2[1].split("!");
    					String UMUser1 = UMUser[1];
    					// added if statement to prevent duplicate issues
    					if (client.mutedList.contains(UMUser1)) {
    						client.mutedList.remove(UMUser1);
    					}
    					// add a notification that the user was unmuted
    					unmuteStatus(client, UMUser1);
    					client.saveMutes();
    					response = message;
    					break;
    					
                    default:	
                        response = message;	
                        break;	
                        
                }	
            } else {	
                // For bolding the text
                if (message.contains("**")) {	
                    message = message.replaceAll("\\*\\*\\b", "<b>").replaceAll("\\b\\*\\*", "</b>");	
                }	
                // For italicizing the text
                if (message.contains("*")) {	
                    message = message.replaceAll("\\*\\b", "<i>").replaceAll("\\b\\*", "</i>");	
                }	
                // For underlining the text
                if (message.contains("__")) {	
                    message = message.replaceAll("\\b__", "<u>").replaceAll("__\\b", "</u>");	
                }	
                response = message;	
            }	
        } catch (Exception e) {	
            e.printStackTrace();	
        }	
        // return wasCommand;	
        return response;	
    }	
    // TODO changed from string to ServerThread	
    protected void sendConnectionStatus(ServerThread client, boolean isConnect, String message) {	
        Iterator < ServerThread > iter = clients.iterator();	
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
        String response = processCommands(message, sender);	
        if (response == null) {	
            // it was a command, don't broadcast	
            return;	
        }	
        if (sendPM(sender, message)){
            return;
        }

        message = response;	
        Iterator < ServerThread > iter = clients.iterator();	
        while (iter.hasNext()) {	
            ServerThread client = iter.next();
            //newline
            if (!client.isMuted(sender.getClientName())) {
            boolean messageSent = client.send(sender.getClientName(), message);	
            if (!messageSent) {	
                iter.remove();	
                log.log(Level.INFO, "Removed client " + client.getId());
                
            }
            }
        }	
    }	

    
    //Send a private message when user is called by "@"
    
    protected boolean sendPM(ServerThread sender, String message) {
    	boolean isPM = false;
    	String receiver = null;

    	if (message.indexOf("@") > -1) {
			String[] words = message.split(" ");
			for(String word: words){
			    if (word.charAt(0)=='@'){
			        receiver = word.substring(1);
			        isPM = true;
			        //now that the message is known to be private, we can send it to each receiver

			        Iterator<ServerThread> iter = clients.iterator();
					while (iter.hasNext()) {
						ServerThread c = iter.next();
						if (c.getClientName().equals(receiver)) {
							c.send(sender.getClientName(), message);
						}
					}
			    }
			}
			//send one message to the sender so they can see it went through
			sender.send(sender.getClientName(), message);
		}
    	//return true boolean
    	return isPM;
    }
    
    // Mute functions. Notify user.
    
    protected void muteStatus(ServerThread client, String recipient) {
		Iterator<ServerThread> iter = clients.iterator();
		while (iter.hasNext()) {
			ServerThread c = iter.next();
			if (c.getClientName().equals(recipient)) {
				c.send(client.getClientName(), "<b><i>has muted you</b></i>");
			}
		}
	}

	protected void unmuteStatus(ServerThread client, String recipient) {
		Iterator<ServerThread> iter = clients.iterator();
		while (iter.hasNext()) {
			ServerThread c = iter.next();
			if (c.getClientName().equals(recipient)) {
				c.send(client.getClientName(), "<b><i>has unmuted you</b></i>");
			}
		}
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
            Iterator < ServerThread > iter = clients.iterator();	
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