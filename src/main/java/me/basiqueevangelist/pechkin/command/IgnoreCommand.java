package me.basiqueevangelist.pechkin.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.basiqueevangelist.pechkin.data.PechkinPersistentState;
import me.basiqueevangelist.pechkin.util.CommandUtil;
import me.basiqueevangelist.pechkin.util.NameUtil;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class IgnoreCommand {
    private static SimpleCommandExceptionType SELF_IGNORE = new SimpleCommandExceptionType(new LiteralText("Can't (un)ignore yourself!"));
    private static SimpleCommandExceptionType ALREADY_IGNORED = new SimpleCommandExceptionType(new LiteralText("You are already ignoring that player."));
    private static SimpleCommandExceptionType NOT_IGNORED = new SimpleCommandExceptionType(new LiteralText("You aren't ignoring that player!"));

    private IgnoreCommand() {

    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mail")
            .then(literal("ignore")
                .then(literal("add")
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .suggests(IgnoreCommand::ignoreAddSuggest)
                        .executes(IgnoreCommand::ignoreAdd)))
                .then(literal("remove")
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .suggests(IgnoreCommand::ignoreRemoveSuggest)
                        .executes(IgnoreCommand::ignoreRemove)))
                .then(literal("list")
                    .executes(IgnoreCommand::ignoreList))));
    }

    private static int ignoreAdd(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        GameProfile offender = CommandUtil.getOnePlayer(ctx, "player");

        if (offender.getId().equals(player.getUuid()))
            throw SELF_IGNORE.create();

        var state = PechkinPersistentState.getFromServer(src.getServer());
        var playerData = state.getDataFor(player.getUuid());

        if (playerData.ignoredPlayers().contains(offender.getId()))
            throw ALREADY_IGNORED.create();

        playerData.ignoredPlayers().add(offender.getId());

        src.sendFeedback(new LiteralText("Ignoring any further messages from ")
            .formatted(Formatting.GREEN)
            .append(new LiteralText(NameUtil.getNameFromUUID(offender.getId())).formatted(Formatting.AQUA))
            .append("."), false);

        return 1;
    }

    private static int ignoreRemove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        GameProfile offender = CommandUtil.getOnePlayer(ctx, "player");

        if (offender.getId().equals(player.getUuid()))
            throw SELF_IGNORE.create();

        var state = PechkinPersistentState.getFromServer(src.getServer());
        var playerData = state.getDataFor(player.getUuid());

        if (!playerData.ignoredPlayers().remove(offender.getId()))
            throw NOT_IGNORED.create();

        src.sendFeedback(new LiteralText("Stopped ignoring messages from ")
            .formatted(Formatting.YELLOW)
            .append(new LiteralText(NameUtil.getNameFromUUID(offender.getId())).formatted(Formatting.AQUA))
            .append("."), false);

        return 1;
    }

    private static CompletableFuture<Suggestions> ignoreAddSuggest(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        var state = PechkinPersistentState.getFromServer(src.getServer());
        var playerData = state.getDataFor(player.getUuid());

        for (var playerId : playerData.lastCorrespondents()) {
            builder.suggest(NameUtil.getNameFromUUID(playerId));
        }

        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> ignoreRemoveSuggest(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        var state = PechkinPersistentState.getFromServer(src.getServer());
        var playerData = state.getDataFor(player.getUuid());

        for (var playerId : playerData.ignoredPlayers()) {
            builder.suggest(NameUtil.getNameFromUUID(playerId));
        }

        return builder.buildFuture();
    }

    private static int ignoreList(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();

        var state = PechkinPersistentState.getFromServer(src.getServer());
        var playerData = state.getDataFor(player.getUuid());

        MutableText playersBuilder = new LiteralText("");
        boolean isFirst = true;

        for (UUID ignoredPlayer : playerData.ignoredPlayers()) {
            if (!isFirst)
                playersBuilder.append(", ");
            isFirst = false;
            playersBuilder.append(new LiteralText(NameUtil.getNameFromUUID(ignoredPlayer)).formatted(Formatting.AQUA));
        }

        if (isFirst)
            src.sendFeedback(new LiteralText("You aren't ignoring messages from anybody.").formatted(Formatting.GREEN), false);
        else
            src.sendFeedback(new LiteralText("You are ignoring messages from ")
                .formatted(Formatting.GREEN)
                .append(playersBuilder)
                .append("."), false);

        return 1;
    }
}
