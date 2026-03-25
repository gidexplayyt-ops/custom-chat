package com.yourname.customchat.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path CONFIG_DIR;
    private static Path CONFIG_FILE;
    
    private static ConfigData config = new ConfigData();
    private static final Map<String, String> nameColors = new HashMap<>();
    
    // Отложенное сохранение
    private static boolean needsSave = false;
    private static final ScheduledExecutorService saveExecutor = Executors.newSingleThreadScheduledExecutor();
    
    private static boolean initialized = false;
    
    public static class ConfigData {
        public String customNickname = null;
        public String nicknameColor = "§f";
        public int chatPositionX = 50;
        public int chatPositionY = 85;
        public boolean showPlayerHeads = true;
        public int messageDuration = 10;
        public int headSize = 16;
        public String speedrunGoal = "Убить Дракона";
    }
    
    private static void ensureInitialized() {
        if (!initialized) {
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc != null && mc.gameDirectory != null) {
                    CONFIG_DIR = Paths.get(mc.gameDirectory.getPath(), "custom-chat");
                    CONFIG_FILE = CONFIG_DIR.resolve("config.json");
                    
                    // Добавляем дефолтные цвета
                    nameColors.put("Торговец", "§6");
                    nameColors.put("Стражник", "§c");
                    nameColors.put("Мудрец", "§5");
                    nameColors.put("Квестодатель", "§b");
                    nameColors.put("Жрец", "§e");
                    nameColors.put("Кузнец", "§7");
                    
                    loadConfig();
                    
                    // Запускаем автосохранение каждые 5 секунд
                    saveExecutor.scheduleAtFixedRate(() -> {
                        if (needsSave) {
                            saveConfigNow();
                            needsSave = false;
                        }
                    }, 5, 5, TimeUnit.SECONDS);
                    
                    initialized = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static Path getConfigDir() {
        ensureInitialized();
        return CONFIG_DIR;
    }
    
    public static Path getHeadsDir() {
        ensureInitialized();
        return CONFIG_DIR != null ? CONFIG_DIR.resolve("heads") : null;
    }
    
    public static Path getCustomizationDir() {
        ensureInitialized();
        return CONFIG_DIR != null ? CONFIG_DIR.resolve("customization_chat") : null;
    }
    
    public static void loadConfig() {
        try {
            if (CONFIG_DIR == null) return;
            
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            
            // Создаём папку heads
            Path headsDir = getHeadsDir();
            if (headsDir != null && !Files.exists(headsDir)) {
                Files.createDirectories(headsDir);
            }
            
            // Создаём папку customization_chat (в разработке)
            Path customizationDir = getCustomizationDir();
            if (customizationDir != null && !Files.exists(customizationDir)) {
                Files.createDirectories(customizationDir);
                createCustomizationReadme(customizationDir);
            }
            
            if (CONFIG_FILE != null && Files.exists(CONFIG_FILE)) {
                try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                    ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
                    if (loaded != null) {
                        config = loaded;
                    }
                }
            } else {
                saveConfigNow();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void createCustomizationReadme(Path dir) {
        try {
            Path readme = dir.resolve("README.txt");
            String content = """
                ========================================
                CUSTOMIZATION CHAT - В РАЗРАБОТКЕ
                ========================================
                
                Эта папка будет использоваться для 
                кастомизации чата в будущих версиях.
                
                Планируемые функции:
                - Кастомные темы чата
                - Кастомные шрифты
                - Кастомные звуки
                - Кастомные анимации
                
                Следите за обновлениями!
                
                Автор: GidexPlayYT
                GitHub: github.com/gidexplayyt-ops/custom-chat
                ========================================
                """;
            Files.writeString(readme, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void saveConfigNow() {
        try {
            if (CONFIG_DIR == null || CONFIG_FILE == null) return;
            
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void saveConfig() {
        needsSave = true;
    }
    
    public static void reload() {
        loadConfig();
        HeadConfig.reload();
    }
    
    // Позиция чата
    public static int getChatPositionX() {
        ensureInitialized();
        return config.chatPositionX;
    }
    
    public static void setChatPositionX(int x) {
        ensureInitialized();
        config.chatPositionX = Math.max(0, Math.min(100, x));
        saveConfig();
    }
    
    public static int getChatPositionY() {
        ensureInitialized();
        return config.chatPositionY;
    }
    
    public static void setChatPositionY(int y) {
        ensureInitialized();
        config.chatPositionY = Math.max(0, Math.min(100, y));
        saveConfig();
    }
    
    // Размер головы
    public static int getHeadSize() {
        ensureInitialized();
        return config.headSize;
    }
    
    public static void setHeadSize(int size) {
        ensureInitialized();
        config.headSize = Math.max(8, Math.min(32, size));
        saveConfig();
    }
    
    // Показывать головы
    public static boolean showPlayerHeads() {
        ensureInitialized();
        return config.showPlayerHeads;
    }
    
    public static void setShowPlayerHeads(boolean show) {
        ensureInitialized();
        config.showPlayerHeads = show;
        saveConfig();
    }
    
    // Длительность сообщения
    public static int getMessageDuration() {
        ensureInitialized();
        return config.messageDuration;
    }
    
    public static void setMessageDuration(int seconds) {
        ensureInitialized();
        config.messageDuration = Math.max(1, Math.min(60, seconds));
        saveConfig();
    }
    
    // Ник
    public static void setCustomNickname(String nickname) {
        ensureInitialized();
        config.customNickname = nickname;
        saveConfig();
    }
    
    public static String getCustomNickname() {
        ensureInitialized();
        return config.customNickname;
    }
    
    public static boolean hasCustomNickname() {
        ensureInitialized();
        return config.customNickname != null && !config.customNickname.isEmpty();
    }
    
    public static void clearCustomNickname() {
        ensureInitialized();
        config.customNickname = null;
        saveConfig();
    }
    
    // Цвет ника
    public static void setNicknameColor(String colorCode) {
        ensureInitialized();
        config.nicknameColor = colorCode;
        saveConfig();
    }
    
    public static String getNicknameColor() {
        ensureInitialized();
        return config.nicknameColor;
    }
    
    // Speedrun Goal
    public static String getSpeedrunGoal() {
        ensureInitialized();
        return config.speedrunGoal;
    }
    
    public static void setSpeedrunGoal(String goal) {
        ensureInitialized();
        config.speedrunGoal = goal;
        saveConfig();
    }
    
    // Цвета NPC
    public static String getNameColor(String name) {
        ensureInitialized();
        return nameColors.getOrDefault(name, "§f");
    }
    
    public static void setNameColor(String name, String colorCode) {
        ensureInitialized();
        nameColors.put(name, colorCode);
    }
    
    public static String parseColorName(String colorName) {
        if (colorName == null) return "§f";
        
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
    
    // Вызывается при закрытии игры
    public static void shutdown() {
        if (needsSave) {
            saveConfigNow();
        }
        saveExecutor.shutdown();
    }
}