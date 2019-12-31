package de.crafttogether.ctsockets.bungee.events;

import net.md_5.bungee.api.plugin.Event;

/**
 * Fires when a command is forwarded to another client
 */

public final class CommandForwardedEvent extends Event {
    private String sender;
    private String target;
    private String command;

    public CommandForwardedEvent(String sender, String target, String command) {
        this.sender = sender;
        this.target = target;
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
     * Get the targets name
     * @return target (String)
     */
    public String getTarget() {
        return this.target;
    }
    
    /**
     * Get received command
     * @return command (String)
     */
    public String getCommand() {
        return this.command;
    }
}