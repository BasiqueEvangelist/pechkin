package me.basiqueevangelist.pechkin.logic;

import me.basiqueevangelist.pechkin.data.MailMessage;
import me.basiqueevangelist.pechkin.util.NameUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public final class MailLogic {
    private static final boolean PINGSPAM_LOADED = FabricLoader.getInstance().isModLoaded("pingspam");

    private MailLogic() {

    }

    public static void notifyMailReceived(MinecraftServer server, UUID receiverId, ServerPlayerEntity sender, MailMessage message) {
        Text fancyMessage = new LiteralText("")
            .append(sender.getDisplayName().shallowCopy().formatted(Formatting.AQUA))
            .append(new LiteralText(" -> ").formatted(Formatting.WHITE))
            .append(new LiteralText(NameUtil.getNameFromUUID(receiverId)).formatted(Formatting.AQUA))
            .append(new LiteralText(": ").formatted(Formatting.WHITE))
            .append(message.contents())
            .append(" [")
            .append(new LiteralText("✔")
                .formatted(Formatting.GREEN)
                .styled(x -> x
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Acknowledge and delete message")))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mail internal delete_silent " + message.messageId()))))
            .append("]");

        ServerPlayerEntity onlinePlayer = server.getPlayerManager().getPlayer(receiverId);

        if (onlinePlayer != null) {
            onlinePlayer.sendMessage(fancyMessage, MessageType.CHAT, sender.getUuid());
        }

        if (PINGSPAM_LOADED)
            PingspamCompat.sendMessageNotification(server, receiverId, fancyMessage);
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
