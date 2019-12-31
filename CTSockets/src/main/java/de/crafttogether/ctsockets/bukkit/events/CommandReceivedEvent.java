package de.crafttogether.ctsockets.bukkit.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fires when a command is received
 */

public final class CommandReceivedEvent extends Event {
    private static final HandlerList handlers;
    
    private String sender;
    private String command;

    public CommandReceivedEvent(String sender, String command, Boolean isAsynchronous) {
    	super(isAsynchronous);
        this.sender = sender;
        this.command = command;
    }
    
    /**
     * Get the senders name
     * @return sender (String)
     */
    public String getSender() {
        return this.sender;
    }
    
    /**
     * Get the received command
     * @return sender (String)
     */
    public String getCommand() {
        return this.command;
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