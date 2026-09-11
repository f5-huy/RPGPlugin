package com.server.rpg.loot;

import com.server.rpg.RPGPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LootManager {
    private final RPGPlugin plugin;
    public final NamespacedKey KEY_LOOT;
    private final Random random = new Random();

    public LootManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.KEY_LOOT = new NamespacedKey(plugin, "loot_chest");
    }

    public void startChestGenerator() {
        new BukkitRunnable() {
            @Override public void run() {
                for (World w : Bukkit.getWorlds()) {
                    if (w.getEnvironment() != World.Environment.NORMAL) continue;
                    int cx = random.nextInt(2000) - 1000;
                    int cz = random.nextInt(2000) - 1000;
                    int y = w.getHighestBlockYAt(cx, cz) + 1;
                    Block b = w.getBlockAt(cx, y, cz);
                    if (b.getType() != Material.AIR) continue;
                    b.setType(Material.CHEST);
                    if (b.getState() instanceof Chest chest) {
                        chest.getPersistentDataContainer().set(KEY_LOOT, PersistentDataType.STRING, "true");
                        chest.update();
                    }
                }
            }
        }.runTaskTimer(plugin, 200L, 20L * 60 * 5);
    }

    public List<ItemStack> rollLoot(int count) {
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ItemStack is = rollOne();
            if (is != null) result.add(is);
        }
        return result;
    }

    private ItemStack rollOne() {
        double r = random.nextDouble() * 100;
        if (r < 50) return new ItemStack(randomVanilla());
        r -= 50;
        if (r < 0.1) return plugin.getItemFactory().createKnightSword();
        return new ItemStack(randomVanilla());
    }

    private Material randomVanilla() {
        Material[] pool = {Material.DIAMOND, Material.IRON_INGOT, Material.GOLD_INGOT,
                Material.EMERALD, Material.NETHERITE_SCRAP, Material.GOLDEN_APPLE,
                Material.EXPERIENCE_BOTTLE, Material.ENDER_PEARL};
        return pool[random.nextInt(pool.length)];
    }
}
