package com.server.rpg.classes;

public class PlayerClass {
    private ClassType type;
    private int level;
    private int experience;
    private int coins;
    private int deathCount;

    public PlayerClass(ClassType type) {
        this.type = type;
        this.level = 1;
        this.experience = 0;
        this.coins = 10000;
        this.deathCount = 0;
    }

    public static int expForNextLevel(int level) {
        return switch (level) {
            case 1 -> 3000;
            case 2 -> 4000;
            case 3 -> 5000;
            case 4 -> 6000;
            default -> Integer.MAX_VALUE;
        };
    }

    public boolean tryLevelUp() {
        if (level >= 5) return false;
        if (experience >= expForNextLevel(level)) {
            experience -= expForNextLevel(level);
            level++;
            return true;
        }
        return false;
    }

    public ClassType getType() { return type; }
    public void setType(ClassType type) { this.type = type; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }
    public void addExperience(int amount) { this.experience += amount; }
    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
    public void addCoins(int amount) { this.coins += amount; }
    public int getDeathCount() { return deathCount; }
    public void setDeathCount(int deathCount) { this.deathCount = deathCount; }
    public void addDeath() { this.deathCount++; }
}
