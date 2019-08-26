package de.crafttogether.ctsockets.bungee.events;

import net.md_5.bungee.api.plugin.Event;

/**
 * Fires when a message is received
 */

public final class MessageReceivedEvent extends Event {
    private String sender;
    private String message;

    public MessageReceivedEvent(String sender, String message) {
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
}