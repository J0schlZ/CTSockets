package de.crafttogether.ctsockets.bukkit.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fires when a server connects
 */

public final class ServerConnectedEvent extends Event
{
    private static final HandlerList handlers;
    private String serverName;
    
    public ServerConnectedEvent(final String serverName, Boolean isAsynchronous) {
        super(isAsynchronous);
    	this.serverName = serverName;
    }
    
    /**
     * Get the servers name
     * @return serverName (String)
     */
    public String getName() {
        return serverName;
    }
    
    /**
     * @hidden
     */
    public HandlerList getHandlers() {
        return handlers;
    }
    
    /**
     * @hidden
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
}