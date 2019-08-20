package de.crafttogether.ctsockets.bungee.events;

import net.md_5.bungee.api.plugin.Event;

public final class ServerDisconnectedEvent extends Event {
    private String serverName;

    public ServerDisconnectedEvent(String serverName) {
        this.serverName = serverName;
    }
    
    public String getServerName() {
        return this.serverName;
    }
}