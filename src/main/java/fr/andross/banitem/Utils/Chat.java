package fr.andross.banitem.Utils;

import org.jetbrains.annotations.NotNull;

public final class Chat {
    private static final char COLOR_CHAR = '\u00A7';

    @NotNull
    public static String color(@NotNull final String text) {
        char[] b = text.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = COLOR_CHAR;
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }

}
