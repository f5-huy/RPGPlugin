package com.server.rpg.auction;

import com.server.rpg.RPGPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AuctionManager {
    private final RPGPlugin plugin;
    private final List<AuctionItem> active = new ArrayList<>();
    private final Map<UUID, List<AuctionItem>> archive = new HashMap<>();
    private final File file;
    private FileConfiguration config;

    public AuctionManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "auction.yml");
        if (!file.exists()) {
            try { file.getParentFile().mkdirs(); file.createNewFile(); }
            catch (IOException e) { e.printStackTrace(); }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public void addItem(AuctionItem item) {
        active.add(item);
        save();
    }

    public void removeItem(AuctionItem item) {
        active.remove(item);
        save();
    }

    public List<AuctionItem> getActive() { return active; }

    public List<AuctionItem> getArchive(UUID uuid) {
        return archive.computeIfAbsent(uuid, k -> new ArrayList<>());
    }

    public void archiveItem(AuctionItem item) {
        getArchive(item.getSeller()).add(item);
        active.remove(item);
        save();
    }

    public void checkExpired() {
        Iterator<AuctionItem> it = active.iterator();
        while (it.hasNext()) {
            AuctionItem ai = it.next();
            if (ai.isExpired()) {
                it.remove();
                getArchive(ai.getSeller()).add(ai);
            }
        }
        save();
    }

    public void giveBackItems(UUID uuid) {
        List<AuctionItem> arc = getArchive(uuid);
        var p = plugin.getServer().getPlayer(uuid);
        if (p == null || !p.isOnline()) return;
        for (AuctionItem ai : new ArrayList<>(arc)) {
            if (p.getInventory().firstEmpty() == -1) {
                p.getWorld().dropItem(p.getLocation(), ai.getItem());
            } else {
                p.getInventory().addItem(ai.getItem());
            }
        }
        arc.clear();
        save();
    }

    private void load() {
        active.clear();
        archive.clear();

        List<String> activeB64 = config.getStringList("active");
        for (String s : activeB64) {
            try {
                AuctionItem ai = deserialize(s);
                if (ai != null) active.add(ai);
            } catch (Exception ignored) {}
        }

        ConfigurationSection arc = config.getConfigurationSection("archive");
        if (arc != null) {
            for (String key : arc.getKeys(false)) {
                UUID u = UUID.fromString(key);
                List<AuctionItem> l = new ArrayList<>();
                for (String s : config.getStringList("archive." + key)) {
                    try { AuctionItem ai = deserialize(s); if (ai != null) l.add(ai); }
                    catch (Exception ignored) {}
                }
                archive.put(u, l);
            }
        }
    }

    public void save() {
        List<String> activeSer = new ArrayList<>();
        for (AuctionItem ai : active) activeSer.add(serialize(ai));
        config.set("active", activeSer);

        for (UUID u : archive.keySet()) {
            List<String> ser = new ArrayList<>();
            for (AuctionItem ai : archive.get(u)) ser.add(serialize(ai));
            config.set("archive." + u, ser);
        }
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    private String serialize(AuctionItem ai) {
        StringBuilder sb = new StringBuilder();
        sb.append(ai.getSeller()).append(";")
          .append(ai.getPrice()).append(";")
          .append(ai.getListedAt()).append(";")
          .append(Base64.getEncoder().encodeToString(ai.getItem().serializeAsBytes()));
        return sb.toString();
    }

    private AuctionItem deserialize(String s) {
        String[] parts = s.split(";", 4);
        if (parts.length < 4) return null;
        UUID seller = UUID.fromString(parts[0]);
        int price = Integer.parseInt(parts[1]);
        long time = Long.parseLong(parts[2]);
        ItemStack is = ItemStack.deserializeBytes(Base64.getDecoder().decode(parts[3]));
        return new AuctionItem(seller, is, price, time);
    }
}
