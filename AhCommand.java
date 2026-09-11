package com.server.rpg.commands;

import com.server.rpg.RPGPlugin;
import com.server.rpg.auction.AuctionGUI;
import com.server.rpg.auction.AuctionItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AhCommand implements CommandExecutor {
    private final RPGPlugin plugin;
    private final AuctionGUI gui;

    public AhCommand(RPGPlugin plugin) {
        this.plugin = plugin;
        this.gui = new AuctionGUI(plugin);
        plugin.getServer().getPluginManager().registerEvents(gui, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cТолько для игроков.");
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("sell")) {
            if (args.length < 3) {
                p.sendMessage("§cИспользование: /ah sell <цена> <кол-во>");
                return true;
            }
            int price, amount;
            try {
                price = Integer.parseInt(args[1]);
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                p.sendMessage("§cНеверные числа.");
                return true;
            }
            if (price <= 0 || amount <= 0) {
                p.sendMessage("§cЧисла должны быть > 0.");
                return true;
            }

            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType().isAir()) {
                p.sendMessage("§cВозьми предмет в руку.");
                return true;
            }
            if (amount > hand.getAmount()) {
                p.sendMessage("§cУ тебя только " + hand.getAmount() + " шт.");
                return true;
            }

            if (plugin.getItemFactory().isBound(hand)) {
                p.sendMessage("§cЭтот предмет нельзя продать!");
                return true;
            }

            ItemStack toSell = hand.clone();
            toSell.setAmount(amount);
            hand.setAmount(hand.getAmount() - amount);

            plugin.getAuctionManager().addItem(new AuctionItem(p.getUniqueId(), toSell, price));
            p.sendMessage("§aВыставлено на аукцион: §e" + amount + " шт. за §6" + price + " монет");
            return true;
        }

        gui.openMain(p);
        return true;
    }
}
