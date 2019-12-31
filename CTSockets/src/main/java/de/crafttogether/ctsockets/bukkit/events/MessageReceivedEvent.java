package de.crafttogether.ctsockets.bukkit.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fires when a message is received
 */

public final class MessageReceivedEvent extends Event {
    private static final HandlerList handlers;
    
    private String sender;
    private String message;

    public MessageReceivedEvent(String sender, String message, Boolean isAsynchronous) {
    	super(isAsynchronous);
        this.sender = sender;
        this.message = message;
    }
    
    /**
     * Get the senders name
     * @return sender (String)
     */
    public String getSender() {
        return this.sender;
    }
    
    /**
     * Get the received message
     * @return sender (String)
     */
    public String getMessage() {
        return this.message;
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