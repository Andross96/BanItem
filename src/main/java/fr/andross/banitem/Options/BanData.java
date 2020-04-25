package fr.andross.banitem.Options;

import org.jetbrains.annotations.NotNull;

/**
 * The ban data used into a ban check
 * The object will be an instance of the described {@link BanDataType}
 * Example: if type == BanDataType.CREATIVE, the object is a boolean.
 * @version 2.0
 * @author Andross
 */
public final class BanData {
    private final BanDataType type;
    private final Object o;

    public BanData(@NotNull final BanDataType type, @NotNull final Object o) {
        this.type = type;
        this.o = o;
    }

    /**
     * @return the ban data type
     */
    @NotNull
    public BanDataType getType() {
        return type;
    }

    /**
     * @return the object, which is an instance described by the {@link BanDataType}
     */
    @NotNull
    public Object getObject() {
        return o;
    }
}
