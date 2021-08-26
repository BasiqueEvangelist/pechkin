package me.basiqueevangelist.pechkin.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.basiqueevangelist.pechkin.data.MailMessage;
import me.basiqueevangelist.pechkin.data.PechkinPersistentState;
import me.basiqueevangelist.pechkin.logic.MailLogic;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class SendCommand {
    private static SimpleCommandExceptionType TOO_MANY_PLAYERS = new SimpleCommandExceptionType(new LiteralText("Can't send mail to many players at once!"));
    private static SimpleCommandExceptionType SELF_MESSAGE = new SimpleCommandExceptionType(new LiteralText("Can't send mail to yourself!"));
    private static SimpleCommandExceptionType IGNORED = new SimpleCommandExceptionType(new LiteralText("That player has ignored you."));

    private SendCommand() {

    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mail")
            .then(literal("send")
                .then(argument("player", GameProfileArgumentType.gameProfile())
                    .then(argument("message", MessageArgumentType.message())
                        .executes(SendCommand::send)))));
    }

    private static int send(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity sender = src.getPlayer();
        Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(ctx, "player");
        Text message = MessageArgumentType.getMessage(ctx, "message");

        if (profiles.size() > 1)
            throw TOO_MANY_PLAYERS.create();

        GameProfile recipient = profiles.iterator().next();

        if (recipient.getId().equals(sender.getUuid()))
            throw SELF_MESSAGE.create();

        var state = PechkinPersistentState.getFromServer(src.getServer());
        var recipientData = state.getDataFor(recipient.getId());

        if (recipientData.ignoredPlayers().contains(sender.getUuid()))
            throw IGNORED.create();

        MailMessage mail = new MailMessage(message, sender.getUuid(), UUID.randomUUID(), Instant.now());
        recipientData.messages().add(mail);
        MailLogic.notifyMailSent(recipient.getId(), sender, mail);

        ServerPlayerEntity onlineRecipient = src.getServer().getPlayerManager().getPlayer(recipient.getId());
        if (onlineRecipient != null) {
            MailLogic.notifyMailReceived(onlineRecipient, sender, mail);
        }

        return 1;
    }
}
