package com.server.rpg.auction;

import com.server.rpg.RPGPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class AuctionGUI implements Listener {
    public static final String TITLE_MAIN = "§8Аукцион";
    public static final String TITLE_ARCHIVE = "§8Архив";
    public static final String TITLE_MY = "§8Мои товары";
    public static final String TITLE_CONFIRM = "§8Подтверждение покупки";

    private final RPGPlugin plugin;
    private final Map<UUID, Integer> pageMap = new HashMap<>();
    private final Map<UUID, SortType> sortMap = new HashMap<>();
    private final Map<UUID, AuctionItem> pendingBuy = new HashMap<>();

    public enum SortType {
        NEW("§aНовые"), OLD("§6Старые"), ARMOR("§bБроня"),
        WEAPON("§cОружие"), TOOL("§eИнструменты"), CUSTOM("§dКастомные"),
        BOOK("§5Книги"), BLOCK("§7Блоки"), MISC("§fРазное");
        final String name;
        SortType(String n) { this.name = n; }
    }

    public AuctionGUI(RPGPlugin plugin) { this.plugin = plugin; }

    public void openMain(Player p) {
        int page = pageMap.getOrDefault(p.getUniqueId(), 0);
        SortType sort = sortMap.getOrDefault(p.getUniqueId(), SortType.NEW);

        List<AuctionItem> items = new ArrayList<>(plugin.getAuctionManager().getActive());
        items = applySort(items, sort);

        int perPage = 45;
        int maxPage = Math.max(1, (int) Math.ceil(items.size() / (double) perPage));
        if (page >= maxPage) page = maxPage - 1;
        if (page < 0) page = 0;
        pageMap.put(p.getUniqueId(), page);

        Inventory inv = Bukkit.createInventory(null, 54, Component.text(TITLE_MAIN));

        int start = page * perPage;
        for (int i = 0; i < perPage && start + i < items.size(); i++) {
            AuctionItem ai = items.get(start + i);
            ItemStack display = ai.getItem().clone();
            ItemMeta meta = display.getItemMeta();
            List<Component> lore = (meta.hasLore() && meta.lore() != null)
                    ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("§6Цена: §e" + ai.getPrice() + " монет"));
            lore.add(Component.text("§7Продавец: §f" + ai.getSellerName()));
            lore.add(Component.text("§aПКМ — купить"));
            meta.lore(lore);
            display.setItemMeta(meta);
            inv.setItem(i, display);
        }

        inv.setItem(45, makeBtn(Material.ARROW, "§eНазад", "§7Стр. " + (page) + "/" + maxPage));
        inv.setItem(46, makeBtn(Material.CHEST, "§6Архив", "§7Снятые предметы"));
        inv.setItem(47, makeBtn(Material.PLAYER_HEAD, "§bМои товары", "§7Управление"));
        inv.setItem(48, makeBtn(Material.ARROW, "§eВперёд", "§7Стр. " + (page + 2) + "/" + maxPage));
        inv.setItem(49, makeBtn(Material.HOPPER, "§dСортировка", "§7Текущая: " + sort.name));
        inv.setItem(53, makeBtn(Material.BARRIER, "§cЗакрыть", ""));

        p.openInventory(inv);
    }

    private List<AuctionItem> applySort(List<AuctionItem> list, SortType sort) {
        switch (sort) {
            case NEW -> list.sort(Comparator.comparingLong(AuctionItem::getListedAt).reversed());
            case OLD -> list.sort(Comparator.comparingLong(AuctionItem::getListedAt));
            case ARMOR, WEAPON, TOOL, CUSTOM, BOOK, BLOCK, MISC -> {
                list.removeIf(ai -> !matchesCategory(ai.getItem(), sort));
            }
        }
        return list;
    }

    private boolean matchesCategory(ItemStack is, SortType sort) {
        String n = is.getType().name();
        return switch (sort) {
            case ARMOR -> n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE")
                    || n.endsWith("_LEGGINGS") || n.endsWith("_BOOTS");
            case WEAPON -> n.endsWith("_SWORD") || n.endsWith("_AXE")
                    || n.contains("BOW") || n.contains("TRIDENT") || n.equals("MACE");
            case TOOL -> n.endsWith("_PICKAXE") || n.endsWith("_SHOVEL")
                    || n.endsWith("_HOE") || n.contains("SHEARS") || n.contains("FISHING");
            case BOOK -> n.contains("BOOK") || n.contains("ENCHANT");
            case BLOCK -> is.getType().isBlock();
            case CUSTOM -> is.hasItemMeta()
                    && is.getItemMeta().getPersistentDataContainer().has(
                            plugin.getItemFactory().KEY_CUSTOM, PersistentDataType.STRING);
            default -> true;
        };
    }

    public void openArchive(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(TITLE_ARCHIVE));
        List<AuctionItem> arc = plugin.getAuctionManager().getArchive(p.getUniqueId());
        for (int i = 0; i < 45 && i < arc.size(); i++) {
            AuctionItem ai = arc.get(i);
            ItemStack d = ai.getItem().clone();
            ItemMeta meta = d.getItemMeta();
            List<Component> lore = (meta.hasLore() && meta.lore() != null)
                    ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Component.text("§eЛКМ — забрать"));
            meta.lore(lore);
            d.setItemMeta(meta);
            inv.setItem(i, d);
        }
        inv.setItem(49, makeBtn(Material.ARROW, "§aНазад", "§7В аукцион"));
        p.openInventory(inv);
    }

    public void openMyItems(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(TITLE_MY));
        List<AuctionItem> mine = new ArrayList<>();
        for (AuctionItem ai : plugin.getAuctionManager().getActive()) {
            if (ai.getSeller().equals(p.getUniqueId())) mine.add(ai);
        }
        for (int i = 0; i < 45 && i < mine.size(); i++) {
            AuctionItem ai = mine.get(i);
            ItemStack d = ai.getItem().clone();
            ItemMeta meta = d.getItemMeta();
            List<Component> lore = (meta.hasLore() && meta.lore() != null)
                    ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("§6Цена: §e" + ai.getPrice()));
            lore.add(Component.text("§cЛКМ — снять с продажи"));
            meta.lore(lore);
            d.setItemMeta(meta);
            inv.setItem(i, d);
        }
        inv.setItem(49, makeBtn(Material.ARROW, "§aНазад", "§7В аукцион"));
        p.openInventory(inv);
    }

    private ItemStack makeBtn(Material mat, String name, String lore) {
        ItemStack is = new ItemStack(mat);
        ItemMeta m = is.getItemMeta();
        m.setDisplayName(name);
        if (!lore.isEmpty()) m.setLore(List.of(lore));
        is.setItemMeta(m);
        return is;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (!title.startsWith("§8")) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;
        e.setCancelled(true);

        int slot = e.getRawSlot();
        boolean right = e.isRightClick();

        if (title.equals(TITLE_MAIN)) {
            if (slot == 45) {
                pageMap.put(p.getUniqueId(), Math.max(0, pageMap.getOrDefault(p.getUniqueId(), 0) - 1));
                openMain(p);
            } else if (slot == 46) openArchive(p);
            else if (slot == 47) openMyItems(p);
            else if (slot == 48) {
                pageMap.put(p.getUniqueId(), pageMap.getOrDefault(p.getUniqueId(), 0) + 1);
                openMain(p);
            } else if (slot == 49) {
                SortType cur = sortMap.getOrDefault(p.getUniqueId(), SortType.NEW);
                SortType[] all = SortType.values();
                sortMap.put(p.getUniqueId(), all[(cur.ordinal() + 1) % all.length]);
                openMain(p);
            } else if (slot == 53) p.closeInventory();
            else if (slot >= 0 && slot < 45) {
                AuctionItem ai = getItemAt(p, slot);
                if (ai != null && right) openConfirm(p, ai);
            }
            return;
        }

        if (title.equals(TITLE_ARCHIVE)) {
            if (slot == 49) { openMain(p); return; }
            if (slot >= 0 && slot < 45) {
                List<AuctionItem> arc = plugin.getAuctionManager().getArchive(p.getUniqueId());
                if (slot >= arc.size()) return;
                AuctionItem ai = arc.remove(slot);
                if (p.getInventory().firstEmpty() == -1)
                    p.getWorld().dropItem(p.getLocation(), ai.getItem());
                else p.getInventory().addItem(ai.getItem());
                p.sendMessage("§aЗабрано из архива.");
                plugin.getAuctionManager().save();
                openArchive(p);
            }
            return;
        }

        if (title.equals(TITLE_MY)) {
            if (slot == 49) { openMain(p); return; }
            if (slot >= 0 && slot < 45) {
                List<AuctionItem> mine = new ArrayList<>();
                for (AuctionItem ai : plugin.getAuctionManager().getActive())
                    if (ai.getSeller().equals(p.getUniqueId())) mine.add(ai);
                if (slot >= mine.size()) return;
                AuctionItem ai = mine.get(slot);
                plugin.getAuctionManager().archiveItem(ai);
                p.sendMessage("§eПредмет снят с продажи и перемещён в архив.");
                openMyItems(p);
            }
            return;
        }

        if (title.equals(TITLE_CONFIRM)) {
            AuctionItem ai = pendingBuy.get(p.getUniqueId());
            if (ai == null) { p.closeInventory(); return; }
            if (slot == 11) {
                var pc = plugin.getDataManager().getPlayer(p.getUniqueId());
                if (pc == null) return;
                if (pc.getCoins() < ai.getPrice()) {
                    p.sendMessage("§cНедостаточно монет!");
                    p.closeInventory();
                    return;
                }
                if (!plugin.getAuctionManager().getActive().contains(ai)) {
                    p.sendMessage("§cЭтот товар уже куплен.");
                    p.closeInventory();
                    return;
                }
                pc.setCoins(pc.getCoins() - ai.getPrice());
                var seller = plugin.getDataManager().getPlayer(ai.getSeller());
                if (seller != null) {
                    seller.addCoins(ai.getPrice());
                    plugin.getDataManager().savePlayer(ai.getSeller());
                }
                if (p.getInventory().firstEmpty() == -1)
                    p.getWorld().dropItem(p.getLocation(), ai.getItem());
                else p.getInventory().addItem(ai.getItem());

                plugin.getAuctionManager().removeItem(ai);
                plugin.getDataManager().savePlayer(p.getUniqueId());
                p.sendMessage("§aПокупка успешна! §6-" + ai.getPrice() + " монет");
                pendingBuy.remove(p.getUniqueId());
                p.closeInventory();
            } else if (slot == 15) {
                pendingBuy.remove(p.getUniqueId());
                openMain(p);
            }
        }
    }

    private AuctionItem getItemAt(Player p, int slot) {
        int page = pageMap.getOrDefault(p.getUniqueId(), 0);
        SortType sort = sortMap.getOrDefault(p.getUniqueId(), SortType.NEW);
        List<AuctionItem> items = applySort(new ArrayList<>(plugin.getAuctionManager().getActive()), sort);
        int idx = page * 45 + slot;
        if (idx < 0 || idx >= items.size()) return null;
        return items.get(idx);
    }

    private void openConfirm(Player p, AuctionItem ai) {
        pendingBuy.put(p.getUniqueId(), ai);
        Inventory inv = Bukkit.createInventory(null, 27, Component.text(TITLE_CONFIRM));
        inv.setItem(11, makeBtn(Material.LIME_CONCRETE, "§aКупить за " + ai.getPrice(),
                "§7ПКМ — подтвердить"));
        inv.setItem(13, ai.getItem().clone());
        inv.setItem(15, makeBtn(Material.RED_CONCRETE, "§cОтмена", "§7Назад"));
        p.openInventory(inv);
    }
}
