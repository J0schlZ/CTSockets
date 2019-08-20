package de.crafttogether.ctsockets.bungee.events;

import net.md_5.bungee.api.plugin.Event;

public final class MessageForwardedEvent extends Event {
    private String sender;
    private String target;
    private String message;

    public MessageForwardedEvent(String sender, String target, String message) {
        this.sender = sender;
        this.target = target;
        this.message = message;
    }
    
    public String getSender() {
        return this.sender;
    }
    
    public String getTarget() {
        return this.target;
    }
    
    public String getMessage() {
        return this.message;
    }
}