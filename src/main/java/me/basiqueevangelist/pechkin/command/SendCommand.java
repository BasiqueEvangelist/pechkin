package me.basiqueevangelist.pechkin.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.basiqueevangelist.pechkin.Pechkin;
import me.basiqueevangelist.pechkin.data.MailMessage;
import me.basiqueevangelist.pechkin.data.PechkinPersistentState;
import me.basiqueevangelist.pechkin.logic.MailLogic;
import me.basiqueevangelist.pechkin.util.CommandUtil;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.time.Instant;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class SendCommand {
    private static final SimpleCommandExceptionType SELF_MESSAGE = new SimpleCommandExceptionType(new LiteralText("Can't send mail to yourself!"));
    private static final SimpleCommandExceptionType IGNORED = new SimpleCommandExceptionType(new LiteralText("That player has ignored you."));
    private static final SimpleCommandExceptionType RATELIMIT = new SimpleCommandExceptionType(new LiteralText("You are being rate limited."));

    private SendCommand() {

    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mail")
            .then(literal("send")
                .then(argument("player", GameProfileArgumentType.gameProfile())
                    .suggests(CommandUtil::suggestPlayersExceptSelf)
                    .then(argument("message", MessageArgumentType.message())
                        .requires(Permissions.require("pechkin.send", true))
                        .executes(SendCommand::send))))
            .then(argument("player", GameProfileArgumentType.gameProfile())
                .suggests(CommandUtil::suggestPlayersExceptSelf)
                .then(argument("message", MessageArgumentType.message())
                    .requires(Permissions.require("pechkin.send", true))
                    .executes(SendCommand::send))));
    }

    private static int send(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity sender = src.getPlayer();
        GameProfile recipient = CommandUtil.getOnePlayer(ctx, "player");
        Text message = MessageArgumentType.getMessage(ctx, "message");

        if (recipient.getId().equals(sender.getUuid()))
            throw SELF_MESSAGE.create();

        var state = PechkinPersistentState.getFromServer(src.getServer());
        var senderData = state.getDataFor(sender.getUuid());
        var recipientData = state.getDataFor(recipient.getId());

        if (recipientData.ignoredPlayers().contains(sender.getUuid()))
            throw IGNORED.create();

        if (!Permissions.check(sender, "pechkin.bypass.cooldown", 2)) {
            int sendCost = Pechkin.CONFIG.getConfig().sendCost;

            if (!senderData.leakyBucket().hasEnoughFor(sendCost))
                throw RATELIMIT.create();

            senderData.leakyBucket().addTime(sendCost);
        }

        recipientData.addCorrespondent(sender.getUuid());

        MailMessage mail = new MailMessage(message, sender.getUuid(), UUID.randomUUID(), Instant.now());
        recipientData.addMessage(mail);
        MailLogic.notifyMailSent(recipient.getId(), sender, mail);

        ServerPlayerEntity onlineRecipient = src.getServer().getPlayerManager().getPlayer(recipient.getId());
        if (onlineRecipient != null) {
            MailLogic.notifyMailReceived(onlineRecipient, sender, mail);
        }

        return 1;
    }
}
