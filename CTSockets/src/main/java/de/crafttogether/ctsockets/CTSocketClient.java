package de.crafttogether.ctsockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.bukkit.Bukkit;

import de.crafttogether.CTSockets;

public class CTSocketClient implements Runnable {
	private String clientName;
	private String host;
	private int port;
	private int connectionAttempts;
	private boolean isConnected;
	private boolean whitelisted;
	
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;

	public CTSocketClient(String host, int port, String clientName) {
		this.clientName = String.valueOf(clientName);
		this.host = host;
		this.port = port;
		
	    this.isConnected = false;
	    this.whitelisted = true;
		this.connectionAttempts = 0;
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
			
			System.out.println("[CTSockets][ERROR]: Could not connect to " + host + ":" + port + " (" + message + ")");
			
			if (message == e.getMessage())
				e.printStackTrace();
		}
	    
	    if (socket == null || !socket.isConnected() && !socket.isClosed()) {
	    	retryConnect();
	    	return;
	    }
	    	    
	    sendMessage("CLIENT_NAME=" + clientName, "bukkit");
	    	
		try {
			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
				if (inputLine.equalsIgnoreCase("WELCOME")) {
					System.out.println("[CTSockets][INFO]: Connection successful!");
					this.connectionAttempts = 0;
					isConnected = true;
					continue;
				}
				if (inputLine.equalsIgnoreCase("NOT_WHITELISTED")) {
					System.out.println("[CTSockets][ERROR]: IP-Adress not whitelisted");
					whitelisted = false;
					continue;
				}
				
				System.out.println("[CTSockets][INFO]: (Received from '" + clientName + "') -> " + inputLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (isConnected)
			System.out.println("[CTSockets][INFO]: lost connection to proxy");

		isConnected = false;
		retryConnect();
	}
	
	public void connect() {
		System.out.println("[CTSockets][INFO]: Connecting to " + host + ":" + port + "...");
		CTSockets.getInstance().getServer().getScheduler().runTaskAsynchronously(CTSockets.getInstance(), this);
	}
	
	private void retryConnect() {
		final CTSocketClient client = this;
		int delay = 3;
		
		if (isConnected || !whitelisted)
			return;
		
		connectionAttempts++;
		
		if (connectionAttempts > 10) delay = 5;
		if (connectionAttempts > 15) delay = 10;
		if (connectionAttempts > 21) delay = 15;
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(CTSockets.getInstance(), new Runnable() {
			@Override
			public void run() {
				System.out.println("[CTSockets][INFO]: Try to reconnect");
				client.connect();
			}
		}, delay * 20L);
		
		System.out.println("[CTSockets][INFO]: Try to reconnect to " + host + ":" + port + " in " + delay + " Seconds");
	}

	public void close() {
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