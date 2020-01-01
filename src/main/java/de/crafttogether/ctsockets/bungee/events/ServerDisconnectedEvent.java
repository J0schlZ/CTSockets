package de.crafttogether.ctsockets.bungee.events;

import net.md_5.bungee.api.plugin.Event;

/**
 * Fires when a server disconnects
 */

public final class ServerDisconnectedEvent extends Event {
    private String serverName;

    public ServerDisconnectedEvent(String serverName) {
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