package com.server.rpg.items;

import com.server.rpg.RPGPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ItemFactory {
    private final RPGPlugin plugin;
    public final NamespacedKey KEY_CUSTOM;
    public final NamespacedKey KEY_BOUND;

    public ItemFactory(RPGPlugin plugin) {
        this.plugin = plugin;
        this.KEY_CUSTOM = new NamespacedKey(plugin, "custom_id");
        this.KEY_BOUND = new NamespacedKey(plugin, "bound");
    }

    public ItemStack createKnightSword() {
        ItemStack is = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = is.getItemMeta();
        meta.displayName(Component.text("§cМеч Рыцаря"));
        meta.addEnchant(Enchantment.SHARPNESS, 17, true);
        meta.addEnchant(Enchantment.LOOTING, 5, true);
        meta.addEnchant(Enchantment.KNOCKBACK, 2, true);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 5, true);
        meta.addEnchant(Enchantment.UNBREAKING, 255, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        meta.lore(List.of(
                Component.text("§7Нельзя выбросить, продать или положить в сундук"),
                Component.text("§7Хранится в Эндер-сундуке или инвентаре")
        ));
        meta.getPersistentDataContainer().set(KEY_CUSTOM, PersistentDataType.STRING, "knight_sword");
        meta.getPersistentDataContainer().set(KEY_BOUND, PersistentDataType.STRING, "true");
        is.setItemMeta(meta);
        return is;
    }

    public ItemStack createMagicWand() {
        ItemStack is = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = is.getItemMeta();
        meta.displayName(Component.text("§9Волшебная палочка"));
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.lore(List.of(Component.text("§7ПКМ — использовать заклинание")));
        meta.getPersistentDataContainer().set(KEY_CUSTOM, PersistentDataType.STRING, "magic_wand");
        meta.getPersistentDataContainer().set(KEY_BOUND, PersistentDataType.STRING, "true");
        is.setItemMeta(meta);
        return is;
    }

    public ItemStack createThiefKnife() {
        ItemStack is = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = is.getItemMeta();
        meta.displayName(Component.text("§aНож вора"));
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(KEY_CUSTOM, PersistentDataType.STRING, "thief_knife");
        meta.getPersistentDataContainer().set(KEY_BOUND, PersistentDataType.STRING, "true");
        is.setItemMeta(meta);
        return is;
    }

    public boolean isBound(ItemStack is) {
        if (is == null || !is.hasItemMeta()) return false;
        return is.getItemMeta().getPersistentDataContainer()
                .has(KEY_BOUND, PersistentDataType.STRING);
    }

    public String getCustomId(ItemStack is) {
        if (is == null || !is.hasItemMeta()) return null;
        return is.getItemMeta().getPersistentDataContainer()
                .get(KEY_CUSTOM, PersistentDataType.STRING);
    }
}
