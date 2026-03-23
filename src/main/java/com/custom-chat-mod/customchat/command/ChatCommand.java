package com.yourname.customchat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.yourname.customchat.ChatHistory;
import com.yourname.customchat.SpeedrunManager;
import com.yourname.customchat.config.ChatConfig;
import com.yourname.customchat.config.HeadConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ChatCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("chat")
                .then(Commands.literal("clear")
                    .executes(ChatCommand::executeClear))
                
                .then(Commands.literal("reload")
                    .executes(ChatCommand::executeReload))
                
                .then(Commands.literal("speedrun")
                    .executes(ChatCommand::executeSpeedrunToggle)
                    .then(Commands.literal("start")
                        .executes(ChatCommand::executeSpeedrunStart))
                    .then(Commands.literal("stop")
                        .executes(ChatCommand::executeSpeedrunStop))
                    .then(Commands.literal("hide")
                        .executes(ChatCommand::executeSpeedrunHide))
                    .then(Commands.literal("goal")
                        .then(Commands.argument("goal", StringArgumentType.greedyString())
                            .executes(ChatCommand::executeSpeedrunGoal))))
                
                .then(Commands.literal("name")
                    .then(Commands.literal("reset")
                        .executes(ChatCommand::executeResetName))
                    .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(ChatCommand::executeSetName))
                    .executes(ChatCommand::executeShowName))
                
                .then(Commands.literal("color")
                    .then(Commands.argument("color", StringArgumentType.word())
                        .executes(ChatCommand::executeSetColor))
                    .executes(ChatCommand::executeShowColors))
                
                .then(Commands.literal("help")
                    .executes(ChatCommand::executeHelp))
                
                .executes(ChatCommand::executeHelp)
        );
        
        // Алиасы
        dispatcher.register(Commands.literal("clearchat").executes(ChatCommand::executeClear));
        dispatcher.register(
            Commands.literal("chatname")
                .then(Commands.literal("reset").executes(ChatCommand::executeResetName))
                .then(Commands.argument("name", StringArgumentType.greedyString()).executes(ChatCommand::executeSetName))
                .executes(ChatCommand::executeShowName)
        );
        dispatcher.register(
            Commands.literal("chatcolor")
                .then(Commands.argument("color", StringArgumentType.word()).executes(ChatCommand::executeSetColor))
                .executes(ChatCommand::executeShowColors)
        );
    }
    
    private static int executeHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(Component.literal("§6=== Custom Chat Commands ==="), false);
        context.getSource().sendSuccess(Component.literal("§e/chat clear §7- Очистить чат"), false);
        context.getSource().sendSuccess(Component.literal("§e/chat reload §7- Перезагрузить конфиг"), false);
        context.getSource().sendSuccess(Component.literal("§e/chat name <ник> §7- Установить ник"), false);
        context.getSource().sendSuccess(Component.literal("§e/chat color <цвет> §7- Изменить цвет"), false);
        context.getSource().sendSuccess(Component.literal("§e/chat speedrun §7- Вкл/выкл спидран"), false);
        context.getSource().sendSuccess(Component.literal("§e/chat speedrun hide §7- Скрыть результат"), false);
        context.getSource().sendSuccess(Component.literal("§e/chat speedrun goal <цель> §7- Цель"), false);
        context.getSource().sendSuccess(Component.literal("§8Форматы: minecraft:diamond, kill:ender_dragon"), false);
        return 1;
    }
    
    private static int executeClear(CommandContext<CommandSourceStack> context) {
        ChatHistory.clearChat();
        context.getSource().sendSuccess(Component.literal("§aЧат очищен!"), false);
        return 1;
    }
    
    private static int executeReload(CommandContext<CommandSourceStack> context) {
        ChatConfig.reload();
        HeadConfig.reload();
        context.getSource().sendSuccess(Component.literal("§aКонфиг и головы перезагружены!"), false);
        return 1;
    }
    
    // Speedrun
    private static int executeSpeedrunToggle(CommandContext<CommandSourceStack> context) {
        if (SpeedrunManager.isActive()) {
            // Если активен — скрываем
            SpeedrunManager.hide();
            context.getSource().sendSuccess(Component.literal("§7Спидран скрыт"), false);
        } else {
            // Если не активен — запускаем
            SpeedrunManager.start();
            context.getSource().sendSuccess(Component.literal("§a§lСпидран начался! §7Цель: " + SpeedrunManager.getGoalDisplayName()), false);
        }
        return 1;
    }
    
    private static int executeSpeedrunStart(CommandContext<CommandSourceStack> context) {
        SpeedrunManager.start();
        context.getSource().sendSuccess(Component.literal("§a§lСпидран начался! §7Цель: " + SpeedrunManager.getGoalDisplayName()), false);
        return 1;
    }
    
    private static int executeSpeedrunStop(CommandContext<CommandSourceStack> context) {
        String time = SpeedrunManager.getFormattedTime();
        SpeedrunManager.stop();
        context.getSource().sendSuccess(Component.literal("§cСпидран остановлен. §7Время: §f" + time), false);
        return 1;
    }
    
    private static int executeSpeedrunHide(CommandContext<CommandSourceStack> context) {
        String time = SpeedrunManager.getFormattedTime();
        SpeedrunManager.hide();
        context.getSource().sendSuccess(Component.literal("§7Спидран скрыт. Время было: §f" + time), false);
        return 1;
    }
    
    private static int executeSpeedrunGoal(CommandContext<CommandSourceStack> context) {
        String goal = StringArgumentType.getString(context, "goal");
        SpeedrunManager.parseGoal(goal);
        ChatConfig.setSpeedrunGoal(goal);
        context.getSource().sendSuccess(Component.literal("§aЦель: §f" + SpeedrunManager.getGoalDisplayName()), false);
        return 1;
    }
    
    // Name
    private static int executeSetName(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        ChatConfig.setCustomNickname(name);
        String color = ChatConfig.getNicknameColor();
        context.getSource().sendSuccess(Component.literal("§aНик: " + color + name), false);
        return 1;
    }
    
    private static int executeShowName(CommandContext<CommandSourceStack> context) {
        if (ChatConfig.hasCustomNickname()) {
            String color = ChatConfig.getNicknameColor();
            context.getSource().sendSuccess(Component.literal("§7Ник: " + color + ChatConfig.getCustomNickname()), false);
        } else {
            context.getSource().sendSuccess(Component.literal("§7Ник не установлен. §f/chat name <ник>"), false);
        }
        return 1;
    }
    
    private static int executeResetName(CommandContext<CommandSourceStack> context) {
        ChatConfig.clearCustomNickname();
        context.getSource().sendSuccess(Component.literal("§aНик сброшен"), false);
        return 1;
    }
    
    // Color
    private static int executeSetColor(CommandContext<CommandSourceStack> context) {
        String colorName = StringArgumentType.getString(context, "color");
        String colorCode = ChatConfig.parseColorName(colorName);
        ChatConfig.setNicknameColor(colorCode);
        context.getSource().sendSuccess(Component.literal("§aЦвет: " + colorCode + colorName), false);
        return 1;
    }
    
    private static int executeShowColors(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(Component.literal("§7Цвета:"), false);
        context.getSource().sendSuccess(Component.literal("§cred §4dark_red §9blue §baqua §agreen §2dark_green"), false);
        context.getSource().sendSuccess(Component.literal("§eyellow §6gold §5purple §dlight_purple §7gray §fwhite"), false);
        return 1;
    }
}