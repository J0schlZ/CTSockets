package de.crafttogether.ctsockets.bungee.events;

import net.md_5.bungee.api.plugin.Event;

/**
 * Fires when a server connects
 */

public final class ServerConnectedEvent extends Event {
    private String serverName;

    public ServerConnectedEvent(String serverName) {
        this.serverName = serverName;
    }
    
    /**
     * Get the servers name
     * @return serverName (String)
     */
    public String getServerName() {
        return this.serverName;
    }
}