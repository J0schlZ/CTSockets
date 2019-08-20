package de.crafttogether.ctsockets.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class ServerDisconnectedEvent extends Event
{
    private static final HandlerList handlers;
    private String serverName;
    
    public ServerDisconnectedEvent(final String serverName) {
        this.serverName = serverName;
    }
    
    public String getName() {
        return serverName;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
}