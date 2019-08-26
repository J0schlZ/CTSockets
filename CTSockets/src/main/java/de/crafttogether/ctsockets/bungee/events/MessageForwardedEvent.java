package de.crafttogether.ctsockets.bungee.events;

import net.md_5.bungee.api.plugin.Event;

/**
 * Fires when a message is forwarded to another client
 */

public final class MessageForwardedEvent extends Event {
    private String sender;
    private String target;
    private String message;

    public MessageForwardedEvent(String sender, String target, String message) {
        this.sender = sender;
        this.target = target;
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
     * Get the targets name
     * @return target (String)
     */
    public String getTarget() {
        return this.target;
    }
    
    /**
     * Get received message
     * @return message (String)
     */
    public String getMessage() {
        return this.message;
    }
}