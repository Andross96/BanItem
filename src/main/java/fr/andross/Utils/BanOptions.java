package fr.andross.Utils;

import java.util.Set;

final public class BanOptions {
    private final Set<BanOption> options;
    private final String message;

    BanOptions(final Set<BanOption> options, final String message){
        this.options = options;
        this.message = message;
    }

    public boolean hasOption(final BanOption o){ return options.contains(o); }
    public String getMessage(){ return message; }
}
