package de.crafttogether.ctsockets.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class ServerDisconnectEvent extends Event
{
    private static final HandlerList handlers;
    private String serverName;
    
    public ServerDisconnectEvent(final String serverName) {
        this.serverName = serverName;
    }
    
    public String getName() {
        return this.serverName;
    }
    
    public HandlerList getHandlers() {
        return ServerDisconnectEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return ServerDisconnectEvent.handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
}