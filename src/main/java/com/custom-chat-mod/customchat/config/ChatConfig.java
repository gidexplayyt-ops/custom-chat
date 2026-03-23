package com.yourname.customchat.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class ChatConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "custom-chat");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");
    
    private static ConfigData config = new ConfigData();
    private static final Map<String, String> nameColors = new HashMap<>();
    
    static {
        nameColors.put("Торговец", "§6");
        nameColors.put("Стражник", "§c");
        nameColors.put("Мудрец", "§5");
        nameColors.put("Квестодатель", "§b");
        nameColors.put("Жрец", "§e");
        nameColors.put("Кузнец", "§7");
        
        loadConfig();
    }
    
    public static class ConfigData {
        public String customNickname = null;
        public String nicknameColor = "§f";
        public int chatPositionX = 50;
        public int chatPositionY = 85;
        public boolean showPlayerHeads = true;
        public int messageDuration = 10;
        public int headSize = 16; // Размер головы
        public String speedrunGoal = "Убить Дракона";
    }
    
    public static Path getConfigDir() {
        return CONFIG_DIR;
    }
    
    public static Path getHeadsDir() {
        return CONFIG_DIR.resolve("heads");
    }
    
    public static void loadConfig() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            
            // Создаём папку heads
            Path headsDir = getHeadsDir();
            if (!Files.exists(headsDir)) {
                Files.createDirectories(headsDir);
            }
            
            if (Files.exists(CONFIG_FILE)) {
                Reader reader = Files.newBufferedReader(CONFIG_FILE);
                config = GSON.fromJson(reader, ConfigData.class);
                reader.close();
                if (config == null) config = new ConfigData();
            } else {
                saveConfig();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void saveConfig() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            
            Writer writer = Files.newBufferedWriter(CONFIG_FILE);
            GSON.toJson(config, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void reload() {
        loadConfig();
        HeadConfig.loadHeads();
    }
    
    // Позиция чата
    public static int getChatPositionX() {
        return config.chatPositionX;
    }
    
    public static void setChatPositionX(int x) {
        config.chatPositionX = Math.max(0, Math.min(100, x));
        saveConfig();
    }
    
    public static int getChatPositionY() {
        return config.chatPositionY;
    }
    
    public static void setChatPositionY(int y) {
        config.chatPositionY = Math.max(0, Math.min(100, y));
        saveConfig();
    }
    
    // Размер головы
    public static int getHeadSize() {
        return config.headSize;
    }
    
    public static void setHeadSize(int size) {
        config.headSize = Math.max(8, Math.min(32, size));
        saveConfig();
    }
    
    // Показывать головы
    public static boolean showPlayerHeads() {
        return config.showPlayerHeads;
    }
    
    public static void setShowPlayerHeads(boolean show) {
        config.showPlayerHeads = show;
        saveConfig();
    }
    
    // Длительность сообщения
    public static int getMessageDuration() {
        return config.messageDuration;
    }
    
    public static void setMessageDuration(int seconds) {
        config.messageDuration = Math.max(1, Math.min(60, seconds));
        saveConfig();
    }
    
    // Ник
    public static void setCustomNickname(String nickname) {
        config.customNickname = nickname;
        saveConfig();
    }
    
    public static String getCustomNickname() {
        return config.customNickname;
    }
    
    public static boolean hasCustomNickname() {
        return config.customNickname != null && !config.customNickname.isEmpty();
    }
    
    public static void clearCustomNickname() {
        config.customNickname = null;
        saveConfig();
    }
    
    // Цвет ника
    public static void setNicknameColor(String colorCode) {
        config.nicknameColor = colorCode;
        saveConfig();
    }
    
    public static String getNicknameColor() {
        return config.nicknameColor;
    }
    
    // Speedrun Goal
    public static String getSpeedrunGoal() {
        return config.speedrunGoal;
    }
    
    public static void setSpeedrunGoal(String goal) {
        config.speedrunGoal = goal;
        saveConfig();
    }
    
    // Цвета NPC
    public static String getNameColor(String name) {
        return nameColors.getOrDefault(name, "§f");
    }
    
    public static void setNameColor(String name, String colorCode) {
        nameColors.put(name, colorCode);
    }
    
    public static String parseColorName(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "red" -> "§c";
            case "dark_red" -> "§4";
            case "blue" -> "§9";
            case "aqua" -> "§b";
            case "green" -> "§a";
            case "dark_green" -> "§2";
            case "yellow" -> "§e";
            case "gold" -> "§6";
            case "purple" -> "§5";
            case "light_purple" -> "§d";
            case "gray" -> "§7";
            case "dark_gray" -> "§8";
            case "white" -> "§f";
            case "black" -> "§0";
            default -> "§f";
        };
    }
}