package Server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ConnectionHandler extends Thread {
	
	private HashMap<String,ClientHandler> clients = new HashMap<String,ClientHandler>();  //Username,Client
	
	private ArrayList<String> userList = new ArrayList<String>();
	
	private Boolean isActive = true;
	
	private Boolean newUser = true;
	
	private ServerSocket s;
	private String host = "Admin-PC";
	private int portNumber;
	private PrintWriter s_out;
	private BufferedReader s_in;
	private String serverName;
	private Socket conn;
	
	public ConnectionHandler(){
		portNumber = 5000;
		serverName = "Test";
		super.start();
	}
	
	public ConnectionHandler(String serverName,int portNumber){
		this.portNumber = portNumber;
		this.serverName = serverName;
		super.start();
	}

    public void run(){
    	while(isActive){
    		try{
    			
    			s = null;
    			conn = null;
    	        
    	        try{
    	            //1. creating a server socket - 1st parameter is port number and 2nd is the backlog
    	        	InetAddress addr = InetAddress.getByName("Admin-PC");       
    	        	
    	        	s = new ServerSocket(portNumber , 10, addr);
    	            s.setReuseAddress(true);
    	            System.out.println("InetAddress : " + s.getInetAddress());
    	             
    	            while(true){
    	            	if(newUser){
    	            		
    	            		newUser = false;
    	            	}else{
    	                //get the connection socket
    	                conn = s.accept(); 
    	                
    	                System.out.println("Connection received from " + conn.getInetAddress().getHostName() + " : " + conn.getPort());
    	                
    	                //create new thread to handle client
    	                new ClientHandler(this,conn);
    	            	}
    	            }
    	        }
    	        catch(IOException e){
    	            System.err.println("IOException");
    	        }
    	        //2. close the connections and stream
    	        try{
    	            s.close();
    	        }catch(IOException ioException){
    	            System.err.println("Unable to close. IOexception");
    	        }
    		}catch(Exception e){
    			System.out.println(e);
    		}
    	}
    }
    
    public void test(PrintStream output){
    	output.println("TEST");
    }
    
    public void sendMessage(ClientHandler sender,String clientRCVR,String message) throws IOException{
    	OutputStream outstream = conn.getOutputStream(); 
    	PrintWriter out = new PrintWriter(outstream);
    	if(clients.size() > 1){
	    	if(clientRCVR.equals("GLOBAL")){
	    		message = "[GLOBAL] " + message;
	    		for (Iterator it = clients.keySet().iterator(); it.hasNext(); ){
	        	    Object key = it.next();
	        	    ClientHandler client = clients.get(key);
	        	    client.sendMessage(message);
	        	}
	    	}else{
		    	try{
		    		System.out.println("Contain PLZ: " + clients.containsKey(clientRCVR));
		    		if(clients.containsKey(clientRCVR)){
		    			message = "[PRIVATE] " + message;
			    		clients.get(clientRCVR).sendMessage(message);
		    		}else{
			    		message = "[MESSAGE COULD NOT BE SENDED]";
			    		sender.sendMessage(message);
		    		}
	    		}
		    	catch(Exception e){
		    		e.printStackTrace();
		    	}
	    	}
    	}
	    else{
    		message = "[MESSAGE COULD NOT BE SENDED]";
    		sender.sendMessage(message);
	    }
    }
    
    public synchronized void addClient(String username, ClientHandler ch){
    	clients.put(username,ch);
    	informAllClientsUserlist();
    }
    
    public synchronized void removeClient(String username){
    	clients.remove(username);
    	informAllClientsUserlist();
    }
    
    public void informAllClientsUserlist(){
    	fillUserList();
    	for (Iterator it = clients.keySet().iterator(); it.hasNext(); ){
    	    Object key = it.next();
    	    ClientHandler client = clients.get(key);
    	    client.sendUserList(userList);
    	}
    }
    
    private void fillUserList(){
    	userList.clear();
    	for (Iterator it = clients.keySet().iterator(); it.hasNext(); ){
    	    Object key = it.next();
    	    ClientHandler client = clients.get(key);
    	    userList.add(client.getUsername());
    	}
    }
    
    public void terminate(){
    	isActive = false;
    }

	public synchronized HashMap<String, ClientHandler> getClients() {
		return clients;
	}

	public synchronized void setClients(HashMap<String, ClientHandler> clients) {
		this.clients = clients;
	}
    
    
}
