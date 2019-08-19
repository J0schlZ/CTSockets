package de.crafttogether;

import java.io.IOException;
import java.net.UnknownHostException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import de.crafttogether.ctsockets.CTSocketClient;

public class CTSockets extends JavaPlugin {
	private static CTSockets plugin;
	
	public static FileConfiguration config;
	public static CTSocketClient socketClient;
	
	@Override
	public void onEnable() {		
		plugin = this;
		
		System.out.println("CTSockets v" + this.getDescription().getVersion() + " enabled");
		loadConfig();
		
		String name = config.getString("name");
		if (name.equals("#"))
			name = Bukkit.getServer().getName();
		
		try {
			socketClient = new CTSocketClient(config.getString("Settings.host"), config.getInt("Settings.port"), name);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		socketClient.close();
		System.out.println("CTSockets v" + this.getDescription().getVersion() + " disabled");
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
