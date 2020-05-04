package fr.andross.banitem.Utils.Debug;

import fr.andross.banitem.Utils.Listable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A debug message with its type
 * @version 2.0
 * @author Andross
 */
public final class DebugMessage {
    private final Listable.Type type;
    private final String node;

    public DebugMessage(@Nullable final Listable.Type type, @NotNull final String node) {
        this.type = type;
        this.node = node;
    }

    /**
     * Get the type of node
     * @return the type of node, null if its not an important node
     */
    @Nullable
    public Listable.Type getType() {
        return type;
    }

    /**
     * The the message for this node
     * @return the message of the node
     */
    @NotNull
    public String getNode() {
        return node;
    }
}
