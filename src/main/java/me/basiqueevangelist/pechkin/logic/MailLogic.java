package me.basiqueevangelist.pechkin.logic;

import me.basiqueevangelist.pechkin.data.MailMessage;
import me.basiqueevangelist.pechkin.util.NameUtil;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.UUID;

public final class MailLogic {
    private MailLogic() {

    }

    public static void notifyMailReceived(ServerPlayerEntity player, ServerPlayerEntity sender, MailMessage message) {
        player.sendMessage(new LiteralText("")
            .append(sender.getDisplayName().shallowCopy().formatted(Formatting.AQUA))
            .append(new LiteralText(" -> ").formatted(Formatting.WHITE))
            .append(player.getDisplayName().shallowCopy().formatted(Formatting.AQUA))
            .append(new LiteralText(": ").formatted(Formatting.WHITE))
            .append(message.contents()), MessageType.CHAT, sender.getUuid());
    }

    public static void notifyMailSent(UUID recipient, ServerPlayerEntity sender, MailMessage message) {
        sender.sendMessage(new LiteralText("")
            .append(sender.getDisplayName().shallowCopy().formatted(Formatting.AQUA))
            .append(new LiteralText(" -> ").formatted(Formatting.WHITE))
            .append(new LiteralText(NameUtil.getNameFromUUID(recipient)).formatted(Formatting.AQUA))
            .append(new LiteralText(": ").formatted(Formatting.WHITE))
            .append(message.contents()), MessageType.CHAT, sender.getUuid());
    }
}
