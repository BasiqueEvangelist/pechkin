package me.basiqueevangelist.pechkin;

import me.basiqueevangelist.pechkin.command.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

import java.lang.ref.WeakReference;

public class Pechkin implements ModInitializer {
    public static WeakReference<MinecraftServer> server;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(s -> {
            server = new WeakReference<>(s);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            SendCommand.register(dispatcher);
            ListCommand.register(dispatcher);
            DeleteCommand.register(dispatcher);
            IgnoreCommand.register(dispatcher);
            ClearCommand.register(dispatcher);
        });
    }
}
