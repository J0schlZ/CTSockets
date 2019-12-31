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
 * Bukkit-Plugin
 * @author J0schlZ
 * @version 1.1.0-BETA
 */

public class CTSockets extends JavaPlugin {
	private static CTSockets plugin;
	private static CTSocketClient socketClient;
	
	private static FileConfiguration config; 
	private static FileConfiguration messages; 
	
    /**
     * @hidden
     */
	@Override
	public void onEnable() {		
		plugin = this;
		loadConfig();
		
		socketClient = new CTSocketClient(config.getString("Settings.host"), config.getInt("Settings.port"), config.getString("Settings.name"));
		socketClient.connect();
		
		new CommandHandler(this);
		System.out.println(this.getDescription().getName() + " v" + this.getDescription().getVersion() + " enabled");
	}
	
    /**
     * @hidden
     */
	@Override
	public void onDisable() {
		if (socketClient != null)
			socketClient.close();
		
		System.out.println(this.getDescription().getName() + " v" + this.getDescription().getVersion() + " disabled");
	}
	
	private FileConfiguration loadConfig() {
        if (!getDataFolder().exists()) {
        	this.getDataFolder().mkdir();
        }

        File configFile = new File(getDataFolder(), "config.yml");
        File msgFile = new File(getDataFolder(), "messages.yml");
        
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
                InputStream is = getResource("bukkitconfig.yml");
                OutputStream os = new FileOutputStream(configFile);
                ByteStreams.copy(is, os);
            }
            
            if (!msgFile.exists()) {
            	msgFile.createNewFile();
                InputStream is = getResource("bukkitmessages.yml");
                OutputStream os = new FileOutputStream(msgFile);
                ByteStreams.copy(is, os);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to create messages.yml", e);
        }

        try {
			config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(configFile)));
			messages = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(msgFile)));
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
     * Checks if connection is established
     * @return Boolean
     */
    public boolean isConnected() {
    	return socketClient.isRegistered();
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
	public void sendToProxy(String message) {
		socketClient.sendMessage(message, "#proxy");
	}
	
	/**
	 * Sends given message to a given target
	 * @param target (String) 
	 * @param message (String) 
	 */
	public void sendToServer(String target, String message) {
		socketClient.sendMessage(message, target);
	}
	
	/**
	 * Broadcasts given message to all connected servers exceptional proxy
	 * @param message (String) 
	 */
	public void sendToAllServers(String message) {
		socketClient.sendMessage(message, "#server");
	}
	
	/**
	 * Broadcasts given message to all connected servers including proxy
	 * @param message (String) 
	 */
	public void sendToAll(String message) {
		socketClient.sendMessage(message, "#all");
	}
	
	/**
	 * Returns the message configuration of the plugin
	 * @return FileConfiguration
	 */
	public FileConfiguration getMessages() {
		return messages;
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
