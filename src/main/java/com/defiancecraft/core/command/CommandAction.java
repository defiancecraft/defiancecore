package com.defiancecraft.core.command;

import org.bukkit.command.CommandSender;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * A functional interface for denoting a command's execution.
 *
 * @param <T> The CommandSender extension to restrict to.
 */
public interface CommandAction<T extends CommandSender> extends BiConsumer<T, String[]>, BiPredicate<T, String[]> {

    /**
     * Handle the processing of this command's action.
     *
     * @param sender The sender who ran the command.
     * @param args The arguments passed for the command.
     */
    public boolean invoke(T sender, String[] args);

    /**
     * Handle the processing of this command's action.
     *
     * @param sender The sender who ran the command.
     * @param args The arguments passed for the command.
     */
    public default void accept(T sender, String[] args) {
        invoke(sender, args);
    }

    /**
     * Handle the processing of this command's action.
     *
     * @param sender The sender who ran the command.
     * @param args The arguments passed for the command.
     */
    public default boolean test(T sender, String[] args) {
        return invoke(sender, args);
    }
}
