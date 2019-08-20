package de.crafttogether.ctsockets.bungee.events;

import net.md_5.bungee.api.plugin.Event;

public final class ServerConnectedEvent extends Event {
    private String serverName;

    public ServerConnectedEvent(String serverName) {
        this.serverName = serverName;
    }
    
    public String getServerName() {
        return this.serverName;
    }
}