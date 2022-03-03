package me.basiqueevangelist.pechkin.data;

import net.minecraft.nbt.NbtCompound;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class LeakyBucket {
    private static final int MAX_TIMEOUT = 120;

    private Instant fillTime;

    public LeakyBucket() {
        this(Instant.now());
    }

    public LeakyBucket(Instant fillTime) {
        this.fillTime = fillTime;
    }

    public void addTime(int cost) {
        Instant now = Instant.now();
        if (fillTime.isBefore(now))
            fillTime = now.plus(cost, ChronoUnit.SECONDS);
        else
            fillTime = fillTime.plus(cost, ChronoUnit.SECONDS);
    }

    public boolean hasEnoughFor(int cost) {
        return MAX_TIMEOUT - Duration.between(Instant.now(), fillTime).get(ChronoUnit.SECONDS) > cost;
    }

    public boolean isFull() {
        return fillTime.isBefore(Instant.now());
    }

    public static LeakyBucket fromTag(NbtCompound tag) {
        return new LeakyBucket(Instant.ofEpochMilli(tag.getLong("LeakyBucketFillTime")));
    }

    public void toTag(NbtCompound tag) {
        tag.putLong("LeakyBucketFillTime", fillTime.toEpochMilli());
    }
}
