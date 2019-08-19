package de.crafttogether.ctsockets.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class ProxyDisconnectedEvent extends Event
{
    private static final HandlerList handlers;
    
    public ProxyDisconnectedEvent(final String serverName) { }
    
    public HandlerList getHandlers() {
        return ProxyDisconnectedEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return ProxyDisconnectedEvent.handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
}