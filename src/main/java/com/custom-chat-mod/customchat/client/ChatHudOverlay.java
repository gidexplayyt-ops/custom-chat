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
    
    public static void render(PoseStack poseStack, Minecraft mc) {
        if (mc.player == null || mc.options.hideGui) return;
        
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        int posX = (int) (screenWidth * ChatConfig.getChatPositionX() / 100.0);
        int posY = (int) (screenHeight * ChatConfig.getChatPositionY() / 100.0);
        
        if (SpeedrunManager.isActive()) {
            renderSpeedrunTimer(poseStack, mc, posX, posY);
            return;
        }
        
        renderChat(poseStack, mc, posX, posY, screenWidth, screenHeight);
    }
    
    private static void renderSpeedrunTimer(PoseStack poseStack, Minecraft mc, int posX, int posY) {
        boolean completed = SpeedrunManager.isCompleted();
        
        int boxWidth = 220;
        int boxHeight = completed ? 85 : 70;
        int boxX = posX - boxWidth / 2;
        int boxY = posY - boxHeight / 2;
        
        // Фон (зелёный если завершён)
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
        String timeColor = completed ? "§a§l" : "§f§l";
        int timeWidth = mc.font.width(time);
        mc.font.drawShadow(poseStack, timeColor + time, posX - timeWidth / 2f, boxY + 8, 0xFFFFFF);
        
        // Статус
        if (completed) {
            String completeText = "§a§l✓ ЗАВЕРШЕНО!";
            int completeWidth = mc.font.width(completeText);
            mc.font.drawShadow(poseStack, completeText, posX - completeWidth / 2f, boxY + 25, 0x00FF00);
        }
        
        // Цель
        String goal = SpeedrunManager.getGoalDisplayName();
        int goalY = completed ? boxY + 42 : boxY + 28;
        mc.font.drawShadow(poseStack, "§7" + goal, posX - mc.font.width(goal) / 2f, goalY, 0xAAAAAA);
        
        // Прогресс
        String progress = SpeedrunManager.getProgressText();
        int progressWidth = mc.font.width(progress);
        
        // Полоса прогресса
        int barWidth = 180;
        int barHeight = 8;
        int barX = posX - barWidth / 2;
        int barY = completed ? boxY + 58 : boxY + 45;
        
        // Фон полосы
        GuiComponent.fill(poseStack, barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        
        // Заполнение
        float progressPercent = (float) SpeedrunManager.getCurrentProgress() / SpeedrunManager.getGoalAmount();
        progressPercent = Math.min(1.0f, progressPercent);
        int fillWidth = (int) (barWidth * progressPercent);
        
        int fillColor = completed ? 0xFF00FF00 : 0xFF00AAFF;
        GuiComponent.fill(poseStack, barX, barY, barX + fillWidth, barY + barHeight, fillColor);
        
        // Текст прогресса
        int progressY = completed ? boxY + 70 : boxY + 56;
        mc.font.drawShadow(poseStack, progress, posX - progressWidth / 2f, progressY, 0xFFFFFF);
        
        // Подсказка скрыть
        if (completed) {
            String hideHint = "§8/chat speedrun - скрыть";
            mc.font.drawShadow(poseStack, hideHint, posX - mc.font.width(hideHint) / 2f, boxY + boxHeight + 5, 0x666666);
        }
    }
    
    private static void renderChat(PoseStack poseStack, Minecraft mc, int posX, int posY, int screenWidth, int screenHeight) {
        List<ChatHistory.ChatMessage> messages = ChatHistory.getRecentMessages(MAX_VISIBLE);
        if (messages.isEmpty()) return;
        
        int headSize = ChatConfig.getHeadSize();
        int lineHeight = Math.max(headSize + 4, 20);
        int padding = 8;
        
        int visibleCount = 0;
        int maxTextWidth = 0;
        
        for (ChatHistory.ChatMessage msg : messages) {
            if (msg.isRecent()) {
                visibleCount++;
                String colorCode = getColorForSender(msg.sender, mc);
                String formattedText = colorCode + msg.sender + "§7: §f" + msg.message;
                int textWidth = mc.font.width(formattedText);
                if (textWidth > maxTextWidth) {
                    maxTextWidth = textWidth;
                }
            }
        }
        
        if (visibleCount == 0) return;
        
        int headSpace = ChatConfig.showPlayerHeads() ? headSize + 6 : 0;
        int boxWidth = maxTextWidth + headSpace + padding * 2;
        if (boxWidth < 150) boxWidth = 150;
        if (boxWidth > screenWidth - 40) boxWidth = screenWidth - 40;
        
        int boxHeight = visibleCount * lineHeight + padding * 2;
        int boxX = posX - boxWidth / 2;
        int boxY = posY - boxHeight / 2;
        
        if (boxX < 5) boxX = 5;
        if (boxX + boxWidth > screenWidth - 5) boxX = screenWidth - boxWidth - 5;
        if (boxY < 5) boxY = 5;
        if (boxY + boxHeight > screenHeight - 5) boxY = screenHeight - boxHeight - 5;
        
        GuiComponent.fill(poseStack, boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xB0101010);
        
        int y = boxY + padding;
        for (ChatHistory.ChatMessage msg : messages) {
            if (!msg.isRecent()) continue;
            
            if (ChatConfig.showPlayerHeads()) {
                renderHead(poseStack, mc, msg.sender, boxX + padding, y, headSize);
            }
            
            String colorCode = getColorForSender(msg.sender, mc);
            String formattedText = colorCode + msg.sender + "§7: §f" + msg.message;
            
            int textX = boxX + padding + headSpace;
            int textY = y + (headSize - 8) / 2;
            mc.font.drawShadow(poseStack, formattedText, textX, textY, 0xFFFFFF);
            
            y += lineHeight;
        }
    }
    
    private static void renderHead(PoseStack poseStack, Minecraft mc, String sender, int x, int y, int size) {
        ResourceLocation customTexture = HeadConfig.getTextureForName(sender);
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        
        if (customTexture != null) {
            RenderSystem.setShaderTexture(0, customTexture);
            GuiComponent.blit(poseStack, x, y, size, size, 0, 0, 64, 64, 64, 64);
        } else {
            if (mc.player != null) {
                RenderSystem.setShaderTexture(0, mc.player.getSkinTextureLocation());
                GuiComponent.blit(poseStack, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
                GuiComponent.blit(poseStack, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
            }
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