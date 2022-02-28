package me.basiqueevangelist.pechkin.util;

import me.basiqueevangelist.pechkin.Pechkin;

import java.util.UUID;

public final class NameUtil {
    private NameUtil() {

    }

    public static String getNameFromUUID(UUID uuid) {
        var optProfile = Pechkin.server.get().getUserCache().getByUuid(uuid);

        if (optProfile.isPresent()) return optProfile.get().getName();

        return "<" + uuid.toString() + ">";
    }
}
