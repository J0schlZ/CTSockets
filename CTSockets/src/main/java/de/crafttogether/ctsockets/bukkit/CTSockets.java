package de.crafttogether.ctsockets.bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteStreams;

/**
 * No more Plugin Messaging Channels!
 * @author J0schlZ
 * @version 1.0.0-BETA
 */

public class CTSockets extends JavaPlugin {
	private static CTSockets plugin;
	private static CTSocketClient socketClient;
	private static FileConfiguration config; 
	
    /**
     * @hidden
     */
	@Override
	public void onEnable() {		
		plugin = this;
		
		System.out.println("CTSockets v" + this.getDescription().getVersion() + " enabled");
		loadConfig();
		
		socketClient = new CTSocketClient(config.getString("Settings.host"), config.getInt("Settings.port"), config.getString("Settings.name"));
		socketClient.connect();
	}
	
    /**
     * @hidden
     */
	@Override
	public void onDisable() {
		if (socketClient != null)
			socketClient.close();
		
		System.out.println("CTSockets v" + this.getDescription().getVersion() + " disabled");
	}
	
	public FileConfiguration loadConfig() {
        if (!getDataFolder().exists()) {
        	this.getDataFolder().mkdir();
        }
        
        File configFile = new File(getDataFolder(), "config.yml");
        
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
                InputStream is = getResource("bukkitconfig.yml");
                OutputStream os = new FileOutputStream(configFile);
                ByteStreams.copy(is, os);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to create config.yml", e);
        }

        try {
			config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(configFile)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        
        return config;
	}

    /**
     * Gets a list of all connected servers, exceptional proxy (Bungeecord)
     * @return ArrayList(String)
     */
    public ArrayList<String> getConnectedServers() {
    	return socketClient.server;
    }
    
    /**
     * Checks if given server is connected
     * @param serverName (String) 
     * @return Boolean
     */
    public boolean isConnected(String serverName) {
    	return socketClient.server.contains(serverName);
    }
    
	/**
	 * Sends given message to the proxy (Bungeecord)
	 * @param message (String) 
	 */
	public void sendMessage(String message) {
		socketClient.sendMessage(message);
	}
	
	/**
	 * Sends given message to a specific server
	 * @param message (String) 
	 * @param target (String) 
	 */
	public void sendMessage(String message, String target) {
		socketClient.sendMessage(message, target);
	}
	
	/**
	 * Broadcasts given message to all connected servers including proxy
	 * @param message (String) 
	 */
	public void broadcastMessage(String message) {
		socketClient.sendMessage(message);
	}
	
	/**
	 * Returns the configuration of the plugin
	 * @return FileConfiguration
	 */
	public FileConfiguration getConfig() {
		return config;
	}
	
	/**
	 * Returns the instance of the plugin 
	 * @return CTSockets
	 */
	public static CTSockets getInstance() {
		return plugin;
	}
}
