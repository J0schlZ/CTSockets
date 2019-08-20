package de.crafttogether.ctsockets.bungee;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import de.crafttogether.ctsockets.bungee.ConnectionHandler;
import de.crafttogether.ctsockets.bungee.events.ServerConnectedEvent;
import de.crafttogether.ctsockets.bungee.events.ServerDisconnectedEvent;
import net.md_5.bungee.api.ProxyServer;

public class CTSocketServer implements Runnable {
	private int port;
	private Boolean listen;
	
	public ServerSocket socket;
	public ConcurrentHashMap<UUID, ConnectionHandler> clients = new ConcurrentHashMap<UUID, ConnectionHandler>();
	public ArrayList<String> server = new ArrayList<String>();

	public CTSocketServer (int port) {		
	    this.port = port;
		this.listen = true;
	}
	
	@Override
	public void run() {
		try {
			socket = new ServerSocket(port);
			listen = true;
			
			System.out.println("[CTSocketsBungee][INFO]: Waiting for connections...");
			
			while (listen) {
				Socket client = null;
				try {
					client = socket.accept();
				}
				catch (SocketException e) {
					if (!e.getMessage().equalsIgnoreCase("socket closed"))
						e.printStackTrace();
				}
				
				if (client != null)
					handleConnection(client);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public void handleConnection(Socket client) {
		ConnectionHandler connection = new ConnectionHandler(client);
		clients.put(connection.getID(), connection);
	}
	  
	public void close() {
		listen = false;
		
		for (UUID clientID : clients.keySet())
			((ConnectionHandler) clients.get(clientID)).disconnect();
		
		try {
			socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static CTSocketServer getInstance() {
		return CTSockets.socketServer;
	}

	public void clientDisconnected(UUID clientID) {
		if (clients.containsKey(clientID))
			clients.remove(clientID);
	}
	
	public void registerServer(ConnectionHandler conn, String srvName) {
		if (server.contains(srvName)) {
			System.out.println("[CTSocketsBungee][ERROR]: Server '" + srvName + "' was registered before!");
			// TODO: Disconnect ?!
			return;
		}
		
		System.out.println("[CTSocketsBungee][INFO]: " + conn.clientName + " registered as '" + srvName + "'");
		server.add(srvName);
		
		ServerConnectedEvent event = new ServerConnectedEvent(srvName);
    	ProxyServer.getInstance().getPluginManager().callEvent(event);
    	
    	JSONObject packet = new JSONObject();
		packet.put("server_connected", srvName);
		broadcast(packet);
	}
	
	public void unregisterServer(ConnectionHandler client, String srvName) {
		if (!server.contains(srvName))
			return;
		
		System.out.println("[CTSocketsBungee][INFO]: Server '" + srvName + "' disconnected");
		server.remove(srvName);

		ServerDisconnectedEvent event = new ServerDisconnectedEvent(srvName);
    	ProxyServer.getInstance().getPluginManager().callEvent(event);
    	
    	JSONObject packet = new JSONObject();
		packet.put("server_disconnected", srvName);
		broadcast(packet);
	}
	
	private JSONObject buildMessagePacket(String message, String sender) {
		JSONObject packet = new JSONObject();
		packet.put("sender", sender);
		packet.put("message", message);	
		return packet;
	}
	
	public void sendMessage(String message, String sender, String target) {
		JSONObject packet = buildMessagePacket(message, sender);
		if (packet == null) return;
		
		System.out.print("[CTSockets][INFO]: Send message from '" + sender + "' to '" + target + "'\r\n" + message + "\r\n");
		
		for (ConnectionHandler client : clients.values()) {
			if (!client.isConnected() || !client.isRegistered() || !client.getName().equalsIgnoreCase(target)) continue;
				client.sendPacket(packet);
		}
	}
	
	public void broadcast(String message, String sender) {
		JSONObject packet = buildMessagePacket(message, sender);
		if (packet == null) return;
		broadcast(packet);
	}
	
	public void broadcast(JSONObject packet) {
		for (ConnectionHandler client : clients.values()) {
			if (!client.isConnected() || !client.isRegistered()) continue;
				client.sendPacket(packet);
		}
	}
}
