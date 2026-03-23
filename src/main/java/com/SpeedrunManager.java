package com.yourname.customchat;

public class SpeedrunManager {
    private static boolean active = false;
    private static boolean completed = false; // Новое: завершён ли спидран
    private static boolean glowingEnabled = false;
    private static long startTime = 0;
    private static long endTime = 0; // Новое: время завершения
    
    // Типы целей
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
    
    public static void start() {
        active = true;
        completed = false;
        startTime = System.currentTimeMillis();
        endTime = 0;
        currentProgress = 0;
    }
    
    public static void stop() {
        active = false;
        completed = false;
        startTime = 0;
        endTime = 0;
    }
    
    public static void complete() {
        // Автоматическое завершение при достижении цели
        if (!completed) {
            completed = true;
            endTime = System.currentTimeMillis();
            // НЕ останавливаем active - показатель остаётся!
        }
    }
    
    public static void hide() {
        // Скрыть показатель полностью
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
    
    public static String getFormattedTime() {
        if (startTime == 0) return "00:00:00";
        
        long elapsed;
        if (completed && endTime > 0) {
            // Если завершён — показываем финальное время
            elapsed = endTime - startTime;
        } else {
            // Если ещё идёт — показываем текущее
            elapsed = System.currentTimeMillis() - startTime;
        }
        
        long hours = elapsed / 3600000;
        long minutes = (elapsed % 3600000) / 60000;
        long seconds = (elapsed % 60000) / 1000;
        long millis = (elapsed % 1000) / 10;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d.%02d", minutes, seconds, millis);
        }
    }
    
    public static long getElapsedMillis() {
        if (startTime == 0) return 0;
        if (completed && endTime > 0) {
            return endTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }
    
    // Goal Type
    public static GoalType getGoalType() {
        return goalType;
    }
    
    public static void setGoalType(GoalType type) {
        goalType = type;
    }
    
    // Goal Target
    public static String getGoalTarget() {
        return goalTarget;
    }
    
    public static void setGoalTarget(String target) {
        goalTarget = target;
    }
    
    // Goal Display Name
    public static String getGoalDisplayName() {
        return goalDisplayName;
    }
    
    public static void setGoalDisplayName(String name) {
        goalDisplayName = name;
    }
    
    // Goal Amount
    public static int getGoalAmount() {
        return goalAmount;
    }
    
    public static void setGoalAmount(int amount) {
        goalAmount = Math.max(1, amount);
    }
    
    // Progress
    public static int getCurrentProgress() {
        return currentProgress;
    }
    
    public static void setCurrentProgress(int progress) {
        currentProgress = progress;
        checkCompletion();
    }
    
    public static void addProgress(int amount) {
        currentProgress += amount;
        checkCompletion();
    }
    
    private static void checkCompletion() {
        if (currentProgress >= goalAmount && !completed) {
            complete();
        }
    }
    
    public static boolean isGoalCompleted() {
        return currentProgress >= goalAmount;
    }
    
    public static String getProgressText() {
        return currentProgress + "/" + goalAmount;
    }
    
    // Парсинг цели
    public static void parseGoal(String input) {
        if (input == null || input.isEmpty()) {
            goalType = GoalType.CUSTOM;
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
            goalDisplayName = "Собрать: " + formatName(input.split(":")[1]);
        } else {
            goalType = GoalType.CUSTOM;
            goalDisplayName = input;
        }
    }
    
    private static String formatName(String id) {
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
    
    // Проверка предмета
    public static boolean matchesItemGoal(String itemId) {
        if (goalType != GoalType.ITEM || !active) return false;
        return goalTarget.equals(itemId);
    }
    
    // Проверка моба
    public static boolean matchesKillGoal(String entityId) {
        if (goalType != GoalType.KILL || !active) return false;
        return entityId.contains(goalTarget);
    }
    
    // Glowing
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