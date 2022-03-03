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
    List<UUID> ignoredPlayers,
    List<UUID> lastCorrespondents,
    LeakyBucket leakyBucket
) {
    public static final int CORRESPONDENTS_QUEUE_LENGTH = 10;
    public static final int MESSAGES_LENGTH = 100;

    public PechkinPlayerData() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new LeakyBucket());
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

        var leakyBucket = LeakyBucket.fromTag(tag);

        return new PechkinPlayerData(messages, ignoredPlayers, lastCorrespondents, leakyBucket);
    }

    public boolean isEmpty() {
        return messages.isEmpty() && ignoredPlayers.isEmpty() && leakyBucket.isFull();
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

        leakyBucket.toTag(tag);

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

    public void addMessage(MailMessage msg) {
        if (messages.size() >= MESSAGES_LENGTH)
            messages.remove(MESSAGES_LENGTH - 1);
        messages.add(0, msg);
    }
}
