package me.basiqueevangelist.pechkin;

import me.basiqueevangelist.pechkin.command.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class Pechkin implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            SendCommand.register(dispatcher);
            ListCommand.register(dispatcher);
            DeleteCommand.register(dispatcher);
            IgnoreCommand.register(dispatcher);
            ClearCommand.register(dispatcher);
        });
    }
}
