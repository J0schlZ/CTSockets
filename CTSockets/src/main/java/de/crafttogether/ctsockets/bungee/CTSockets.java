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

public class CTSockets extends Plugin {
	private static CTSockets plugin;
	
    public static CTSocketServer socketServer;
    public static Configuration config;
    
    @Override
	public void onEnable() {
    	plugin = this;
    	
    	System.out.println("CTSockets v" + this.getDescription().getVersion() + " enabled");
    	
		loadConfig();
		socketServer = new CTSocketServer(config.getInt("Settings.port"));
		getProxy().getScheduler().runAsync(this, socketServer);
	}
	
    @Override
	public void onDisable() {
		socketServer.close();
		System.out.println("CTSockets v" + this.getDescription().getVersion() + " disabled");
    }
    
    public ArrayList<String> getConnectedServers() {
    	return socketServer.server;
    }
    
    public boolean isConnected(String srvName) {
    	return socketServer.server.contains(srvName);
    }
    
    public void sendMessage(String message, String target) {
    	socketServer.sendMessage(message, "#proxy", target);
    }
    
    public void broadcastMessage(String message) {
    	socketServer.broadcast(message, "#proxy");
    }
		
	public Configuration loadConfig() {
        if (!getDataFolder().exists()) {
        	this.getDataFolder().mkdir();
        }
        
        File configFile = new File(getDataFolder(), "config.yml");
        
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
                InputStream is = getResourceAsStream("bungeeconfig.yml");
                OutputStream os = new FileOutputStream(configFile);
                ByteStreams.copy(is, os);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to create config.yml", e);
        }
        try {
        	config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new InputStreamReader(new FileInputStream(configFile), "UTF8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return config;
	}
	
	public static CTSockets getInstance() {
		return plugin;
	}
}
