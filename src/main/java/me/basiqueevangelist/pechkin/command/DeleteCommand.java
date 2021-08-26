package me.basiqueevangelist.pechkin.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.basiqueevangelist.pechkin.data.PechkinPersistentState;
import me.basiqueevangelist.pechkin.data.PechkinPlayerData;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

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
                .then(literal("delete")
                    .then(argument("message", UuidArgumentType.uuid())
                        .executes(DeleteCommand::deleteMail)))));
    }

    private static int deleteMail(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        UUID messageId = UuidArgumentType.getUuid(ctx, "message");
        PechkinPlayerData data = PechkinPersistentState.getFromServer(src.getServer()).getDataFor(player.getUuid());

        if (!data.messages().removeIf(x -> x.messageId().equals(messageId))) {
            throw MESSAGE_DOESNT_EXIST.create();
        }

        // Resend the list, since this command will only be invoked via list anyway 🚎
        ListCommand.list(ctx);

        return 1;
    }
}
