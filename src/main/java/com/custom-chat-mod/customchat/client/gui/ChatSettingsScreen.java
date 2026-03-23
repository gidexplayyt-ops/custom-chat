package com.yourname.customchat.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yourname.customchat.SpeedrunManager;
import com.yourname.customchat.config.ChatConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChatSettingsScreen extends Screen {
    private EditBox posXField;
    private EditBox posYField;
    private EditBox durationField;
    private EditBox headSizeField;
    private EditBox goalField;
    private EditBox amountField;
    private boolean dragging = false;
    
    private int currentTab = 0; // 0 = Чат, 1 = Спидран
    
    public ChatSettingsScreen() {
        super(Component.literal("Настройки чата"));
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int tabY = 45; // Кнопки вкладок ниже
        int startY = 80; // Контент ещё ниже
        
        // Кнопки вкладок
        this.addRenderableWidget(new Button(
            centerX - 102, tabY, 100, 20,
            Component.literal(currentTab == 0 ? "§a> Чат" : "§7Чат"),
            button -> {
                currentTab = 0;
                this.rebuildWidgets();
            }
        ));
        
        this.addRenderableWidget(new Button(
            centerX + 2, tabY, 100, 20,
            Component.literal(currentTab == 1 ? "§a> Спидран" : "§7Спидран"),
            button -> {
                currentTab = 1;
                this.rebuildWidgets();
            }
        ));
        
        if (currentTab == 0) {
            initChatTab(centerX, startY);
        } else {
            initSpeedrunTab(centerX, startY);
        }
    }
    
    private void initChatTab(int centerX, int startY) {
        // X позиция
        this.posXField = new EditBox(this.font, centerX - 100, startY + 15, 60, 20, Component.literal("X"));
        this.posXField.setValue(String.valueOf(ChatConfig.getChatPositionX()));
        this.posXField.setMaxLength(3);
        this.addRenderableWidget(this.posXField);
        
        // Y позиция
        this.posYField = new EditBox(this.font, centerX + 40, startY + 15, 60, 20, Component.literal("Y"));
        this.posYField.setValue(String.valueOf(ChatConfig.getChatPositionY()));
        this.posYField.setMaxLength(3);
        this.addRenderableWidget(this.posYField);
        
        // Длительность
        this.durationField = new EditBox(this.font, centerX - 30, startY + 55, 60, 20, Component.literal("Сек"));
        this.durationField.setValue(String.valueOf(ChatConfig.getMessageDuration()));
        this.durationField.setMaxLength(2);
        this.addRenderableWidget(this.durationField);
        
        // Размер головы
        this.headSizeField = new EditBox(this.font, centerX - 30, startY + 95, 60, 20, Component.literal("Size"));
        this.headSizeField.setValue(String.valueOf(ChatConfig.getHeadSize()));
        this.headSizeField.setMaxLength(2);
        this.addRenderableWidget(this.headSizeField);
        
        // Кнопка голов
        this.addRenderableWidget(new Button(
            centerX - 75, startY + 130, 150, 20,
            Component.literal("Головы: " + (ChatConfig.showPlayerHeads() ? "§aВКЛ" : "§cВЫКЛ")),
            button -> {
                ChatConfig.setShowPlayerHeads(!ChatConfig.showPlayerHeads());
                button.setMessage(Component.literal("Головы: " + (ChatConfig.showPlayerHeads() ? "§aВКЛ" : "§cВЫКЛ")));
            }
        ));
        
        // Сброс
        this.addRenderableWidget(new Button(
            centerX - 75, startY + 160, 150, 20,
            Component.literal("Сбросить настройки"),
            button -> {
                ChatConfig.setChatPositionX(50);
                ChatConfig.setChatPositionY(85);
                ChatConfig.setHeadSize(16);
                this.posXField.setValue("50");
                this.posYField.setValue("85");
                this.headSizeField.setValue("16");
            }
        ));
        
        // Сохранить
        this.addRenderableWidget(new Button(
            centerX - 75, this.height - 40, 150, 20,
            Component.literal("§aСохранить и закрыть"),
            button -> {
                saveSettings();
                this.minecraft.setScreen(null);
            }
        ));
    }
    
    private void initSpeedrunTab(int centerX, int startY) {
        // Статус спидрана
        this.addRenderableWidget(new Button(
            centerX - 75, startY, 150, 20,
            Component.literal(SpeedrunManager.isActive() ? "§cОстановить" : "§aНачать спидран"),
            button -> {
                SpeedrunManager.toggle();
                button.setMessage(Component.literal(SpeedrunManager.isActive() ? "§cОстановить" : "§aНачать спидран"));
            }
        ));
        
        // Поле цели
        this.goalField = new EditBox(this.font, centerX - 100, startY + 45, 200, 20, Component.literal("Цель"));
        this.goalField.setValue(SpeedrunManager.getGoalTarget());
        this.goalField.setMaxLength(50);
        this.addRenderableWidget(this.goalField);
        
        // Количество
        this.amountField = new EditBox(this.font, centerX - 30, startY + 90, 60, 20, Component.literal("Кол-во"));
        this.amountField.setValue(String.valueOf(SpeedrunManager.getGoalAmount()));
        this.amountField.setMaxLength(4);
        this.addRenderableWidget(this.amountField);
        
        // Применить цель
        this.addRenderableWidget(new Button(
            centerX - 75, startY + 125, 150, 20,
            Component.literal("§eПрименить цель"),
            button -> {
                String goal = this.goalField.getValue();
                SpeedrunManager.parseGoal(goal);
                try {
                    int amount = Integer.parseInt(this.amountField.getValue());
                    SpeedrunManager.setGoalAmount(amount);
                } catch (NumberFormatException e) {
                    SpeedrunManager.setGoalAmount(1);
                }
                SpeedrunManager.setCurrentProgress(0);
            }
        ));
        
        // Свечение
        this.addRenderableWidget(new Button(
            centerX - 75, startY + 155, 150, 20,
            Component.literal("Свечение: " + (SpeedrunManager.isGlowingEnabled() ? "§aВКЛ" : "§cВЫКЛ")),
            button -> {
                SpeedrunManager.toggleGlowing();
                button.setMessage(Component.literal("Свечение: " + (SpeedrunManager.isGlowingEnabled() ? "§aВКЛ" : "§cВЫКЛ")));
            }
        ));
        
        // Закрыть
        this.addRenderableWidget(new Button(
            centerX - 75, this.height - 40, 150, 20,
            Component.literal("§aЗакрыть"),
            button -> this.minecraft.setScreen(null)
        ));
    }
    
    private void saveSettings() {
        try {
            if (posXField != null) {
                int x = Integer.parseInt(this.posXField.getValue());
                ChatConfig.setChatPositionX(x);
            }
            if (posYField != null) {
                int y = Integer.parseInt(this.posYField.getValue());
                ChatConfig.setChatPositionY(y);
            }
            if (durationField != null) {
                int duration = Integer.parseInt(this.durationField.getValue());
                ChatConfig.setMessageDuration(duration);
            }
            if (headSizeField != null) {
                int size = Integer.parseInt(this.headSizeField.getValue());
                ChatConfig.setHeadSize(size);
            }
        } catch (NumberFormatException e) {
            // Игнорируем
        }
    }
    
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        
        int centerX = this.width / 2;
        
        // Заголовок (выше)
        drawCenteredString(poseStack, this.font, "§l§fНастройки Custom Chat", centerX, 15, 0xFFFFFF);
        
        if (currentTab == 0) {
            // Подписи для вкладки Чат
            int startY = 80;
            drawString(poseStack, this.font, "§7X (0-100%):", centerX - 100, startY, 0xAAAAAA);
            drawString(poseStack, this.font, "§7Y (0-100%):", centerX + 40, startY, 0xAAAAAA);
            drawString(poseStack, this.font, "§7Длительность (сек):", centerX - 100, startY + 40, 0xAAAAAA);
            drawString(poseStack, this.font, "§7Размер головы (8-32):", centerX - 100, startY + 80, 0xAAAAAA);
            
            // Превью
            int previewX = (int) (this.width * ChatConfig.getChatPositionX() / 100.0);
            int previewY = (int) (this.height * ChatConfig.getChatPositionY() / 100.0);
            
            fill(poseStack, previewX - 60, previewY - 12, previewX + 60, previewY + 12, 0x80000000);
            drawCenteredString(poseStack, this.font, "§e⬛ §7Перетащи", previewX, previewY - 4, 0xFFFFFF);
        } else {
            // Вкладка Спидран
            int startY = 80;
            
            // Подсказки по формату
            drawCenteredString(poseStack, this.font, "§7Формат цели:", centerX, startY + 25, 0xAAAAAA);
            drawCenteredString(poseStack, this.font, "§8minecraft:diamond §7или §8kill:ender_dragon", centerX, startY + 35, 0x888888);
            
            drawString(poseStack, this.font, "§7Количество:", centerX - 100, startY + 75, 0xAAAAAA);
            
            // Текущая цель
            drawCenteredString(poseStack, this.font, "§7Текущая цель: §f" + SpeedrunManager.getGoalDisplayName(), centerX, startY + 185, 0xFFFFFF);
            
            if (SpeedrunManager.isActive()) {
                // Таймер
                drawCenteredString(poseStack, this.font, "§aТаймер: §f" + SpeedrunManager.getFormattedTime(), centerX, startY + 200, 0x00FF00);
                
                // Прогресс
                String progress = SpeedrunManager.getProgressText();
                int progressColor = SpeedrunManager.isGoalCompleted() ? 0x00FF00 : 0xFFFFFF;
                drawCenteredString(poseStack, this.font, "§7Прогресс: §f" + progress, centerX, startY + 215, progressColor);
                
                if (SpeedrunManager.isGoalCompleted()) {
                    drawCenteredString(poseStack, this.font, "§a§l✓ ЦЕЛЬ ДОСТИГНУТА!", centerX, startY + 230, 0x00FF00);
                }
            }
        }
        
        super.render(poseStack, mouseX, mouseY, partialTick);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (currentTab == 0) {
            int previewX = (int) (this.width * ChatConfig.getChatPositionX() / 100.0);
            int previewY = (int) (this.height * ChatConfig.getChatPositionY() / 100.0);
            
            if (mouseX >= previewX - 60 && mouseX <= previewX + 60 &&
                mouseY >= previewY - 12 && mouseY <= previewY + 12) {
                dragging = true;
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && currentTab == 0) {
            int newX = (int) (mouseX * 100 / this.width);
            int newY = (int) (mouseY * 100 / this.height);
            
            newX = Math.max(5, Math.min(95, newX));
            newY = Math.max(5, Math.min(95, newY));
            
            ChatConfig.setChatPositionX(newX);
            ChatConfig.setChatPositionY(newY);
            
            if (posXField != null) posXField.setValue(String.valueOf(newX));
            if (posYField != null) posYField.setValue(String.valueOf(newY));
            
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}