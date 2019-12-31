package de.crafttogether.ctsockets.bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.crafttogether.ctsockets.bukkit.events.MessageReceivedEvent;
import de.crafttogether.ctsockets.bukkit.events.ServerConnectedEvent;
import de.crafttogether.ctsockets.bukkit.events.ServerDisconnectedEvent;
import net.md_5.bungee.api.ChatColor;

/**
 * @hidden
 */

public class CTSocketClient implements Runnable {
	private String clientName;
	private String host;
	private int port;
	private int connectionAttempts;
	private boolean shutdown;
	private boolean isConnected;
	private boolean isRegistered;
	private boolean whitelisted;
	
	public ArrayList<String> server;
	public HashMap<Long, CommandSender> pingRequests;
	
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;

	public CTSocketClient(String host, int port, String clientName) {
		this.server = new ArrayList<String>();
		this.pingRequests = new HashMap<Long, CommandSender>();
		
		this.clientName = String.valueOf(clientName);
		this.host = host;
		this.port = port;
	    this.whitelisted = true;
	}
	
	@Override
	public void run() {
		shutdown = false;
		isConnected = false;
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
				
				if (packet != null && packet.has("serverlist")) {
					JSONArray serverlist = packet.getJSONArray("serverlist");
					
					if (serverlist == null) {
						System.out.println("[CTSockets][ERROR]: Unable to read serverlist");
						continue;
					}
					
					for (Object srvName : serverlist) {
						if (!server.contains(srvName))
							server.add(String.valueOf(srvName));
					}
					
					System.out.println("[CTSockets][INFO]: Connection successful!");
					isRegistered = true;
					connectionAttempts = 0;					
					continue;
				}
				
				if (packet != null && packet.has("server_connected")) {
					final String srvName = packet.getString("server_connected");

					if (!server.contains(srvName))
						server.add(String.valueOf(srvName));
					
					System.out.println("[CTSockets][INFO]: #ServerConnectedEvent (" + srvName + ")");
					ServerConnectedEvent event = new ServerConnectedEvent(srvName, true);
					Bukkit.getPluginManager().callEvent(event);
					continue;
				}
				
				if (packet != null && packet.has("server_disconnected")) {
					final String srvName = packet.getString("server_disconnected");
					
					if (server.contains(srvName))
						server.remove(String.valueOf(srvName));
					
					System.out.println("[CTSockets][INFO]: #ServerDisconnectedEvent (" + srvName + ")");
					ServerDisconnectedEvent event = new ServerDisconnectedEvent(srvName, true);
					Bukkit.getPluginManager().callEvent(event);
					continue;
				}
				
				if (packet == null || !packet.has("message") || !packet.has("sender")) {
					System.out.println("[CTSockets][WARNING]: INVALID PACKET (Received from 'proxy'");
					System.out.println(inputLine);
				}
				
				String sender = packet.getString("sender");
				String message = packet.getString("message");

				if (message.startsWith("#PING-")) {
					sendMessage(message.replace("PING", "PONG"), sender);
					continue;
				}

				if (message.startsWith("#PONG-")) {
					Long pingID = Long.parseLong(message.replace("#PONG-", ""));
					
					if (pingRequests.containsKey(pingID)) {
						CommandSender pingSender = pingRequests.get(pingID);
						Long time = System.currentTimeMillis() - pingID;
						pingSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Pong &eerhalten. (" + time + "ms)"));
						pingRequests.remove(pingID);
					}
					continue;
				}
				
				System.out.println("[CTSockets][INFO]: Received from '" + clientName + "' -> PACKET[");
				System.out.println("Sender: " + sender);
				System.out.println("Message: " + message + "]");
				
				MessageReceivedEvent event = new MessageReceivedEvent(sender, message, true);
				Bukkit.getPluginManager().callEvent(event);
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
		
		Bukkit.getScheduler().runTaskLater(CTSockets.getInstance(), new Runnable() {
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
		writer.flush();
	}
	
	private void register(String clientName) {
		JSONObject packet = new JSONObject();
		packet.put("register", clientName);
		sendPacket(packet);
	}
	
	public void sendMessage(String message, String target) {
		JSONObject packet = new JSONObject();
		packet.put("sender", clientName);
		packet.put("target", target);
		packet.put("message", message);
		sendPacket(packet);
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public boolean isRegistered() {
		return isRegistered;
	}
}