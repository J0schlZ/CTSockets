package de.crafttogether.ctsockets.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class ProxyConnectedEvent extends Event
{
    private static final HandlerList handlers;
    
    public ProxyConnectedEvent(final String serverName) { }
    
    public HandlerList getHandlers() {
        return ProxyConnectedEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return ProxyConnectedEvent.handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
}