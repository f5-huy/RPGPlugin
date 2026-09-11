package com.server.rpg.classes;

import com.server.rpg.RPGPlugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

public class ClassManager {
    private final RPGPlugin plugin;

    public ClassManager(RPGPlugin plugin) { this.plugin = plugin; }

    public void applyClassEffects(Player player) {
        PlayerClass pc = plugin.getDataManager().getPlayer(player.getUniqueId());
        if (pc == null) return;

        resetAttributes(player);

        double moveSpeed = 0.1;
        double maxHealth = 20.0;

        switch (pc.getType()) {
            case KNIGHT -> {
                double swordBonus = switch (pc.getLevel()) {
                    case 1 -> 0.05; case 2 -> 0.10; case 3 -> 0.15;
                    case 4 -> 0.20; case 5 -> 0.25; default -> 0;
                };
                double speedPenalty = switch (pc.getLevel()) {
                    case 1 -> 0.005; case 2 -> 0.05; case 3 -> 0.07;
                    case 4 -> 0.11; case 5 -> 0.23; default -> 0;
                };
                addModifier(player, Attribute.GENERIC_ATTACK_DAMAGE, "rpg_sword", swordBonus, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
                moveSpeed -= speedPenalty * 0.1;
            }
            case MAGE -> {
                double damagePenalty = switch (pc.getLevel()) {
                    case 1 -> 0.05; case 2 -> 0.15; case 3 -> 0.25;
                    case 4 -> 0.35; case 5 -> 0.50; default -> 0;
                };
                addModifier(player, Attribute.GENERIC_ATTACK_DAMAGE, "rpg_mage", -damagePenalty, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
            }
            case THIEF -> {
                double speedBonus = switch (pc.getLevel()) {
                    case 1 -> 0.05; case 2 -> 0.10; case 3 -> 0.15;
                    case 4 -> 0.20; case 5 -> 0.30; default -> 0;
                };
                double healthPenalty = switch (pc.getLevel()) {
                    case 1 -> 1.0; case 2 -> 3.0; case 3 -> 5.0;
                    case 4 -> 6.0; case 5 -> 10.0; default -> 0;
                };
                addModifier(player, Attribute.GENERIC_MOVEMENT_SPEED, "rpg_thief_speed", speedBonus, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
                maxHealth -= healthPenalty;
            }
        }

        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(moveSpeed);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    }

    private void resetAttributes(Player player) {
        for (Attribute attr : new Attribute[]{
                Attribute.GENERIC_ATTACK_DAMAGE,
                Attribute.GENERIC_MOVEMENT_SPEED,
                Attribute.GENERIC_MAX_HEALTH}) {
            var inst = player.getAttribute(attr);
            if (inst == null) continue;
            for (AttributeModifier mod : new java.util.ArrayList<>(inst.getModifiers())) {
                if (mod.getName().startsWith("rpg_")) inst.removeModifier(mod);
            }
        }
    }

    private void addModifier(Player player, Attribute attr, String name, double value, AttributeModifier.Operation op) {
        var inst = player.getAttribute(attr);
        if (inst == null) return;
        inst.addModifier(new AttributeModifier(
                org.bukkit.NamespacedKey.minecraft(name),
                value, op));
    }

    public void giveClassStarterKit(Player player) {
        PlayerClass pc = plugin.getDataManager().getPlayer(player.getUniqueId());
        if (pc == null) return;
        switch (pc.getType()) {
            case KNIGHT -> {
                if (pc.getLevel() >= 2) player.getInventory().addItem(plugin.getItemFactory().createKnightSword());
            }
            case MAGE -> player.getInventory().addItem(plugin.getItemFactory().createMagicWand());
            case THIEF -> {
                if (pc.getLevel() >= 2) player.getInventory().addItem(plugin.getItemFactory().createThiefKnife());
            }
        }
    }
}
