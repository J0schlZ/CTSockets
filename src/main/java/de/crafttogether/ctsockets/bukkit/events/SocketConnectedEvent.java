package de.crafttogether.ctsockets.bukkit.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fires when the socket connection is established
 */

public final class SocketConnectedEvent extends Event
{
    private static final HandlerList handlers;
    
    public SocketConnectedEvent(Boolean isAsynchronous) {
    	super(isAsynchronous);
    }
    
    /**
     * @hidden
     * @return handlers (HandlerList)
     */
    public HandlerList getHandlers() {
        return handlers;
    }
    
    /**
     * @hidden
     * @return handlers (HandlerList)
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
}