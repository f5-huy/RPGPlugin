package com.server.rpg.listeners;

import com.server.rpg.RPGPlugin;
import com.server.rpg.classes.PlayerClass;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Random;

public class ChestListener implements Listener {
    private final RPGPlugin plugin;
    private final Random random = new Random();

    public ChestListener(RPGPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onOpen(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(e.getClickedBlock().getState() instanceof Chest chest)) return;

        Player player = e.getPlayer();
        PlayerClass pc = plugin.getDataManager().getPlayer(player.getUniqueId());
        if (pc == null) return;

        var pdc = chest.getPersistentDataContainer();
        if (!pdc.has(plugin.getLootManager().KEY_LOOT, PersistentDataType.STRING)) return;
        e.setCancelled(true);

        int exp = 200 + random.nextInt(401);
        int coins = 2000 + random.nextInt(10001);
        pc.addExperience(exp);
        pc.addCoins(coins);

        Inventory inv = player.getInventory();
        int items = 4 + random.nextInt(5);
        List<ItemStack> loot = plugin.getLootManager().rollLoot(items);
        for (ItemStack is : loot) {
            if (inv.firstEmpty() == -1) player.getWorld().dropItem(player.getLocation(), is);
            else inv.addItem(is);
        }

        player.sendMessage("§a+ " + exp + " опыта, §6+ " + coins + " монет");

        while (pc.tryLevelUp()) {
            player.sendMessage("§e§lУровень повышен! Теперь: §f" + pc.getLevel());
            plugin.getClassManager().applyClassEffects(player);
            plugin.getClassManager().giveClassStarterKit(player);
        }
        plugin.getDataManager().savePlayer(player.getUniqueId());
    }
}
