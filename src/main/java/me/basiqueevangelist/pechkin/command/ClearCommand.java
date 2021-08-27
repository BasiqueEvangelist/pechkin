package me.basiqueevangelist.pechkin.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.basiqueevangelist.pechkin.data.PechkinPersistentState;
import me.basiqueevangelist.pechkin.data.PechkinPlayerData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public final class ClearCommand {
    private ClearCommand() {

    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mail")
            .then(literal("clear")
                .executes(ClearCommand::clear)));
    }

    private static int clear(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        PechkinPlayerData data = PechkinPersistentState.getFromServer(src.getServer()).getDataFor(player.getUuid());

        Text sent = new LiteralText("Deleted " + data.messages().size() + " message" + (data.messages().size() == 1 ? "." : "s.")).formatted(Formatting.DARK_RED);

        data.messages().clear();

        src.sendFeedback(sent, false);

        return 1;
    }
}
