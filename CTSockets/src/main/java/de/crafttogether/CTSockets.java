package de.crafttogether;

import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import de.crafttogether.ctsockets.CTSocketClient;

public class CTSockets extends JavaPlugin {
	private static CTSockets plugin;
	private static CTSocketClient socketClient;
	
	public static FileConfiguration config;
	
	@Override
	public void onEnable() {		
		plugin = this;
		
		System.out.println("CTSockets v" + this.getDescription().getVersion() + " enabled");
		loadConfig();
		
		socketClient = new CTSocketClient(config.getString("Settings.host"), config.getInt("Settings.port"), config.getString("Settings.name"));
		socketClient.connect();
	}
	
	@Override
	public void onDisable() {
		socketClient.close();
		System.out.println("CTSockets v" + this.getDescription().getVersion() + " disabled");
	}

    public ArrayList<String> getConnectedServers() {
    	return socketClient.server;
    }
    
    public boolean isConnected(String srvName) {
    	return socketClient.server.contains(srvName);
    }
    
	public void sendMessage(String message) {
		socketClient.sendMessage(message);
	}
	
	public void sendMessage(String message, String target) {
		socketClient.sendMessage(message, target);
	}
	
	public void broadcastMessage(String message) {
		socketClient.sendMessage(message);
	}
	
	public FileConfiguration loadConfig() {
		FileConfiguration cfg = getConfig();
		cfg.options().copyDefaults(true);
		saveConfig();
		config = getConfig();
		return config;
	}
	
	public static CTSockets getInstance() {
		return plugin;
	}
}
