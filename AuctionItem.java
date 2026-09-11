package com.server.rpg.auction;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AuctionItem {
    private final UUID seller;
    private final ItemStack item;
    private final int price;
    private final long listedAt;

    public AuctionItem(UUID seller, ItemStack item, int price) {
        this.seller = seller;
        this.item = item;
        this.price = price;
        this.listedAt = System.currentTimeMillis();
    }

    public AuctionItem(UUID seller, ItemStack item, int price, long listedAt) {
        this.seller = seller;
        this.item = item;
        this.price = price;
        this.listedAt = listedAt;
    }

    public UUID getSeller() { return seller; }
    public ItemStack getItem() { return item; }
    public int getPrice() { return price; }
    public long getListedAt() { return listedAt; }

    public boolean isExpired() {
        return System.currentTimeMillis() - listedAt > 14L * 24 * 60 * 60 * 1000;
    }

    public String getSellerName() {
        return org.bukkit.Bukkit.getOfflinePlayer(seller).getName();
    }
}
