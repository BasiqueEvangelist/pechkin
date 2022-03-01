package me.basiqueevangelist.pechkin.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.basiqueevangelist.pechkin.data.MailMessage;
import me.basiqueevangelist.pechkin.data.PechkinPersistentState;
import me.basiqueevangelist.pechkin.data.PechkinPlayerData;
import me.basiqueevangelist.pechkin.util.CommandUtil;
import me.basiqueevangelist.pechkin.util.NameUtil;
import me.basiqueevangelist.pechkin.util.TimeUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ListCommand {
    private ListCommand() {

    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mail")
            .then(literal("list")
                .executes(ListCommand::list)
                .then(argument("player", GameProfileArgumentType.gameProfile())
                    .suggests(CommandUtil::suggestPlayersExceptSelf)
                    .requires(Permissions.require("pechkin.list", 3))
                    .executes(ListCommand::listOther))));
    }

    public static int listOther(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        GameProfile player = CommandUtil.getOnePlayer(ctx, "player");
        PechkinPlayerData data = PechkinPersistentState.getFromServer(src.getServer()).getDataFor(player.getId());
        Text playerName = new LiteralText(NameUtil.getNameFromUUID(player.getId()))
            .formatted(Formatting.AQUA);

        MutableText complete = new LiteralText("")
            .append(playerName)
            .append(" has " + data.messages().size() + " message" + (data.messages().size() != 1 ? "s" : "") + " stored:");

        for (var message : data.messages()) {
            complete.append(writeMessageDesc(message, playerName, "/mail internal delete_list_other " + NameUtil.getNameFromUUID(player.getId()) + " "));
        }

        src.sendFeedback(complete, false);

        return 1;
    }

    public static int list(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        PechkinPlayerData data = PechkinPersistentState.getFromServer(src.getServer()).getDataFor(player.getUuid());

        MutableText complete = new LiteralText("You have " + data.messages().size() + " message" + (data.messages().size() != 1 ? "s" : "") + " stored:");

        for (var message : data.messages()) {
            complete.append(writeMessageDesc(message, player.getDisplayName(), "/mail internal delete_list "));
        }

        src.sendFeedback(complete, false);

        return 1;
    }

    private static Text writeMessageDesc(MailMessage msg, Text playerName, String deleteCmdPrefix) {
        return new LiteralText("\n[")
            .append(new LiteralText("✘")
            .formatted(Formatting.RED)
            .styled(x -> x.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, deleteCmdPrefix + msg.messageId()))))
            .append(" ")
            .append(new LiteralText("i")
                .formatted(Formatting.BLUE)
                .styled(x -> x.withHoverEvent(HoverEvent.Action.SHOW_TEXT.buildHoverEvent(
                    new LiteralText("Sent ")
                        .append(TimeUtils.formatTime(msg.sentAt()))
                        .append(" ago\nUUID: " + msg.messageId())
                ))))
            .append("] ")
            .append(new LiteralText(NameUtil.getNameFromUUID(msg.sender())).formatted(Formatting.AQUA))
            .append(new LiteralText(" -> ").formatted(Formatting.WHITE))
            .append(playerName.shallowCopy().formatted(Formatting.AQUA))
            .append(new LiteralText(": ").formatted(Formatting.WHITE))
            .append(msg.contents());
    }
}
