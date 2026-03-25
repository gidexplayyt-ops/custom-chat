package com.yourname.customchat.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yourname.customchat.ChatHistory;
import com.yourname.customchat.config.ChatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CustomChatScreen extends ChatScreen {
    private EditBox customInput;
    private static final int MAX_CHAT_LINES = 10;
    private final String defaultText;
    private int inputY;
    
    // TAB автодополнение
    private final List<String> suggestions = new ArrayList<>();
    private int suggestionIndex = 0;
    private String lastTabText = "";
    
    // Кэш команд
    private static final String[] ALL_COMMANDS = {
        "/chat help",
        "/chat clear",
        "/chat reload",
        "/chat name ",
        "/chat name reset",
        "/chat color ",
        "/chat speedrun",
        "/chat speedrun start",
        "/chat speedrun stop",
        "/chat speedrun hide",
        "/chat speedrun goal ",
        "/clearchat",
        "/chatname ",
        "/chatcolor ",
        "/gamemode creative",
        "/gamemode survival",
        "/gamemode spectator",
        "/gamemode adventure",
        "/give @s ",
        "/tp @s ",
        "/time set day",
        "/time set night",
        "/time set noon",
        "/weather clear",
        "/weather rain",
        "/weather thunder",
        "/kill",
        "/help"
    };

    public CustomChatScreen(String defaultText) {
        super(defaultText);
        this.defaultText = defaultText != null ? defaultText : "";
    }

    @Override
    protected void init() {
        super.init();
        
        ChatHistory.resetHistoryIndex();
        
        if (this.input != null) {
            this.input.setVisible(false);
        }
        
        int fieldWidth = 350;
        int fieldHeight = 20;
        int centerX = this.width / 2 - fieldWidth / 2;
        this.inputY = this.height / 2 + 120;
        
        this.customInput = new EditBox(
            this.font,
            centerX,
            this.inputY,
            fieldWidth,
            fieldHeight,
            Component.literal("")
        );
        
        this.customInput.setMaxLength(256);
        this.customInput.setBordered(true);
        this.customInput.setVisible(true);
        this.customInput.setTextColor(0xFFFFFF);
        this.customInput.setCanLoseFocus(false);
        this.customInput.setValue(this.defaultText);
        
        this.addRenderableWidget(this.customInput);
        this.setInitialFocus(this.customInput);
        
        suggestions.clear();
        suggestionIndex = 0;
        lastTabText = "";
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        fill(poseStack, 0, 0, this.width, this.height, 0x80000000);
        
        List<ChatHistory.ChatMessage> messages = ChatHistory.getRecentMessages(MAX_CHAT_LINES);
        
        int maxTextWidth = calculateMaxTextWidth(messages);
        
        int boxWidth = Math.min(maxTextWidth + 50, this.width - 40);
        boxWidth = Math.max(boxWidth, 300);
        
        int lineHeight = 18;
        int displayCount = Math.max(1, Math.min(messages.size(), MAX_CHAT_LINES));
        
        int boxHeight = displayCount * lineHeight + 60;
        int boxX = this.width / 2 - boxWidth / 2;
        int boxY = this.height / 2 - boxHeight / 2 + 40;
        
        fill(poseStack, boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xE0101010);
        
        drawCenteredString(poseStack, this.font, "§l§fЧат", this.width / 2, boxY + 8, 0xFFFFFF);
        
        fill(poseStack, boxX + 10, boxY + 22, boxX + boxWidth - 10, boxY + 23, 0x40FFFFFF);
        
        renderChatHistory(poseStack, boxX + 10, boxY + 30, boxWidth - 20, messages);
        
        this.customInput.render(poseStack, mouseX, mouseY, partialTick);
        
        renderSuggestions(poseStack);
        
        String hint = "§7[Enter] §fОтправить  §7[Esc] §fЗакрыть  §7[↑↓] §fИстория  §7[TAB] §fКоманды";
        drawCenteredString(poseStack, this.font, hint, this.width / 2, this.inputY + 25, 0xAAAAAA);
    }
    
    private int calculateMaxTextWidth(List<ChatHistory.ChatMessage> messages) {
        int maxWidth = 200;
        for (ChatHistory.ChatMessage msg : messages) {
            String colorCode = getColorForSender(msg.sender);
            String formattedText = colorCode + msg.sender + "§7: §f" + msg.message;
            int width = this.font.width(formattedText);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        return maxWidth;
    }
    
    private void renderSuggestions(PoseStack poseStack) {
        if (suggestions.isEmpty()) return;
        
        int x = this.width / 2 - 175;
        int y = this.inputY - 5 - (suggestions.size() * 12);
        
        int boxWidth = 350;
        int boxHeight = suggestions.size() * 12 + 4;
        fill(poseStack, x, y, x + boxWidth, y + boxHeight, 0xE0202020);
        
        for (int i = 0; i < suggestions.size(); i++) {
            String suggestion = suggestions.get(i);
            int color = (i == suggestionIndex) ? 0xFFFFFF00 : 0xFFAAAAAA;
            this.font.drawShadow(poseStack, suggestion, x + 4, y + 2 + i * 12, color);
        }
    }

    private void renderChatHistory(PoseStack poseStack, int x, int y, int maxWidth, List<ChatHistory.ChatMessage> messages) {
        if (messages.isEmpty()) {
            this.font.drawShadow(poseStack, "§7Сообщений пока нет...", x + 10, y + 10, 0x888888);
            return;
        }
        
        int lineHeight = 18;
        int headSize = ChatConfig.getHeadSize();
        int headSpace = ChatConfig.showPlayerHeads() ? headSize + 4 : 0;
        
        for (int i = 0; i < messages.size(); i++) {
            ChatHistory.ChatMessage msg = messages.get(i);
            int msgY = y + i * lineHeight;
            
            if (ChatConfig.showPlayerHeads()) {
                renderPlayerHead(poseStack, x, msgY, headSize);
            }
            
            String colorCode = getColorForSender(msg.sender);
            String formattedText = colorCode + msg.sender + "§7: §f" + msg.message;
            
            // Обрезаем текст если нужно
            if (this.font.width(formattedText) > maxWidth - headSpace - 10) {
                while (this.font.width(formattedText + "...") > maxWidth - headSpace - 10 && formattedText.length() > 0) {
                    formattedText = formattedText.substring(0, formattedText.length() - 1);
                }
                formattedText += "...";
            }
            
            this.font.drawShadow(poseStack, formattedText, x + headSpace, msgY + 3, 0xFFFFFF);
        }
    }
    
    private String getColorForSender(String sender) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            String playerName = mc.player.getName().getString();
            if (sender.equals(playerName) || 
                (ChatConfig.hasCustomNickname() && sender.equals(ChatConfig.getCustomNickname()))) {
                return ChatConfig.getNicknameColor();
            }
        }
        return ChatConfig.getNameColor(sender);
    }

    private void renderPlayerHead(PoseStack poseStack, int x, int y, int size) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, mc.player.getSkinTextureLocation());
        RenderSystem.enableBlend();
        
        blit(poseStack, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
        blit(poseStack, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
        
        RenderSystem.disableBlend();
    }
    
    private void updateSuggestions() {
        suggestions.clear();
        suggestionIndex = 0;
        
        String text = this.customInput.getValue();
        if (text == null || !text.startsWith("/")) return;
        
        String cmd = text.toLowerCase();
        
        for (String command : ALL_COMMANDS) {
            if (command.toLowerCase().startsWith(cmd)) {
                suggestions.add(command);
                if (suggestions.size() >= 8) break;
            }
        }
    }
    
    private void applySuggestion() {
        if (suggestions.isEmpty()) return;
        
        String suggestion = suggestions.get(suggestionIndex);
        this.customInput.setValue(suggestion);
        this.customInput.moveCursorToEnd();
        
        if (!suggestion.endsWith(" ")) {
            suggestions.clear();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // TAB
        if (keyCode == 258) {
            String currentText = this.customInput.getValue();
            
            if (suggestions.isEmpty() || !currentText.equals(lastTabText)) {
                updateSuggestions();
                lastTabText = currentText;
                
                if (!suggestions.isEmpty()) {
                    applySuggestion();
                    lastTabText = this.customInput.getValue();
                }
            } else {
                suggestionIndex = (suggestionIndex + 1) % suggestions.size();
                applySuggestion();
                lastTabText = this.customInput.getValue();
            }
            return true;
        }
        
        // Стрелка вверх
        if (keyCode == 265) {
            if (!suggestions.isEmpty()) {
                suggestionIndex = (suggestionIndex - 1 + suggestions.size()) % suggestions.size();
                applySuggestion();
                lastTabText = this.customInput.getValue();
                return true;
            }
            String prev = ChatHistory.getPreviousMessage();
            if (prev != null) {
                this.customInput.setValue(prev);
                this.customInput.moveCursorToEnd();
            }
            return true;
        }
        
        // Стрелка вниз
        if (keyCode == 264) {
            if (!suggestions.isEmpty()) {
                suggestionIndex = (suggestionIndex + 1) % suggestions.size();
                applySuggestion();
                lastTabText = this.customInput.getValue();
                return true;
            }
            String next = ChatHistory.getNextMessage();
            this.customInput.setValue(next);
            this.customInput.moveCursorToEnd();
            return true;
        }
        
        // Enter
        if (keyCode == 257 || keyCode == 335) {
            suggestions.clear();
            String message = this.customInput.getValue();
            if (message != null) {
                message = message.trim();
                if (!message.isEmpty()) {
                    ChatHistory.addSentMessage(message);
                    if (this.input != null) {
                        this.input.setValue(message);
                    }
                    this.handleChatInput(message, true);
                }
            }
            this.minecraft.setScreen(null);
            return true;
        }
        
        // Escape
        if (keyCode == 256) {
            if (!suggestions.isEmpty()) {
                suggestions.clear();
                return true;
            }
            this.minecraft.setScreen(null);
            return true;
        }
        
        return this.customInput.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean result = this.customInput.charTyped(codePoint, modifiers);
        
        String text = this.customInput.getValue();
        if (text != null && text.startsWith("/")) {
            updateSuggestions();
        } else {
            suggestions.clear();
        }
        
        return result;
    }
    
    @Override
    public void tick() {
        this.customInput.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}