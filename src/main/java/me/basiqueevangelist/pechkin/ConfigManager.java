package me.basiqueevangelist.pechkin;


import blue.endless.jankson.Jankson;
import blue.endless.jankson.api.SyntaxError;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Jankson JANKSON = Jankson.builder().build();
    private static final Logger LOGGER = LoggerFactory.getLogger("Pechkin/ConfigManager");

    private PechkinConfig config = new PechkinConfig();

    public ConfigManager() {
        load();
    }

    public PechkinConfig getConfig() {
        return config;
    }

    public void load() {
        Path confPath = FabricLoader.getInstance().getConfigDir().resolve("pechkin.json5");
        if (Files.exists(confPath)) {
            try {
                config = JANKSON.fromJson(JANKSON.load(confPath.toFile()), PechkinConfig.class);
            } catch (IOException | SyntaxError e) {
                LOGGER.error("Could not load config file!", e);
            }
        } else {
            save();
        }
    }

    public void save() {
        Path confPath = FabricLoader.getInstance().getConfigDir().resolve("pechkin.json5");
        try {
            try (BufferedWriter bw = Files.newBufferedWriter(confPath)) {
                bw.write(JANKSON.toJson(config).toJson(true, true));
            }
        } catch (IOException e) {
            LOGGER.error("Could not load config file!", e);
        }
    }
}