package de.crafttogether.ctsockets.bungee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.crafttogether.ctsockets.bungee.events.CommandForwardedEvent;
import de.crafttogether.ctsockets.bungee.events.MessageForwardedEvent;
import de.crafttogether.ctsockets.bungee.events.MessageReceivedEvent;
import net.md_5.bungee.api.ProxyServer;

/**
 * @hidden
 */

public class ConnectionHandler implements Runnable {
	private CTSockets plugin;
	
	private UUID clientID;
	private Socket client;
	private PrintWriter writer;
	private BufferedReader reader;
	private List<String> whitelist;
	private boolean isConnected;
	private boolean isRegistered;
	
	private boolean debug;
	
	public String clientName;
	
	public ConnectionHandler(Socket client) {
		this.plugin = CTSockets.getInstance();
		
		this.client = client;
		this.clientID = UUID.randomUUID();
		this.clientName = "IP(" + client.getInetAddress().getHostAddress() + ")";
		this.isConnected = true;
		this.isRegistered = false;
		
		this.debug = plugin.getConfig().getBoolean("Settings.debug");
		this.whitelist = plugin.getConfig().getStringList("Whitelist");
		
		try {
			this.writer = new PrintWriter(client.getOutputStream(), true);
			this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (checkWhiteList(client.getInetAddress().getHostAddress())) {
			if (debug)
				plugin.getLogger().info(clientName + " connected");
			
			ProxyServer.getInstance().getScheduler().runAsync(plugin, this);
		} else {
			plugin.getLogger().warning(clientName + " tried to connect but is not whitelisted!");
			sendError("NOT_WHITELISTED");
			
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}

	@Override
	public void run() {		
		if (client.isConnected() && !client.isClosed()) {			
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
					
					if (packet != null && packet.has("register")) {
						String serverName = packet.getString("register");
						CTSocketServer.getInstance().registerServer(this, serverName);
						clientName = serverName;
						isRegistered = true;
						sendServerlist();
						continue;
					}
					
					if (packet != null && packet.has("command") && packet.has("sender") && packet.has("target")) {
						String sender = packet.getString("sender");
						String target = packet.getString("target");
						String command = packet.getString("command");
						
						if (CTSocketServer.getInstance().server.contains(target)) {
							if (debug)
								plugin.getLogger().info("Try to redirect command from '" + sender + "' to '" + target);
							
							CTSocketServer.getInstance().sendCommand(command, sender, target);
							
							CommandForwardedEvent forwardedEvent = new CommandForwardedEvent(sender, target, command);
					    	ProxyServer.getInstance().getPluginManager().callEvent(forwardedEvent);
					    	continue;
						}

						// TODO: Send to client?
						if (debug) {
							plugin.getLogger().warning("Server '" + target + "' is not connected.");
							plugin.getLogger().warning("Cannot forward command from '" + sender + "' to '" + target + "'");
							plugin.getLogger().warning(command);
						}
						
						continue;
					}
					
					if (packet == null || !packet.has("message") || !packet.has("sender") || !packet.has("target")) {
						plugin.getLogger().warning("INVALID PACKET (Received from '" + clientName + "')");
						plugin.getLogger().warning(inputLine);
					}

					String sender = packet.getString("sender");
					String target = packet.getString("target");
					String message = packet.getString("message");
					
					if (target.equalsIgnoreCase("#all") || target.equalsIgnoreCase("#proxy") || target.equalsIgnoreCase("#server")) {
						if (!target.equalsIgnoreCase("#server")) {
							MessageReceivedEvent receivedEvent = new MessageReceivedEvent(sender, message);
							ProxyServer.getInstance().getPluginManager().callEvent(receivedEvent);
						}
						
						if (target.equalsIgnoreCase("#all") || target.equalsIgnoreCase("#server")) {							
							for (String server : CTSocketServer.getInstance().server)
								CTSocketServer.getInstance().sendMessage(message, sender, server);
							
							MessageForwardedEvent forwardedEvent = new MessageForwardedEvent(sender, target, message);
					    	ProxyServer.getInstance().getPluginManager().callEvent(forwardedEvent);
						}
						
						continue;
					}
					
					else {
						if (CTSocketServer.getInstance().server.contains(target)) {
							if (debug)
								plugin.getLogger().info("Try to redirect message from '" + sender + "' to '" + target);
							
							CTSocketServer.getInstance().sendMessage(message, sender, target);
							
							MessageForwardedEvent forwardedEvent = new MessageForwardedEvent(sender, target, message);
					    	ProxyServer.getInstance().getPluginManager().callEvent(forwardedEvent);
					    	continue;
						}

						// TODO: Send to client?
						if (debug) {
							plugin.getLogger().warning("Server '" + target + "' is not connected.");
							plugin.getLogger().warning("Cannot forward message from '" + sender + "' to '" + target + "'");
							plugin.getLogger().warning(message);
						}
						
						continue;
					}
				}
			} catch (Exception e) {
				if (!e.getMessage().equalsIgnoreCase("socket closed") && !e.getMessage().equalsIgnoreCase("connection reset"))
					e.printStackTrace();
			}
			
			if (isConnected) {
				if (isRegistered)
					plugin.getLogger().warning("Lost connection to Server '" + clientName + "'");
				else if (debug)
					plugin.getLogger().warning("Lost connection to " + clientName);
	
				disconnect();
			}
		}
	}
	
	public void sendPacket(JSONObject packet) {
		String strPacket = packet.toString();
		
		// TODO: Exception?
		if (strPacket == null)
			return;
		
		writer.println(strPacket + "\r\n");
		writer.flush();
	}
	
	public void sendServerlist() {
		JSONObject wPacket = new JSONObject();
		wPacket.put("serverlist", new JSONArray(CTSocketServer.getInstance().server));
		sendPacket(wPacket);
	}
	
	public void sendError(String err) {
		JSONObject wPacket = new JSONObject();
		wPacket.put("error", err);
		sendPacket(wPacket);
	}

	public void disconnect() {
		boolean wasRegistered = isRegistered;
		isConnected = false;
		isRegistered = false;
		
		writer.flush();
		writer.close();
		
		try {
			reader.close();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (wasRegistered)
			CTSocketServer.getInstance().unregisterServer(this, clientName);
		else
			System.out.println(String.valueOf(clientName) + " disconnected");
		
		CTSocketServer.getInstance().clientDisconnected(clientID);
	}
	
	private boolean checkWhiteList(String ip) {
		boolean match = false;
		for (String wip : whitelist) {
			if (wip.equalsIgnoreCase(ip))
				match = true;
		}
		return match;
	}
	
	public UUID getID() {
		return clientID;
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
}
