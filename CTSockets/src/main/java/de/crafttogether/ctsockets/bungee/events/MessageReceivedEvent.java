package de.crafttogether.ctsockets.bungee.events;

import net.md_5.bungee.api.plugin.Event;

public final class MessageReceivedEvent extends Event {
    private String sender;
    private String message;

    public MessageReceivedEvent(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }
    
    public String getSender() {
        return this.sender;
    }
    
    public String getMessage() {
        return this.message;
    }
}