package me.basiqueevangelist.pechkin.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.basiqueevangelist.onedatastore.api.DataStore;
import me.basiqueevangelist.pechkin.Pechkin;
import me.basiqueevangelist.pechkin.data.PechkinPlayerData;
import me.basiqueevangelist.pechkin.util.CommandUtil;
import me.basiqueevangelist.pechkin.util.NameUtil;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ClearCommand {
    private ClearCommand() {

    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mail")
            .then(literal("clear")
                .executes(ClearCommand::clear)
                .then(argument("player", GameProfileArgumentType.gameProfile())
                    .requires(Permissions.require("pechkin.clear.other", 2))
                    .suggests(CommandUtil::suggestPlayersExceptSelf)
                    .executes(ClearCommand::clearOther))));
    }

    private static int clear(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        PechkinPlayerData data = DataStore.getFor(src.getServer()).getPlayer(player.getUuid(), Pechkin.PLAYER_DATA);

        Text sent = new LiteralText("Deleted " + data.messages().size() + " message" + (data.messages().size() == 1 ? "" : "s") + " from your inbox.")
            .formatted(Formatting.GREEN);

        data.messages().clear();

        src.sendFeedback(sent, false);

        return 1;
    }

    private static int clearOther(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        GameProfile player = CommandUtil.getOnePlayer(ctx, "player");
        PechkinPlayerData data = DataStore.getFor(src.getServer()).getPlayer(player.getId(), Pechkin.PLAYER_DATA);

        Text sent = new LiteralText("Deleted " + data.messages().size() + " message" + (data.messages().size() == 1 ? "" : "s") + " from ")
            .append(new LiteralText(NameUtil.getNameFromUUID(player.getId())).formatted(Formatting.AQUA))
            .append("'s inbox.")
            .formatted(Formatting.GREEN);

        data.messages().clear();

        src.sendFeedback(sent, true);

        return 1;
    }
}
