package de.crafttogether.ctsockets.bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.crafttogether.ctsockets.bukkit.events.CommandReceivedEvent;
import de.crafttogether.ctsockets.bukkit.events.MessageReceivedEvent;
import de.crafttogether.ctsockets.bukkit.events.ServerConnectedEvent;
import de.crafttogether.ctsockets.bukkit.events.ServerDisconnectedEvent;
import de.crafttogether.ctsockets.bukkit.events.SocketConnectedEvent;
import de.crafttogether.ctsockets.bukkit.events.SocketDisconnectedEvent;
import net.md_5.bungee.api.ChatColor;

/**
 * @hidden
 */

public class CTSocketClient implements Runnable {
	private static CTSocketClient socketClient;
	private CTSockets plugin;
	
	private String clientName;
	private String host;
	private int port;
	private int connectionAttempts;
	private boolean shutdown;
	private boolean isConnected;
	private boolean isRegistered;
	private boolean isReconnecting;
	private boolean whitelisted;
	
	private boolean debug;
	
	public ArrayList<String> server;
	public HashMap<Long, CommandSender> pingRequests;
	
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;

	public CTSocketClient(String host, int port, String clientName) {
		socketClient = this;
		
		this.plugin = CTSockets.getInstance();
		this.server = new ArrayList<String>();
		this.pingRequests = new HashMap<Long, CommandSender>();
		
		this.clientName = String.valueOf(clientName);
		this.host = host;
		this.port = port;
	    this.whitelisted = true;
	    this.isReconnecting = false;
	    
	    this.debug = plugin.getConfig().getBoolean("Settings.debug");
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
			
			if (!isReconnecting)
			plugin.getLogger().warning("Error: Could not connect to " + host + ":" + port + " (" + message + ")");
			
			if (message == e.getMessage())
				e.printStackTrace();
			
			closeConection();
			retryConnect();
			return;
		}
	    
		isConnected = true;
		isReconnecting = false;
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
						plugin.getLogger().warning("Error: IP-Adress not whitelisted");
						whitelisted = false;
					}
					continue;
				}
				
				if (packet != null && packet.has("serverlist")) {
					JSONArray serverlist = packet.getJSONArray("serverlist");
					
					if (serverlist == null) {
						plugin.getLogger().warning("Error: Unable to read serverlist");
						continue;
					}
					
					for (Object srvName : serverlist) {
						if (!server.contains(srvName))
							server.add(String.valueOf(srvName));
					}
					
					plugin.getLogger().info("Connection successful!");
					isRegistered = true;
					connectionAttempts = 0;
					
					SocketConnectedEvent event = new SocketConnectedEvent(true);
					Bukkit.getPluginManager().callEvent(event);
					continue;
				}
				
				if (packet != null && packet.has("server_connected")) {
					final String srvName = packet.getString("server_connected");

					if (!server.contains(srvName))
						server.add(String.valueOf(srvName));
					
					if (debug)
						plugin.getLogger().info("#ServerConnectedEvent (" + srvName + ")");
					
					ServerConnectedEvent event = new ServerConnectedEvent(srvName, true);
					Bukkit.getPluginManager().callEvent(event);
					continue;
				}
				
				if (packet != null && packet.has("server_disconnected")) {
					final String srvName = packet.getString("server_disconnected");
					
					if (server.contains(srvName))
						server.remove(String.valueOf(srvName));
					
					if (debug)
						plugin.getLogger().info("#ServerDisconnectedEvent (" + srvName + ")");
					
					ServerDisconnectedEvent event = new ServerDisconnectedEvent(srvName, true);
					Bukkit.getPluginManager().callEvent(event);
					continue;
				}
				
				if (packet != null && packet.has("command") && packet.has("sender")) {
					String sender = packet.getString("sender");
					String command = packet.getString("command");
					
					if (debug) {
						plugin.getLogger().info("Received Command from: " + sender);
						plugin.getLogger().info(command);
					}
					
					CommandReceivedEvent event = new CommandReceivedEvent(sender, command, true);
					Bukkit.getPluginManager().callEvent(event);
					
					Bukkit.getScheduler().callSyncMethod(CTSockets.getInstance(), new Callable<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
						}
					});
					continue;
				}
				
				if (packet == null || !packet.has("message") || !packet.has("sender")) {
					plugin.getLogger().warning("INVALID PACKET (Received from 'proxy')");
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
						pingSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Pong &ereceived. (" + time + "ms)"));
						pingRequests.remove(pingID);
					}
					continue;
				}
				
				if (debug) {
					plugin.getLogger().info("Received message from '" + clientName + "' -> PACKET[");
					plugin.getLogger().info("Sender: " + sender);
					plugin.getLogger().info("Message: " + message + "]");
				}
				
				MessageReceivedEvent event = new MessageReceivedEvent(sender, message, true);
				Bukkit.getPluginManager().callEvent(event);
			}
		} catch (Exception e) {
			if (!e.getMessage().equalsIgnoreCase("socket closed") && !e.getMessage().equalsIgnoreCase("connection reset"))
				e.printStackTrace();
		}
		
		if (isConnected && !shutdown)
			plugin.getLogger().warning("lost connection to proxy");

		closeConection();
		retryConnect();
	}
	
	public void connect() {
		if (!isReconnecting)
			plugin.getLogger().info("Connecting to " + host + ":" + port + "...");
		
		CTSockets.getInstance().getServer().getScheduler().runTaskAsynchronously(CTSockets.getInstance(), this);
	}
	
	private void retryConnect() {
		if (shutdown || isConnected || !whitelisted)
			return;
		
		final CTSocketClient client = this;
		int delay = 1;
		
		connectionAttempts++;
		if (connectionAttempts > 60) delay = 3;
		
		if (!isReconnecting)
			plugin.getLogger().info("Try to reconnect");
		
		isReconnecting = true;
		
		Bukkit.getScheduler().runTaskLater(CTSockets.getInstance(), new Runnable() {
			@Override
			public void run() {
				client.connect();
			}
		}, delay * 20L);
		
		if (debug)
			plugin.getLogger().info("Try to reconnect to " + host + ":" + port + " in " + delay + " Seconds");
	}

	public void close() {
		this.shutdown = true;
		this.closeConection();
	}
	
	private void closeConection() {
		isConnected = false;
		isRegistered = false;
		
		if (writer != null) {
			writer.flush();
			writer.close();
		}
		
		try {
			if (reader != null)
				reader.close();
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SocketDisconnectedEvent event = new SocketDisconnectedEvent(true);
		Bukkit.getPluginManager().callEvent(event);
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

	public void sendPacket(JSONObject packet) {
		String strPacket = packet.toString();
		
		// TODO: Exception?
		if (strPacket == null)
			return;
		
		writer.println(strPacket + "\r\n");
		writer.flush();
	}
	
	public String getName() {
		return clientName;
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public boolean isRegistered() {
		return isRegistered;
	}

	public static CTSocketClient getInstance() {
		return socketClient;
	}
}