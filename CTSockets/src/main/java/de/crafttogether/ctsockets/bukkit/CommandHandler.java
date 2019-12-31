package de.crafttogether.ctsockets.bukkit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.json.JSONObject;

import net.md_5.bungee.api.ChatColor;

public class CommandHandler implements TabExecutor {
	private CTSockets plugin;
	private CTSocketClient client;
	
	public CommandHandler(CTSockets plugin) {
		this.plugin = plugin;
		this.client = CTSocketClient.getInstance();
		
		plugin.getCommand("ctsockets").setExecutor(this);
		plugin.getCommand("ctsockets").setTabCompleter(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1)
			return false;
		
		if (!plugin.isConnected())
			sendMessage(sender, "&cThere is no connection to CTSockets!");
		
		// /ctsockets help
		if (args[0].equalsIgnoreCase("help")) {
			sendMessage(sender, "&e/ctsockets help &f- &6Lists all commands of CTSockets");
			sendMessage(sender, "&e/ctsockets list &f- &6Lists all connected servers");
			sendMessage(sender, "&e/ctsockets ping &7<server> &f- &6Ping another server");
			sendMessage(sender, "&e/ctsockets sync &7<server> <cmd> &f- &6Dispatch command on another server");
			return true;
		}
		
		if (!plugin.isConnected())
			return true;
		
		// /ctsockets list
		if (args[0].equalsIgnoreCase("list")) {
			
			if (!sender.hasPermission("ctsockets.list")) {
				sendMessage(sender, "&cYou don't have permission!");
				return true;
			}
			
			ArrayList<String> connectedServers = plugin.getConnectedServers();
			
			if (connectedServers.size() < 1)
				sendMessage(sender, "&cThere aren't any connected servers.");
			else {
				sendMessage(sender, "&6Connected servers&f:");
				
				for (String server : connectedServers)
					sendMessage(sender, "&f- &e" + server);
			}
			
			return true;
		}
		
		// /ctsockets ping <server>
		if (args[0].equalsIgnoreCase("ping")) {

			if (!sender.hasPermission("ctsockets.ping")) {
				sendMessage(sender, "&cYou don't have permission!");
				return true;
			}
			
			if (args.length < 2) {
				sendMessage(sender, "&cYou have to provide the name of a connected server");
				return true;
			}
			else {
				ArrayList<String> connectedServers = plugin.getConnectedServers();
				long pingID = System.currentTimeMillis();
				
				for(String server : connectedServers) {
					if (server.equalsIgnoreCase(args[1])) {
						client.pingRequests.put(pingID, sender);
						client.sendMessage("#PING-" + pingID, server);
						
						sendMessage(sender, "&6Ping &esend...");
						return true;					
					}
				}
				
				sendMessage(sender, "&cThere is no server with name '&6" + args[1] + "' &c connected at this time.");
				return true;
			}
					
		}
		
		// /ctsockets sync <server> <command>
		if (args[0].equalsIgnoreCase("sync")) {
			
			if (!sender.hasPermission("ctsockets.ping")) {
				sendMessage(sender, "&cYou don't have permission!");
				return true;
			}
			
			if (args.length < 2) {
				sendMessage(sender, "&cYou have to provide the name of a connected server");
				return true;
			}
			else {
				ArrayList<String> connectedServers = plugin.getConnectedServers();
				
				for(String server : connectedServers) {
					if (server.equalsIgnoreCase(args[1])) {
						
						if (args.length < 3) {
							sendMessage(sender, "&cYou have to provide a command to dispatch");
							return true;
						}
						else {
							StringBuilder cmdString = new StringBuilder();
							
							for (int i = 2; i < args.length; i++)
								cmdString.append(args[i] + " ");
							
							JSONObject packet = new JSONObject();
							packet.put("sender", client.getName());
							packet.put("target", args[1]);
							packet.put("command", cmdString.toString());
							client.sendPacket(packet);
							
							sendMessage(sender, "&2Command dispatched.");
							return true;	
						}
					}
				}
				
				sendMessage(sender, "&cThere is no server with name '&6" + args[1] + "' &c connected at this time.");
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		ArrayList<String> list = new ArrayList<String>();
		
		if (args.length < 2) {
			list.add("help");
			list.add("list");
			list.add("ping");
			list.add("sync");
		}
		else if (args.length < 3 && args.length > 1) {
			if (args[0].equalsIgnoreCase("ping") || args[0].equalsIgnoreCase("sync")) {				
				for (String server : plugin.getConnectedServers())
					list.add(server);
			}			
		}
		return list;
	}
	
	private void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
}
