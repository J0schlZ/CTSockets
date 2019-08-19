package de.crafttogether.ctsockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
		this.clientName = clientName;
		this.host = host;
		this.port = port;
	    this.read = true;
	    this.isConnected = false;
	}
	
	@Override
	public void run() {
	    try {
	    	this.socket = new Socket(host, port);
			this.writer = new PrintWriter(this.socket.getOutputStream(), true);
			this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		} catch (UnknownHostException e) {
			System.out.println("[CTSocketsBungee]: Connection failed!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("[CTSocketsBungee]: Connection failed!");
			e.printStackTrace();
		}

	    System.out.println("[CTSocketsBungee]: Connection established");
	    
	    this.isConnected = true;
	    this.read = true;
	    
	    sendMessage("Hello Server, i'm " + this.clientName, "bukkit");
	    
		while (this.read) {
			try {
				String inputLine;
			
				while ((inputLine = this.reader.readLine()) != null) {
					System.out.println("[CTSocketsBungee]: (Received from proxy) -> " + inputLine);
				}
		
			} catch (SocketException e) {
				e.printStackTrace();
				System.out.println("[CTSocketsBungee]: lost connection to proxy");
				this.isConnected = false;
				this.read = false;
				this.reconnect();
			
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("[CTSocketsBungee]: lost connection to proxy");
				this.isConnected = false;
				this.read = false;
				this.reconnect();
			
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		} 
	}
	
	public void connect() {
		System.out.println("[CTSocketsBungee]: Connecting to " + this.host + "...");
		CTSockets.getInstance().getServer().getScheduler().runTaskAsynchronously(CTSockets.getInstance(), this);
	}
	
	public void reconnect() {
		// TODO Auto-generated method stub
		
	}

	public void close() {
		this.read = false;
		this.writer.flush();
	    this.writer.close();
	    
		try {
			this.reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message, String sender) {
	    this.writer.println(String.valueOf(message) + "\r\n");
	}
	
	public boolean isConnected() {
		return this.isConnected;
	}
}
