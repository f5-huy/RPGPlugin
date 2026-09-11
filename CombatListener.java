package com.server.rpg.listeners;

import com.server.rpg.RPGPlugin;
import com.server.rpg.classes.PlayerClass;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatListener implements Listener {
    private final RPGPlugin plugin;
    public CombatListener(RPGPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;

        PlayerClass pc = plugin.getDataManager().getPlayer(attacker.getUniqueId());
        if (pc == null) return;

        double mult = 1.0;
        switch (pc.getType()) {
            case KNIGHT -> {
                if (attacker.getInventory().getItemInMainHand().getType().name().contains("SWORD")) {
                    mult = switch (pc.getLevel()) {
                        case 1 -> 1.05; case 2 -> 1.10; case 3 -> 1.15;
                        case 4 -> 1.20; case 5 -> 1.25; default -> 1.0;
                    };
                }
            }
            case MAGE -> {
                mult = switch (pc.getLevel()) {
                    case 1 -> 0.95; case 2 -> 0.85; case 3 -> 0.75;
                    case 4 -> 0.65; case 5 -> 0.50; default -> 1.0;
                };
            }
            case THIEF -> { }
        }
        e.setDamage(e.getDamage() * mult);
    }
}
