package de.crafttogether.ctsockets.bungee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import com.google.common.io.ByteStreams;

import de.crafttogether.ctsockets.bungee.CTSocketServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 * Bungeecord-Plugin
 * @author J0schlZ
 * @version 1.1.0-BETA
 */

public class CTSockets extends Plugin {
	private static CTSockets plugin;
	private static CTSocketServer socketServer;
	
    public static Configuration config;
    public static Configuration messages;
    
    /**
     * @hidden
     */
    @Override
	public void onEnable() {
    	plugin = this;
    	
		loadConfig();
		
		socketServer = new CTSocketServer(config.getInt("Settings.port"));
		getProxy().getScheduler().runAsync(this, socketServer);
		
		System.out.println(this.getDescription().getName() + " v" + this.getDescription().getVersion() + " enabled");
	}
	
    /**
     * @hidden
     */
    @Override
	public void onDisable() {
		socketServer.close();
		System.out.println(this.getDescription().getName() + " v" + this.getDescription().getVersion() + " disabled");
    }
    
	private Configuration loadConfig() {
        if (!getDataFolder().exists()) {
        	this.getDataFolder().mkdir();
        }

        File configFile = new File(getDataFolder(), "config.yml");
        File msgFile = new File(getDataFolder(), "config.yml");
        
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
                InputStream is = getResourceAsStream("bungeeconfig.yml");
                OutputStream os = new FileOutputStream(configFile);
                ByteStreams.copy(is, os);
            }
            
            if (!msgFile.exists()) {
            	msgFile.createNewFile();
                InputStream is = getResourceAsStream("bungeemessages.yml");
                OutputStream os = new FileOutputStream(msgFile);
                ByteStreams.copy(is, os);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to create messages.yml", e);
        }
        try {
        	config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new InputStreamReader(new FileInputStream(configFile), "UTF8"));
        	messages = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new InputStreamReader(new FileInputStream(msgFile), "UTF8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return config;
	}
    
    /**
     * Gets a list of all connected servers, exceptional proxy (Bungeecord)
     * @return ArrayList(String)
     */
    public ArrayList<String> getConnectedServers() {
    	return socketServer.server;
    }
    
    /**
     * Checks if given server is connected
     * @param srvName (String) 
     * @return Boolean
     */
    public boolean isConnected(String srvName) {
    	return socketServer.server.contains(srvName);
    }
    
	/**
	 * Sends given message to the given target.
	 * @param target (String)
	 * @param message (String) 
	 */
    public void sendToServer(String target, String message) {
    	socketServer.sendMessage(message, "#proxy", target);
    }
    
	/**
	 * Broadcasts given message to all connected servers including proxy
	 * @param message (String) 
	 */
    public void sendToAllServers(String message) {
    	socketServer.broadcast(message, "#proxy");
    }
	
	/**
	 * Returns the configuration of the plugin
	 * @return Configuration
	 */
	public Configuration getConfig() {
		return config;
	}
	
	/**
	 * Returns the message configuration of the plugin
	 * @return Configuration
	 */
	public Configuration getMessages() {
		return messages;
	}
	
	/**
	 * @return plugin (CTSockets)
	 */
	public static CTSockets getInstance() {
		return plugin;
	}
}
