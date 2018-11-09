package Server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ClientHandler extends Thread {
    
	private ConnectionHandler connectionHandler;

	private Socket conn;
	
	private Boolean isActive = true;
	
	private String username,userInput;
	
	private DataInputStream in;
	
	private PrintStream out;
	
	private String userListResponse = "";
	
	public ClientHandler(ConnectionHandler cHandler,Socket conn) {
		try{
	        this.connectionHandler = cHandler;
	        this.conn = conn;
	        super.start();
	        //get socket writing and reading streams
	        in = new DataInputStream(conn.getInputStream());
	        out = new PrintStream(conn.getOutputStream());
		}catch(Exception e){
			e.printStackTrace();
		}
    }
	
	public void run(){
    	while(isActive){
    		try{
               if(username == null){
	            	out.println("Type your Username:");
	                while((username = in.readLine()) != null){
	                	if(connectionHandler.getClients().containsKey(username)){
	                		System.out.println("You cannot chose this username");
	                	}
	                	else if(!username.equals("")){
	                		System.out.println("Username accepted");
	                		System.out.println(username);
	                		connectionHandler.addClient(username,this); 
	                		break;
	                	}else{
	                		System.out.println("No Username chosen.");
	                	}
	                }
               }
            
                while((userInput = in.readLine()) != null && !userInput.equals(".")){
                	if(userInput.equals("")){
                		System.out.println("The Commands");
                		break;
                	}else if(userInput.equals("/M")){
                		if(connectionHandler.getClients().size() > 1){
	                		String message,receiver;
	                		while((receiver = in.readLine()) != null){
	                			System.out.println("SENDER / " + receiver);
	                			while((message = in.readLine()) != null){
	    							connectionHandler.sendMessage(this,receiver,username + ": " + message);
	    							break;
	    						}
								break;
							}
                		}else{
                			out.println("/M");
                			out.println("[NOBODY IS HERE]");
                		}
                		break;
                	}else if(userInput.equals("/EXIT")){
                		try{
	                		connectionHandler.removeClient(username);
	    					conn.close();
	    					isActive = false;
                		}catch(Exception e){
                			e.printStackTrace();
                		}
                	}
                }
    		}catch(SocketException se){
    			try {
    				se.printStackTrace();
    				connectionHandler.removeClient(username);
					conn.close();
					isActive = false;
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}catch(Exception e){
    			System.out.println(e);
    		}
    	}
    }
	
	public void sendUserList(ArrayList<String> ul){
		out.println("/GAU");
		for (int i = 0; i < ul.size(); i++) {
			userListResponse += (ul.get(i) + ":") ;
		}
		out.println(userListResponse + "[END]");
		userListResponse = "";
	}
	
	public void sendMessage(String msg){
		
			out.println("/M");
			out.println(msg);
		
	}
	
	public void terminate(){
    	isActive = false;
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
}
