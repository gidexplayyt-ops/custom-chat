package com.yourname.customchat.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yourname.customchat.ChatHistory;
import com.yourname.customchat.SpeedrunManager;
import com.yourname.customchat.config.ChatConfig;
import com.yourname.customchat.config.HeadConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ChatHudOverlay {
    private static final int MAX_VISIBLE = 1;
    
    // Кэш для избежания повторных вычислений
    private static int cachedScreenWidth = 0;
    private static int cachedScreenHeight = 0;
    private static int cachedPosX = 0;
    private static int cachedPosY = 0;
    
    public static void render(PoseStack poseStack, Minecraft mc) {
        if (mc == null || mc.player == null || mc.options.hideGui) return;
        
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        // Обновляем кэш позиции только если размер экрана изменился
        if (screenWidth != cachedScreenWidth || screenHeight != cachedScreenHeight) {
            cachedScreenWidth = screenWidth;
            cachedScreenHeight = screenHeight;
            updateCachedPosition(screenWidth, screenHeight);
        }
        
        if (SpeedrunManager.isActive()) {
            renderSpeedrunTimer(poseStack, mc, cachedPosX, cachedPosY);
        } else if (ChatHistory.hasMessages()) {
            renderChat(poseStack, mc, cachedPosX, cachedPosY, screenWidth, screenHeight);
        }
    }
    
    private static void updateCachedPosition(int screenWidth, int screenHeight) {
        cachedPosX = (int) (screenWidth * ChatConfig.getChatPositionX() / 100.0);
        cachedPosY = (int) (screenHeight * ChatConfig.getChatPositionY() / 100.0);
    }
    
    public static void invalidateCache() {
        cachedScreenWidth = 0;
        cachedScreenHeight = 0;
    }
    
    private static void renderSpeedrunTimer(PoseStack poseStack, Minecraft mc, int posX, int posY) {
        boolean completed = SpeedrunManager.isCompleted();
        
        int boxWidth = 220;
        int boxHeight = completed ? 85 : 70;
        int boxX = posX - boxWidth / 2;
        int boxY = posY - boxHeight / 2;
        
        // Фон
        int bgColor = completed ? 0xCC004400 : 0xCC000000;
        GuiComponent.fill(poseStack, boxX, boxY, boxX + boxWidth, boxY + boxHeight, bgColor);
        
        // Рамка если завершён
        if (completed) {
            int borderColor = 0xFF00FF00;
            GuiComponent.fill(poseStack, boxX - 2, boxY - 2, boxX + boxWidth + 2, boxY, borderColor);
            GuiComponent.fill(poseStack, boxX - 2, boxY + boxHeight, boxX + boxWidth + 2, boxY + boxHeight + 2, borderColor);
            GuiComponent.fill(poseStack, boxX - 2, boxY, boxX, boxY + boxHeight, borderColor);
            GuiComponent.fill(poseStack, boxX + boxWidth, boxY, boxX + boxWidth + 2, boxY + boxHeight, borderColor);
        }
        
        // Таймер
        String time = SpeedrunManager.getFormattedTime();
        String timePrefix = completed ? "§a§l" : "§f§l";
        drawCenteredString(poseStack, mc, timePrefix + time, posX, boxY + 8);
        
        // Статус
        if (completed) {
            drawCenteredString(poseStack, mc, "§a§l✓ ЗАВЕРШЕНО!", posX, boxY + 25);
        }
        
        // Цель
        String goal = SpeedrunManager.getGoalDisplayName();
        int goalY = completed ? boxY + 42 : boxY + 28;
        drawCenteredString(poseStack, mc, "§7" + goal, posX, goalY);
        
        // Полоса прогресса
        int barWidth = 180;
        int barHeight = 8;
        int barX = posX - barWidth / 2;
        int barY = completed ? boxY + 58 : boxY + 45;
        
        GuiComponent.fill(poseStack, barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        
        float progressPercent = SpeedrunManager.getProgressPercent();
        int fillWidth = (int) (barWidth * progressPercent);
        int fillColor = completed ? 0xFF00FF00 : 0xFF00AAFF;
        GuiComponent.fill(poseStack, barX, barY, barX + fillWidth, barY + barHeight, fillColor);
        
        // Прогресс текст
        String progress = SpeedrunManager.getProgressText();
        int progressY = completed ? boxY + 70 : boxY + 56;
        drawCenteredString(poseStack, mc, progress, posX, progressY);
        
        // Подсказка
        if (completed) {
            mc.font.drawShadow(poseStack, "§8/chat speedrun - скрыть", 
                posX - mc.font.width("/chat speedrun - скрыть") / 2f, 
                boxY + boxHeight + 5, 0x666666);
        }
    }
    
    private static void renderChat(PoseStack poseStack, Minecraft mc, int posX, int posY, int screenWidth, int screenHeight) {
        List<ChatHistory.ChatMessage> messages = ChatHistory.getRecentMessages(MAX_VISIBLE);
        if (messages.isEmpty()) return;
        
        // Проверяем есть ли видимые сообщения
        ChatHistory.ChatMessage visibleMsg = null;
        for (ChatHistory.ChatMessage msg : messages) {
            if (msg.isRecent()) {
                visibleMsg = msg;
                break;
            }
        }
        
        if (visibleMsg == null) return;
        
        int headSize = ChatConfig.getHeadSize();
        int padding = 8;
        int lineHeight = Math.max(headSize + 4, 20);
        
        String colorCode = getColorForSender(visibleMsg.sender, mc);
        String formattedText = colorCode + visibleMsg.sender + "§7: §f" + visibleMsg.message;
        int textWidth = mc.font.width(formattedText);
        
        int headSpace = ChatConfig.showPlayerHeads() ? headSize + 6 : 0;
        int boxWidth = textWidth + headSpace + padding * 2;
        boxWidth = Math.max(150, Math.min(boxWidth, screenWidth - 40));
        
        int boxHeight = lineHeight + padding * 2;
        int boxX = posX - boxWidth / 2;
        int boxY = posY - boxHeight / 2;
        
        // Ограничиваем границы
        boxX = Math.max(5, Math.min(boxX, screenWidth - boxWidth - 5));
        boxY = Math.max(5, Math.min(boxY, screenHeight - boxHeight - 5));
        
        GuiComponent.fill(poseStack, boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xB0101010);
        
        int contentY = boxY + padding;
        
        if (ChatConfig.showPlayerHeads()) {
            renderHead(poseStack, mc, visibleMsg.sender, boxX + padding, contentY, headSize);
        }
        
        int textX = boxX + padding + headSpace;
        int textY = contentY + (headSize - 8) / 2;
        mc.font.drawShadow(poseStack, formattedText, textX, textY, 0xFFFFFF);
    }
    
    private static void drawCenteredString(PoseStack poseStack, Minecraft mc, String text, int x, int y) {
        mc.font.drawShadow(poseStack, text, x - mc.font.width(text) / 2f, y, 0xFFFFFF);
    }
    
    private static void renderHead(PoseStack poseStack, Minecraft mc, String sender, int x, int y, int size) {
        ResourceLocation customTexture = HeadConfig.getTextureForName(sender);
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        
        if (customTexture != null) {
            RenderSystem.setShaderTexture(0, customTexture);
            GuiComponent.blit(poseStack, x, y, size, size, 0, 0, 64, 64, 64, 64);
        } else if (mc.player != null) {
            RenderSystem.setShaderTexture(0, mc.player.getSkinTextureLocation());
            GuiComponent.blit(poseStack, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
            GuiComponent.blit(poseStack, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
        }
        
        RenderSystem.disableBlend();
    }
    
    private static String getColorForSender(String sender, Minecraft mc) {
        if (mc.player != null) {
            String playerName = mc.player.getName().getString();
            if (sender.equals(playerName) || 
                (ChatConfig.hasCustomNickname() && sender.equals(ChatConfig.getCustomNickname()))) {
                return ChatConfig.getNicknameColor();
            }
        }
        return ChatConfig.getNameColor(sender);
    }
}