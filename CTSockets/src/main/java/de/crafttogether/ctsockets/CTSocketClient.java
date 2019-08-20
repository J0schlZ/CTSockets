package de.crafttogether.ctsockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.bukkit.Bukkit;
import org.json.JSONException;
import org.json.JSONObject;

import de.crafttogether.CTSockets;
import de.crafttogether.ctsockets.events.ServerConnectedEvent;
import de.crafttogether.ctsockets.events.ServerDisconnectedEvent;

public class CTSocketClient implements Runnable {
	private String clientName;
	private String host;
	private int port;
	private int connectionAttempts;
	private boolean shutdown;
	private boolean isConnected;
	private boolean isRegistered;
	private boolean whitelisted;
	
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;

	public CTSocketClient(String host, int port, String clientName) {
		this.clientName = String.valueOf(clientName);
		this.host = host;
		this.port = port;
	    this.isConnected = false;
	    this.isRegistered = false;
	    this.whitelisted = true;
		this.connectionAttempts = 0;
	}
	
	@Override
	public void run() {
		shutdown = false;
		isRegistered = false;
		
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
	    
		isConnected = true;
	    register(clientName);
	    	
		try {
			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
				if (inputLine.strip().length() < 1)
					continue;
				
				JSONObject packet = null;
				try {
				     packet = new JSONObject(inputLine);
				}catch (JSONException e){
				     e.printStackTrace();
				}
				
				if (packet != null && packet.has("error")) {
					String err = packet.getString("error");
					
					if (err.equalsIgnoreCase("NOT_WHITELISTED")) {
						System.out.println("[CTSockets][ERROR]: IP-Adress not whitelisted");
						whitelisted = false;
					}
					continue;
				}
				
				if (packet != null && packet.has("success")) {
					String msg = packet.getString("success");
					
					if(msg.equalsIgnoreCase("WELCOME")) {
						System.out.println("[CTSockets][INFO]: Connection successful!");
						isRegistered = true;
						connectionAttempts = 0;
						
						// TODO: Test
						Bukkit.getScheduler().runTaskLaterAsynchronously(CTSockets.getInstance(), new Runnable() {
							public void run() {
								CTSockets.getInstance().broadcastMessage("hallooooo TEST");
							}
						}, 3*20L);
					}
					continue;
				}
				
				if (packet != null && packet.has("server_connected")) {
					final String srvName = packet.getString("server_connected");					
					Bukkit.getScheduler().runTask(CTSockets.getInstance(), new Runnable() {
						@Override
						public void run() {
							ServerConnectedEvent event = new ServerConnectedEvent(srvName);
							Bukkit.getPluginManager().callEvent(event);
						}
					});
					continue;
				}
				
				if (packet != null && packet.has("server_disconnected")) {
					final String srvName = packet.getString("server_disconnected");
					System.out.println("[CTSockets][INFO]: #ServerDisconnectedEvent (" + srvName + ")");
					Bukkit.getScheduler().runTask(CTSockets.getInstance(), new Runnable() {
						@Override
						public void run() {
							ServerDisconnectedEvent event = new ServerDisconnectedEvent(srvName);
							Bukkit.getPluginManager().callEvent(event);
						}
					});
					continue;
				}
				
				if (packet == null || !packet.has("message") || !packet.has("sender"))
					System.out.print("[CTSockets][WARNING]: INVALID PACKET (Received from 'proxy'");
				
				System.out.println("[CTSockets][INFO]: Received from '" + clientName + "' -> PACKET[");
				System.out.println("Sender: " + packet.getString("sender"));
				System.out.println("Message: " + packet.getString("message") + "]");
			}
		} catch (Exception e) {
			if (!e.getMessage().equalsIgnoreCase("socket closed") && !e.getMessage().equalsIgnoreCase("connection reset"))
				e.printStackTrace();
		}
		
		if (isConnected & !shutdown)
			System.out.println("[CTSockets][INFO]: lost connection to proxy");

		isConnected = false;
		retryConnect();
	}
	
	public void connect() {
		System.out.println("[CTSockets][INFO]: Connecting to " + host + ":" + port + "...");
		CTSockets.getInstance().getServer().getScheduler().runTaskAsynchronously(CTSockets.getInstance(), this);
	}
	
	private void retryConnect() {
		if (shutdown || isConnected || !whitelisted)
			return;
		
		final CTSocketClient client = this;
		int delay = 3;
		
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
		this.shutdown = true;
		
		writer.flush();
	    writer.close();
	    
		try {
			reader.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendPacket(JSONObject packet) {
		String strPacket = packet.toString();
		
		// TODO: Exception?
		if (strPacket == null)
			return;
		
		writer.println(strPacket + "\r\n");
	}
	
	private void register(String clientName) {
		JSONObject packet = new JSONObject();
		packet.put("register", clientName);
		sendPacket(packet);
	}
	
	private void sendMessage(String sender, String target, String message) {
		JSONObject packet = new JSONObject();
		packet.put("sender", sender);
		packet.put("target", target);
		packet.put("message", message);
		sendPacket(packet);
	}
	
	public void sendMessage(String message) {
		sendMessage(clientName, "#proxy", message);
	}
	
	public void sendMessage(String message, String target) {
		sendMessage(clientName, target, message);
	}
	
	public void broadcast(String message) {
		sendMessage("#all", clientName, message);
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public boolean isRegistered() {
		return isRegistered;
	}
}