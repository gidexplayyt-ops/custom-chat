package com.yourname.customchat;

import com.yourname.customchat.config.ChatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ChatHistory {
    private static final List<ChatMessage> messages = new ArrayList<>();
    private static final List<String> sentMessages = new ArrayList<>();
    private static final int MAX_MESSAGES = 50;
    private static final int MAX_SENT_HISTORY = 100;
    private static int sentHistoryIndex = -1;
    
    // Кэш для избежания повторных вычислений
    private static long lastMessageTime = 0;
    
    public static void addMessage(Component content) {
        if (content == null) return;
        
        String text = content.getString();
        if (text == null || text.isEmpty()) return;
        
        String sender = "Система";
        String message = text;
        
        if (text.startsWith("<") && text.contains(">")) {
            int endBracket = text.indexOf(">");
            if (endBracket > 1) {
                sender = text.substring(1, endBracket);
                message = text.substring(endBracket + 1).trim();
                
                // Заменяем свой ник на кастомный
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    String playerName = mc.player.getName().getString();
                    if (sender.equals(playerName) && ChatConfig.hasCustomNickname()) {
                        sender = ChatConfig.getCustomNickname();
                    }
                }
            }
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Скрываем только если прошло меньше 100мс (оптимизация)
        if (currentTime - lastMessageTime > 100) {
            for (ChatMessage msg : messages) {
                msg.hideNow();
            }
        }
        lastMessageTime = currentTime;
        
        messages.add(0, new ChatMessage(sender, message, currentTime));
        
        // Удаляем старые сообщения
        while (messages.size() > MAX_MESSAGES) {
            messages.remove(messages.size() - 1);
        }
    }
    
    public static void addSentMessage(String message) {
        if (message == null || message.trim().isEmpty()) return;
        
        message = message.trim();
        
        // Не добавляем дубликаты подряд
        if (!sentMessages.isEmpty() && sentMessages.get(0).equals(message)) {
            resetHistoryIndex();
            return;
        }
        
        sentMessages.add(0, message);
        
        while (sentMessages.size() > MAX_SENT_HISTORY) {
            sentMessages.remove(sentMessages.size() - 1);
        }
        
        resetHistoryIndex();
    }
    
    public static void resetHistoryIndex() {
        sentHistoryIndex = -1;
    }
    
    public static String getPreviousMessage() {
        if (sentMessages.isEmpty()) return null;
        
        sentHistoryIndex++;
        if (sentHistoryIndex >= sentMessages.size()) {
            sentHistoryIndex = sentMessages.size() - 1;
        }
        
        return sentMessages.get(sentHistoryIndex);
    }
    
    public static String getNextMessage() {
        if (sentMessages.isEmpty() || sentHistoryIndex < 0) return "";
        
        sentHistoryIndex--;
        if (sentHistoryIndex < 0) {
            sentHistoryIndex = -1;
            return "";
        }
        
        return sentMessages.get(sentHistoryIndex);
    }
    
    public static void clearChat() {
        messages.clear();
    }
    
    public static List<ChatMessage> getMessages() {
        return messages;
    }
    
    public static List<ChatMessage> getRecentMessages(int count) {
        int size = Math.min(count, messages.size());
        if (size == 0) return new ArrayList<>();
        return new ArrayList<>(messages.subList(0, size));
    }
    
    public static boolean hasMessages() {
        return !messages.isEmpty();
    }
    
    public static class ChatMessage {
        public final String sender;
        public final String message;
        private long timestamp;
        private boolean hidden = false;
        
        public ChatMessage(String sender, String message, long timestamp) {
            this.sender = sender;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public void hideNow() {
            this.hidden = true;
        }
        
        public boolean isRecent() {
            if (hidden) return false;
            int duration = ChatConfig.getMessageDuration() * 1000;
            return System.currentTimeMillis() - timestamp < duration;
        }
        
        public long getAge() {
            return System.currentTimeMillis() - timestamp;
        }
    }
}