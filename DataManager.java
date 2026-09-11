package com.server.rpg.data;

import com.server.rpg.RPGPlugin;
import com.server.rpg.classes.ClassType;
import com.server.rpg.classes.PlayerClass;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataManager {
    private final RPGPlugin plugin;
    private final Map<UUID, PlayerClass> cache = new HashMap<>();
    private final File file;
    private FileConfiguration config;

    public DataManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        if (!file.exists()) {
            try { file.getParentFile().mkdirs(); file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public PlayerClass getPlayer(UUID uuid) {
        if (cache.containsKey(uuid)) return cache.get(uuid);
        if (config.contains("players." + uuid)) {
            String typeName = config.getString("players." + uuid + ".class");
            if (typeName == null) return null;
            PlayerClass pc = new PlayerClass(ClassType.valueOf(typeName));
            pc.setLevel(config.getInt("players." + uuid + ".level", 1));
            pc.setExperience(config.getInt("players." + uuid + ".exp", 0));
            pc.setCoins(config.getInt("players." + uuid + ".coins", 10000));
            pc.setDeathCount(config.getInt("players." + uuid + ".deaths", 0));
            cache.put(uuid, pc);
            return pc;
        }
        return null;
    }

    public PlayerClass createPlayer(UUID uuid, ClassType type) {
        PlayerClass pc = new PlayerClass(type);
        cache.put(uuid, pc);
        return pc;
    }

    public void savePlayer(UUID uuid) {
        PlayerClass pc = cache.get(uuid);
        if (pc == null) return;
        String p = "players." + uuid;
        config.set(p + ".class", pc.getType().name());
        config.set(p + ".level", pc.getLevel());
        config.set(p + ".exp", pc.getExperience());
        config.set(p + ".coins", pc.getCoins());
        config.set(p + ".deaths", pc.getDeathCount());
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public void saveAll() {
        for (UUID uuid : cache.keySet()) savePlayer(uuid);
    }
}
