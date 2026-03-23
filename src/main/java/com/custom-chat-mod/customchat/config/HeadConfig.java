package com.yourname.customchat.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class HeadConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path HEADS_FILE = ChatConfig.getConfigDir().resolve("heads.json");
    
    private static Map<String, String> npcHeads = new HashMap<>();
    private static Map<String, ResourceLocation> customTextures = new HashMap<>();
    
    static {
        loadHeads();
    }
    
    public static void loadHeads() {
        try {
            Path configDir = ChatConfig.getConfigDir();
            Path headsDir = ChatConfig.getHeadsDir();
            
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            
            if (!Files.exists(headsDir)) {
                Files.createDirectories(headsDir);
            }
            
            // Загружаем JSON конфиг
            if (Files.exists(HEADS_FILE)) {
                Reader reader = Files.newBufferedReader(HEADS_FILE);
                npcHeads = GSON.fromJson(reader, new TypeToken<Map<String, String>>(){}.getType());
                reader.close();
                if (npcHeads == null) {
                    npcHeads = new HashMap<>();
                }
            } else {
                // Примеры по умолчанию
                npcHeads.put("Торговец", "villager.png");
                npcHeads.put("Стражник", "guard.png");
                npcHeads.put("Мудрец", "wizard.png");
                npcHeads.put("Система", "system.png");
                saveHeads();
            }
            
            // Загружаем PNG файлы из папки heads
            loadCustomTextures();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void loadCustomTextures() {
        try {
            Path headsDir = ChatConfig.getHeadsDir();
            if (!Files.exists(headsDir)) return;
            
            // Очищаем старые текстуры
            customTextures.clear();
            
            Files.list(headsDir)
                .filter(path -> path.toString().toLowerCase().endsWith(".png"))
                .forEach(path -> {
                    try {
                        String fileName = path.getFileName().toString();
                        NativeImage image = NativeImage.read(Files.newInputStream(path));
                        DynamicTexture texture = new DynamicTexture(image);
                        
                        ResourceLocation location = Minecraft.getInstance()
                            .getTextureManager()
                            .register("customchat_head_" + fileName.replace(".png", ""), texture);
                        
                        customTextures.put(fileName, location);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void saveHeads() {
        try {
            Path configDir = ChatConfig.getConfigDir();
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            
            Writer writer = Files.newBufferedWriter(HEADS_FILE);
            GSON.toJson(npcHeads, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void reload() {
        loadHeads();
    }
    
    public static String getHeadFile(String name) {
        return npcHeads.getOrDefault(name, null);
    }
    
    public static ResourceLocation getCustomTexture(String fileName) {
        return customTextures.get(fileName);
    }
    
    public static boolean hasCustomTexture(String name) {
        String fileName = npcHeads.get(name);
        if (fileName == null) return false;
        return customTextures.containsKey(fileName);
    }
    
    public static ResourceLocation getTextureForName(String name) {
        String fileName = npcHeads.get(name);
        if (fileName != null && customTextures.containsKey(fileName)) {
            return customTextures.get(fileName);
        }
        return null;
    }
    
    public static void setHeadFile(String name, String fileName) {
        npcHeads.put(name, fileName);
        saveHeads();
    }
    
    public static void removeHead(String name) {
        npcHeads.remove(name);
        saveHeads();
    }
    
    public static Map<String, String> getAllHeads() {
        return new HashMap<>(npcHeads);
    }
}