package me.basiqueevangelist.pechkin.util;

import com.mojang.authlib.GameProfile;
import me.basiqueevangelist.pechkin.Pechkin;

import java.util.UUID;

public final class NameUtil {
    private NameUtil() {

    }

    public static String getNameFromUUID(UUID uuid) {
        var optProfile = Pechkin.server.get().getUserCache().getByUuid(uuid);

        if (optProfile.isPresent()) return optProfile.get().getName();

        GameProfile profile = new GameProfile(uuid, null);
        Pechkin.server.get().getSessionService().fillProfileProperties(profile, true);

        if (profile.getName() != null) return profile.getName();

        return "<" + uuid.toString() + ">";
    }
}
