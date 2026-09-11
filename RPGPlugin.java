package com.server.rpg;

import com.server.rpg.auction.AuctionManager;
import com.server.rpg.classes.ClassManager;
import com.server.rpg.commands.AhCommand;
import com.server.rpg.commands.PayCommand;
import com.server.rpg.data.DataManager;
import com.server.rpg.items.ItemFactory;
import com.server.rpg.listeners.*;
import com.server.rpg.loot.LootManager;
import com.server.rpg.util.CooldownManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RPGPlugin extends JavaPlugin {

    private static RPGPlugin instance;
    private DataManager dataManager;
    private ClassManager classManager;
    private AuctionManager auctionManager;
    private LootManager lootManager;
    private CooldownManager cooldownManager;
    private ItemFactory itemFactory;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.dataManager = new DataManager(this);
        this.classManager = new ClassManager(this);
        this.auctionManager = new AuctionManager(this);
        this.lootManager = new LootManager(this);
        this.cooldownManager = new CooldownManager();
        this.itemFactory = new ItemFactory(this);

        getCommand("ah").setExecutor(new AhCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ClassSelectGUI(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new AbilityListener(this), this);

        lootManager.startChestGenerator();
        getServer().getScheduler().runTaskTimer(this, () -> auctionManager.checkExpired(), 1200L, 1200L);

        getLogger().info("RPGPlugin включен!");
    }

    @Override
    public void onDisable() {
        dataManager.saveAll();
        auctionManager.save();
    }

    public static RPGPlugin getInstance() { return instance; }
    public DataManager getDataManager() { return dataManager; }
    public ClassManager getClassManager() { return classManager; }
    public AuctionManager getAuctionManager() { return auctionManager; }
    public LootManager getLootManager() { return lootManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public ItemFactory getItemFactory() { return itemFactory; }
}
