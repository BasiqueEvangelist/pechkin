package me.basiqueevangelist.pechkin.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.*;

public class PechkinPersistentState extends PersistentState {
    private final Map<UUID, PechkinPlayerData> playerMap = new HashMap<>();

    public static PechkinPersistentState getFromServer(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(
            PechkinPersistentState::new,
            PechkinPersistentState::new,
            "pechkin"
        );
    }

    public PechkinPersistentState() {

    }

    public PechkinPersistentState(NbtCompound tag) {
        var playersTag = tag.getList("Players", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < playersTag.size(); i++) {
            var playerTag = playersTag.getCompound(i);

            UUID playerId = playerTag.getUuid("UUID");

            playerMap.put(playerId, PechkinPlayerData.fromTag(playerTag));
        }
    }

    public PechkinPlayerData getDataFor(UUID player) {
        return playerMap.computeIfAbsent(player, k -> new PechkinPlayerData());
    }

    public Map<UUID, PechkinPlayerData> getPlayerMap() {
        return playerMap;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var playersTag = new NbtList();
        nbt.put("Players", playersTag);

        for (var entry : playerMap.entrySet()) {
            if (entry.getValue().isEmpty()) continue;

            var playerTag = entry.getValue().toTag(new NbtCompound());
            playerTag.put("UUID", NbtHelper.fromUuid(entry.getKey()));
            playersTag.add(playerTag);
        }

        return nbt;
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
