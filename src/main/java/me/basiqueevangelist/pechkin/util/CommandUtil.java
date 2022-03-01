package me.basiqueevangelist.pechkin.util;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public final class CommandUtil {
    private static SimpleCommandExceptionType TOO_MANY_PLAYERS = new SimpleCommandExceptionType(new LiteralText("Can't (un)ignore many players at once!"));

    private CommandUtil() {

    }

    public static GameProfile getOnePlayer(CommandContext<ServerCommandSource> ctx, String argName) throws CommandSyntaxException {
        Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(ctx, "player");

        if (profiles.size() > 1)
            throw TOO_MANY_PLAYERS.create();

        return profiles.iterator().next();
    }

    public static CompletableFuture<Suggestions> suggestPlayersExceptSelf(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) throws CommandSyntaxException {
        var playerName = ctx.getSource().getPlayer().getEntityName();

        return CommandSource.suggestMatching(
            () -> ctx.getSource()
                .getPlayerNames()
                .stream()
                .filter(x -> !x.equals(playerName))
                .iterator(),
            builder);
    }
}
