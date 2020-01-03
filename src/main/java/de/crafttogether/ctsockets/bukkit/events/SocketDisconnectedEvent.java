package de.crafttogether.ctsockets.bukkit.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fires when the socket disconnects
 */

public final class SocketDisconnectedEvent extends Event
{
    private static final HandlerList handlers;
    
    public SocketDisconnectedEvent(Boolean isAsynchronous) {
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