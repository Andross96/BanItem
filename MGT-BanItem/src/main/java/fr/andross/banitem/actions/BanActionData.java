/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem.actions;

import org.jetbrains.annotations.NotNull;
import java.util.*;

/**
 * Represents the data associated with a banned item action.
 * Contains messages, commands, and other action-specific data.
 */
public class BanActionData {
    private final List<String> messages;
    private final List<String> commands;
    private final Map<String, Object> additionalData;

    public BanActionData() {
        this.messages = new ArrayList<>();
        this.commands = new ArrayList<>();
        this.additionalData = new HashMap<>();
    }

    public BanActionData(@NotNull final List<String> messages, 
                        @NotNull final List<String> commands,
                        @NotNull final Map<String, Object> additionalData) {
        this.messages = new ArrayList<>(messages);
        this.commands = new ArrayList<>(commands);
        this.additionalData = new HashMap<>(additionalData);
    }

    @NotNull
    public List<String> getMessages() {
        return messages;
    }

    @NotNull
    public List<String> getCommands() {
        return commands;
    }

    @NotNull
    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void addMessage(@NotNull final String message) {
        messages.add(message);
    }

    public void addCommand(@NotNull final String command) {
        commands.add(command);
    }

    public void putData(@NotNull final String key, @NotNull final Object value) {
        additionalData.put(key, value);
    }

    @NotNull
    public Map<String, Object> serialize() {
        final Map<String, Object> result = new HashMap<>();
        if (!messages.isEmpty()) {
            result.put("message", messages.size() == 1 ? messages.get(0) : messages);
        }
        if (!commands.isEmpty()) {
            result.put("command", commands.size() == 1 ? commands.get(0) : commands);
        }
        result.putAll(additionalData);
        return result;
    }
}
