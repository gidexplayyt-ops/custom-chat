package com.yourname.customchat;

public class SpeedrunManager {
    private static boolean active = false;
    private static boolean completed = false;
    private static boolean glowingEnabled = false;
    private static long startTime = 0;
    private static long endTime = 0;
    
    public enum GoalType {
        ITEM,
        KILL,
        CUSTOM
    }
    
    private static GoalType goalType = GoalType.CUSTOM;
    private static String goalTarget = "ender_dragon";
    private static String goalDisplayName = "Убить Дракона";
    private static int goalAmount = 1;
    private static int currentProgress = 0;
    
    // Кэш для форматированного времени
    private static String cachedTime = "00:00.00";
    private static long lastTimeUpdate = 0;
    
    public static void start() {
        active = true;
        completed = false;
        startTime = System.currentTimeMillis();
        endTime = 0;
        currentProgress = 0;
        cachedTime = "00:00.00";
    }
    
    public static void stop() {
        active = false;
        completed = false;
        startTime = 0;
        endTime = 0;
        cachedTime = "00:00.00";
    }
    
    public static void complete() {
        if (!completed && active) {
            completed = true;
            endTime = System.currentTimeMillis();
            updateCachedTime();
        }
    }
    
    public static void hide() {
        active = false;
        completed = false;
        startTime = 0;
        endTime = 0;
    }
    
    public static void toggle() {
        if (active) {
            hide();
        } else {
            start();
        }
    }
    
    public static boolean isActive() {
        return active;
    }
    
    public static boolean isCompleted() {
        return completed;
    }
    
    private static void updateCachedTime() {
        long elapsed = getElapsedMillis();
        
        long hours = elapsed / 3600000;
        long minutes = (elapsed % 3600000) / 60000;
        long seconds = (elapsed % 60000) / 1000;
        long millis = (elapsed % 1000) / 10;
        
        if (hours > 0) {
            cachedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            cachedTime = String.format("%02d:%02d.%02d", minutes, seconds, millis);
        }
    }
    
    public static String getFormattedTime() {
        if (startTime == 0) return "00:00.00";
        
        // Обновляем кэш только раз в 50мс (оптимизация)
        long now = System.currentTimeMillis();
        if (!completed && now - lastTimeUpdate > 50) {
            updateCachedTime();
            lastTimeUpdate = now;
        }
        
        return cachedTime;
    }
    
    public static long getElapsedMillis() {
        if (startTime == 0) return 0;
        if (completed && endTime > 0) {
            return endTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }
    
    public static GoalType getGoalType() {
        return goalType;
    }
    
    public static void setGoalType(GoalType type) {
        goalType = type;
    }
    
    public static String getGoalTarget() {
        return goalTarget;
    }
    
    public static void setGoalTarget(String target) {
        goalTarget = target != null ? target : "";
    }
    
    public static String getGoalDisplayName() {
        return goalDisplayName;
    }
    
    public static void setGoalDisplayName(String name) {
        goalDisplayName = name != null ? name : "Без цели";
    }
    
    public static int getGoalAmount() {
        return goalAmount;
    }
    
    public static void setGoalAmount(int amount) {
        goalAmount = Math.max(1, amount);
    }
    
    public static int getCurrentProgress() {
        return currentProgress;
    }
    
    public static void setCurrentProgress(int progress) {
        int oldProgress = currentProgress;
        currentProgress = Math.max(0, progress);
        
        // Проверяем завершение только если прогресс изменился
        if (currentProgress != oldProgress && currentProgress >= goalAmount && !completed) {
            complete();
        }
    }
    
    public static void addProgress(int amount) {
        setCurrentProgress(currentProgress + amount);
    }
    
    public static boolean isGoalCompleted() {
        return currentProgress >= goalAmount;
    }
    
    public static String getProgressText() {
        return currentProgress + "/" + goalAmount;
    }
    
    public static float getProgressPercent() {
        if (goalAmount <= 0) return 0;
        return Math.min(1.0f, (float) currentProgress / goalAmount);
    }
    
    public static void parseGoal(String input) {
        if (input == null || input.isEmpty()) {
            goalType = GoalType.CUSTOM;
            goalTarget = "";
            goalDisplayName = "Без цели";
            return;
        }
        
        input = input.trim().toLowerCase();
        
        if (input.startsWith("kill:")) {
            goalType = GoalType.KILL;
            goalTarget = input.substring(5);
            goalDisplayName = "Убить: " + formatName(goalTarget);
        } else if (input.contains(":")) {
            goalType = GoalType.ITEM;
            goalTarget = input;
            String[] parts = input.split(":");
            goalDisplayName = "Собрать: " + formatName(parts.length > 1 ? parts[1] : parts[0]);
        } else {
            goalType = GoalType.CUSTOM;
            goalTarget = input;
            goalDisplayName = input;
        }
    }
    
    private static String formatName(String id) {
        if (id == null || id.isEmpty()) return "";
        
        String[] parts = id.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                      .append(part.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }
    
    public static boolean matchesItemGoal(String itemId) {
        if (goalType != GoalType.ITEM || !active || itemId == null) return false;
        return goalTarget.equals(itemId);
    }
    
    public static boolean matchesKillGoal(String entityId) {
        if (goalType != GoalType.KILL || !active || entityId == null) return false;
        return entityId.toLowerCase().contains(goalTarget);
    }
    
    public static boolean isGlowingEnabled() {
        return glowingEnabled;
    }
    
    public static void setGlowingEnabled(boolean enabled) {
        glowingEnabled = enabled;
    }
    
    public static void toggleGlowing() {
        glowingEnabled = !glowingEnabled;
    }
}