package me.basiqueevangelist.pechkin.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record PechkinPlayerData(
    List<MailMessage> messages,
    List<UUID> ignoredPlayers
) {
    public PechkinPlayerData() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public static PechkinPlayerData fromTag(NbtCompound tag) {
        var messagesTag = tag.getList("Messages", NbtElement.COMPOUND_TYPE);
        var messages = new ArrayList<MailMessage>();

        for (int i = 0; i < messagesTag.size(); i++) {
            messages.add(MailMessage.fromTag(messagesTag.getCompound(i)));
        }

        var ignoredPlayersTag = tag.getList("IgnoredPlayers", NbtElement.INT_ARRAY_TYPE);
        var ignoredPlayers = new ArrayList<UUID>();

        for (var ignoredPlayerTag : ignoredPlayersTag) {
            ignoredPlayers.add(NbtHelper.toUuid(ignoredPlayerTag));
        }

        return new PechkinPlayerData(messages, ignoredPlayers);
    }

    public boolean isEmpty() {
        return messages.isEmpty() && ignoredPlayers.isEmpty();
    }

    public NbtCompound toTag(NbtCompound tag) {
        if (!messages.isEmpty()) {
            var messagesTag = new NbtList();
            tag.put("Messages", messagesTag);
            for (var message : messages) {
                messagesTag.add(message.toTag(new NbtCompound()));
            }
        }

        if (!ignoredPlayers.isEmpty()) {
            var ignoresTag = new NbtList();
            tag.put("IgnoredPlayers", ignoresTag);
            for (var ignoredPlayer : ignoredPlayers) {
                ignoresTag.add(NbtHelper.fromUuid(ignoredPlayer));
            }
        }

        return tag;
    }
}
