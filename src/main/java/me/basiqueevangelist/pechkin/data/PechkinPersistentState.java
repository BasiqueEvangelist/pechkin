package me.basiqueevangelist.pechkin.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.*;

public class PechkinPersistentState extends PersistentState {
    private final Map<UUID, List<MailMessage>> playerMap = new HashMap<>();

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
            var messagesTag = playerTag.getList("Messages", NbtElement.COMPOUND_TYPE);
            var messages = new ArrayList<MailMessage>();

            for (int j = 0; j < messagesTag.size(); j++) {
                messages.add(MailMessage.fromTag(messagesTag.getCompound(j)));
            }

            playerMap.put(playerId, messages);
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var playersTag = new NbtList();
        nbt.put("Players", playersTag);

        for (var entry : playerMap.entrySet()) {
            if (entry.getValue().isEmpty()) continue;

            var playerTag = new NbtCompound();
            playerTag.put("UUID", NbtHelper.fromUuid(entry.getKey()));
            playersTag.add(playerTag);

            var messagesTag = new NbtList();
            playerTag.put("Messages", messagesTag);
            for (MailMessage message : entry.getValue()) {
                messagesTag.add(message.toTag(new NbtCompound()));
            }
        }

        return nbt;
    }
}
