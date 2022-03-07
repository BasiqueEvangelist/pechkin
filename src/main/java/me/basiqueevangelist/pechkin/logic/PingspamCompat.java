package me.basiqueevangelist.pechkin.logic;

import me.basiqueevangelist.pingspam.api.PingspamApiV0;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.util.UUID;

public final class PingspamCompat {
    private PingspamCompat() {

    }

    public static void sendMessageNotification(MinecraftServer server, UUID receiver, Text fancyMessage) {
        PingspamApiV0.sendNotificationTo(server, receiver, fancyMessage);
    }
}
