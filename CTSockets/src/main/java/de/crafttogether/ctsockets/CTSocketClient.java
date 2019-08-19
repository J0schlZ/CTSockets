package de.crafttogether.ctsockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import de.crafttogether.CTSockets;

public class CTSocketClient implements Runnable {
	private String clientName;
	private String host;
	private int port;
	private boolean read;
	private boolean isConnected;
	
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;

	public CTSocketClient(String host, int port, String clientName) {
		this.clientName = String.valueOf(clientName);
		this.host = host;
		this.port = port;
	    this.read = true;
	    this.isConnected = false;
	}
	
	@Override
	public void run() {
	    try {
	    	socket = new Socket(host, port);
			writer = new PrintWriter(socket.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			String message = e.getMessage();
			
			if (e.getMessage().contains("Connection refused"))
				message = "Connection refused";
			
			System.out.println("[CTSockets]: Could not connect to " + host + ":" + port + " (" + message + ")");
			
			if (message == e.getMessage())
				e.printStackTrace();
		}
	    
	    if (socket == null || !socket.isConnected()) {
	    	reconnect();
	    	return;
	    }

	    System.out.println("[CTSockets]: Connection established");
	    
	    isConnected = true;
	    read = true;
	    
	    sendMessage("Hello Server, i'm " + clientName, "bukkit");
	    
		while (read) {
			try {
				String inputLine;
			
				while ((inputLine = reader.readLine()) != null) {
					System.out.println("[CTSockets]: (Received from proxy) -> " + inputLine);
				}			
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("[CTSockets]: lost connection to proxy");
				isConnected = false;
				read = false;
				reconnect();
			
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		} 
	}
	
	public void connect() {
		System.out.println("[CTSockets]: Connecting to " + host + "...");
		CTSockets.getInstance().getServer().getScheduler().runTaskAsynchronously(CTSockets.getInstance(), this);
	}
	
	public void reconnect() {
		
	}

	public void close() {
		read = false;
		writer.flush();
	    writer.close();
	    
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message, String sender) {
	    writer.println(String.valueOf(message) + "\r\n");
	}
	
	public boolean isConnected() {
		return isConnected;
	}
}
