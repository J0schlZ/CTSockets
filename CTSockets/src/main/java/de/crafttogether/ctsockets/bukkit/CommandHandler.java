package de.crafttogether.ctsockets.bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import net.md_5.bungee.api.ChatColor;

public class CommandHandler implements TabExecutor {
	private CTSockets plugin;
	private HashMap<UUID, CommandSender> pingRequests;
	
	public CommandHandler(CTSockets plugin) {
		this.plugin = plugin;
		this.pingRequests = new HashMap<UUID, CommandSender>();
		
		plugin.getCommand("ctsockets").setExecutor(this);
		plugin.getCommand("ctsockets").setTabCompleter(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1)
			return false;
		
		if (!plugin.isConnected())
			sendMessage(sender, "&cEs besteht keine Verbindung zu CTSockets!");
		
		// /ctsockets help
		if (args[0].equalsIgnoreCase("help")) {
			sendMessage(sender, "&e/ctsockets help &f- &6Listet alle commands von CTSockets");
			sendMessage(sender, "&e/ctsockets list &f- &6Listet alle verbundenen Server");
			sendMessage(sender, "&e/ctsockets ping &7<server> &f- &6Pingt einen anderen Server an");
			sendMessage(sender, "&e/ctsockets sync &7<server> <cmd> &f- &6Führt einen Befehl auf einem anderen Server aus");
			return true;
		}
		
		if (!plugin.isConnected())
			return true;
		
		// /ctsockets list
		if (args[0].equalsIgnoreCase("list")) {
			
			if (!sender.hasPermission("ctsockets.list")) {
				sendMessage(sender, "&cDazu hast du keine Berechtigung.");
				return true;
			}
			
			ArrayList<String> connectedServers = plugin.getConnectedServers();
			
			if (connectedServers.size() < 1)
				sendMessage(sender, "&cEs sind keine weiteren Server verbunden.");
			else {
				sendMessage(sender, "&6Verbundene Server&e:");
				
				for (String server : connectedServers)
					sendMessage(sender, "&f- &e" + server);
			}
			
			return true;
		}
		
		
		// /ctsockets ping <server>
		if (args[0].equalsIgnoreCase("ping")) {

			if (!sender.hasPermission("ctsockets.ping")) {
				sendMessage(sender, "&cDazu hast du keine Berechtigung.");
				return true;
			}
			
			if (args.length < 2) {
				sendMessage(sender, "&cBitte gebe den Namen des gewünschten Server an.");
				return true;
			}
			else {
				ArrayList<String> connectedServers = plugin.getConnectedServers();
				
				for(String server : connectedServers) {
					if (server.equalsIgnoreCase(args[1])) {
						UUID pingID = UUID.randomUUID();
						pingRequests.put(pingID, sender);
						plugin.sendToServer(server, "#PING|" + pingID);						
					}
					
					sendMessage(sender, "&6Ping &egesendet...");
					return true;
				}
				
				sendMessage(sender, "&cEs ist kein Server mit diesem Namen verbunden.");
				return true;
			}
					
		}
		
		
		// /ctsockets sync <server> <command>
		if (args[0].equalsIgnoreCase("sync")) {
			
			if (!sender.hasPermission("ctsockets.ping")) {
				sendMessage(sender, "&cDazu hast du keine Berechtigung.");
				return true;
			}
			
			sendMessage(sender, "&cComing soon... ^^");
			return true;
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
