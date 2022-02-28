package me.basiqueevangelist.pechkin.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.basiqueevangelist.pechkin.data.PechkinPersistentState;
import me.basiqueevangelist.pechkin.data.PechkinPlayerData;
import me.basiqueevangelist.pechkin.util.NameUtil;
import me.basiqueevangelist.pechkin.util.TimeUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public final class ListCommand {
    private ListCommand() {

    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mail")
            .then(literal("list")
                .executes(ListCommand::list)));
    }

    public static int list(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        PechkinPlayerData data = PechkinPersistentState.getFromServer(src.getServer()).getDataFor(player.getUuid());

        MutableText complete = new LiteralText("You have " + data.messages().size() + " message" + (data.messages().size() != 1 ? "s" : "") + " stored:");

        for (var message : data.messages()) {
            complete.append(new LiteralText("\n["))
                .append(new LiteralText("✘")
                    .formatted(Formatting.RED)
                    .styled(x -> x.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mail internal delete_list " + message.messageId()))))
                .append(" ")
                .append(new LiteralText("i")
                    .formatted(Formatting.BLUE)
                    .styled(x -> x.withHoverEvent(HoverEvent.Action.SHOW_TEXT.buildHoverEvent(
                        new LiteralText("Sent ")
                            .append(TimeUtils.formatTime(message.sentAt()))
                            .append(" ago\nUUID: " + message.messageId())
                    ))))
                .append("] ")
                .append(new LiteralText(NameUtil.getNameFromUUID(message.sender())).formatted(Formatting.AQUA))
                .append(new LiteralText(" -> ").formatted(Formatting.WHITE))
                .append(player.getDisplayName().shallowCopy().formatted(Formatting.AQUA))
                .append(new LiteralText(": ").formatted(Formatting.WHITE))
                .append(message.contents());
        }

        src.sendFeedback(complete, false);

        return 1;
    }
}
