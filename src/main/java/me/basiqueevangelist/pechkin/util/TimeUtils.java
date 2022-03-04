package me.basiqueevangelist.pechkin.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

import java.time.Duration;
import java.time.Instant;

public final class TimeUtils {
    private TimeUtils() {

    }

    public static MutableText formatTime(Instant time) {
        var duration = Duration.between(time, Instant.now());

        MutableText text = new LiteralText("");
        if (duration.toDaysPart() > 0)
            text.append(duration.toDaysPart() + "d");
        if (duration.toHoursPart() > 0)
            text.append(duration.toHoursPart() + "h");
        if (duration.toMinutesPart() > 0)
            text.append(duration.toMinutesPart() + "m");
        if (duration.toSecondsPart() > 0)
            text.append(duration.toSecondsPart() + "s");

        return text;
    }
}
