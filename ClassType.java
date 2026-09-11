package com.server.rpg.classes;

public enum ClassType {
    KNIGHT("Рыцарь", "§c"),
    MAGE("Маг", "§9"),
    THIEF("Вор", "§a");

    private final String display;
    private final String color;

    ClassType(String display, String color) {
        this.display = display;
        this.color = color;
    }

    public String getDisplay() { return display; }
    public String getColor() { return color; }
}
