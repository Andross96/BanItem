/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents additional data associated with a ban action.
 */
public class BanData {
    private final BanDataType type;
    private final Object value;

    public BanData(@NotNull final BanDataType type, @Nullable final Object value) {
        this.type = type;
        this.value = value;
    }

    @NotNull
    public BanDataType getType() {
        return type;
    }

    @Nullable
    public Object getValue() {
        return value;
    }
}
