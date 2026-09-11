package com.server.rpg.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private final Map<String, Long> cooldowns = new HashMap<>();

    public void set(UUID player, String key, long millis) {
        cooldowns.put(player + ":" + key, System.currentTimeMillis() + millis);
    }

    public boolean isReady(UUID player, String key) {
        Long until = cooldowns.get(player + ":" + key);
        return until == null || System.currentTimeMillis() >= until;
    }

    public long remaining(UUID player, String key) {
        Long until = cooldowns.get(player + ":" + key);
        if (until == null) return 0;
        return Math.max(0, until - System.currentTimeMillis());
    }
}
