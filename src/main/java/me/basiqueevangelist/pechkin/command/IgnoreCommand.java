package me.basiqueevangelist.pechkin.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.basiqueevangelist.pechkin.data.PechkinPersistentState;
import me.basiqueevangelist.pechkin.util.NameUtil;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class IgnoreCommand {
    private static SimpleCommandExceptionType TOO_MANY_PLAYERS = new SimpleCommandExceptionType(new LiteralText("Can't (un)ignore many players at once!"));
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
                        .executes(IgnoreCommand::ignoreAdd)))
                .then(literal("remove")
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .executes(IgnoreCommand::ignoreRemove)))
                .then(literal("list")
                    .executes(IgnoreCommand::ignoreList))));
    }

    private static int ignoreAdd(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(ctx, "player");

        if (profiles.size() > 1)
            throw TOO_MANY_PLAYERS.create();

        GameProfile offender = profiles.iterator().next();

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
        Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(ctx, "player");

        if (profiles.size() > 1)
            throw TOO_MANY_PLAYERS.create();

        GameProfile offender = profiles.iterator().next();

        if (offender.getId().equals(player.getUuid()))
            throw SELF_IGNORE.create();

        var state = PechkinPersistentState.getFromServer(src.getServer());
        var playerData = state.getDataFor(player.getUuid());

        if (!playerData.ignoredPlayers().remove(offender.getId()))
            throw NOT_IGNORED.create();

        src.sendFeedback(new LiteralText("Stopped ignoring messages from ")
            .formatted(Formatting.DARK_RED)
            .append(new LiteralText(NameUtil.getNameFromUUID(offender.getId())).formatted(Formatting.AQUA))
            .append("."), false);

        return 1;
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
