package me.basiqueevangelist.pechkin.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PechkinPlayerData {
    public static final int CORRESPONDENTS_QUEUE_LENGTH = 10;

    private final List<MailMessage> messages;
    private final List<UUID> ignoredPlayers;
    private final List<UUID> lastCorrespondents;
    private Instant lastMessageSent;

    public PechkinPlayerData(
        List<MailMessage> messages,
        List<UUID> ignoredPlayers,
        List<UUID> lastCorrespondents,
        Instant lastMessageSent
    ) {
        this.messages = messages;
        this.ignoredPlayers = ignoredPlayers;
        this.lastCorrespondents = lastCorrespondents;
        this.lastMessageSent = lastMessageSent;
    }

    public PechkinPlayerData() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), Instant.EPOCH);
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

        var lastCorrespondentsTag = tag.getList("LastCorrespondents", NbtElement.INT_ARRAY_TYPE);
        var lastCorrespondents = new ArrayList<UUID>();

        for (var correspondentTag : lastCorrespondentsTag) {
            lastCorrespondents.add(NbtHelper.toUuid(correspondentTag));
        }

        Instant lastMessageSent = Instant.ofEpochMilli(tag.getLong("LastMessageSent"));

        return new PechkinPlayerData(messages, ignoredPlayers, lastCorrespondents, lastMessageSent);
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

        if (!lastCorrespondents.isEmpty()) {
            var lastCorrespondentsTag = new NbtList();
            tag.put("LastCorrespondents", lastCorrespondentsTag);
            for (var correspondent : lastCorrespondents) {
                lastCorrespondentsTag.add(NbtHelper.fromUuid(correspondent));
            }
        }

        return tag;
    }

    public void addCorrespondent(UUID id) {
        if (!lastCorrespondents.contains(id)) {
            if (lastCorrespondents.size() >= CORRESPONDENTS_QUEUE_LENGTH)
                lastCorrespondents.remove(CORRESPONDENTS_QUEUE_LENGTH - 1);
            lastCorrespondents.add(0, id);
        } else {
            int index = lastCorrespondents.indexOf(id);
            if (index != 0) {
                lastCorrespondents.remove(index);
                lastCorrespondents.add(0, id);
            }
        }
    }

    public List<MailMessage> messages() {
        return messages;
    }

    public List<UUID> ignoredPlayers() {
        return ignoredPlayers;
    }

    public List<UUID> lastCorrespondents() {
        return lastCorrespondents;
    }

    public Instant getLastMessageSent() {
        return lastMessageSent;
    }

    public void setLastMessageSent(Instant lastMessageSent) {
        this.lastMessageSent = lastMessageSent;
    }
}
