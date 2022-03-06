package me.basiqueevangelist.pechkin.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.basiqueevangelist.onedatastore.api.DataStore;
import me.basiqueevangelist.pechkin.Pechkin;
import me.basiqueevangelist.pechkin.data.PechkinPlayerData;
import me.basiqueevangelist.pechkin.hack.StateTracker;
import me.basiqueevangelist.pechkin.util.CommandUtil;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class DeleteCommand {
    private static final SimpleCommandExceptionType MESSAGE_DOESNT_EXIST = new SimpleCommandExceptionType(new LiteralText("No such message"));

    private DeleteCommand() {

    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mail")
            .then(literal("internal")
                .requires(x -> !StateTracker.IS_IN_COMMAND_TREE_CREATION)
                .then(literal("delete_list")
                    .then(argument("message", UuidArgumentType.uuid())
                        .executes(DeleteCommand::deleteList)))
                .then(literal("delete_list_other")
                    .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("message", UuidArgumentType.uuid())
                            .executes(DeleteCommand::deleteListOther))))
                .then(literal("delete_silent")
                    .then(argument("message", UuidArgumentType.uuid())
                        .executes(DeleteCommand::deleteSilent)))));
    }

    private static int deleteSilent(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        UUID messageId = UuidArgumentType.getUuid(ctx, "message");
        PechkinPlayerData data = DataStore.getFor(src.getServer()).getPlayer(player.getUuid(), Pechkin.PLAYER_DATA);

        data.messages().removeIf(x -> x.messageId().equals(messageId));

        return 1;
    }

    private static int deleteList(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        UUID messageId = UuidArgumentType.getUuid(ctx, "message");
        PechkinPlayerData data = DataStore.getFor(src.getServer()).getPlayer(player.getUuid(), Pechkin.PLAYER_DATA);

        if (!data.messages().removeIf(x -> x.messageId().equals(messageId))) {
            throw MESSAGE_DOESNT_EXIST.create();
        }

        // Resend the list, since this command will only be invoked via list anyway 🚎
        ListCommand.list(ctx);

        return 1;
    }

    private static int deleteListOther(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        UUID messageId = UuidArgumentType.getUuid(ctx, "message");
        GameProfile player = CommandUtil.getOnePlayer(ctx, "player");
        PechkinPlayerData data = DataStore.getFor(src.getServer()).getPlayer(player.getId(), Pechkin.PLAYER_DATA);

        if (!data.messages().removeIf(x -> x.messageId().equals(messageId))) {
            throw MESSAGE_DOESNT_EXIST.create();
        }

        // Resend the list, since this command will only be invoked via list anyway 🚎
        ListCommand.listOther(ctx);

        return 1;
    }
}
