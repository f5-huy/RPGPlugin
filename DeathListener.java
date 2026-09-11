package com.server.rpg.listeners;

import com.server.rpg.RPGPlugin;
import com.server.rpg.classes.PlayerClass;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
    private final RPGPlugin plugin;
    public DeathListener(RPGPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        var player = e.getEntity();
        PlayerClass pc = plugin.getDataManager().getPlayer(player.getUniqueId());
        if (pc == null) return;
        pc.addDeath();
        if (pc.getDeathCount() >= 2) {
            pc.setDeathCount(0);
            if (pc.getLevel() > 1) {
                pc.setLevel(pc.getLevel() - 1);
                player.sendMessage("§cТы потерял уровень! Теперь: §f" + pc.getLevel());
                plugin.getClassManager().applyClassEffects(player);
            }
        }
        plugin.getDataManager().savePlayer(player.getUniqueId());
    }
}
