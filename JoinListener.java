package com.server.rpg.listeners;

import com.server.rpg.RPGPlugin;
import com.server.rpg.classes.ClassSelectGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements Listener {
    private final RPGPlugin plugin;
    public JoinListener(RPGPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();
        if (plugin.getDataManager().getPlayer(player.getUniqueId()) == null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                new ClassSelectGUI(plugin).open(player);
            }, 20L);
        } else {
            plugin.getClassManager().applyClassEffects(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getAuctionManager().giveBackItems(e.getPlayer().getUniqueId());
        plugin.getDataManager().savePlayer(e.getPlayer().getUniqueId());
    }
}
