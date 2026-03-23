package com.yourname.customchat;

import com.mojang.logging.LogUtils;
import com.yourname.customchat.client.KeyBindings;
import com.yourname.customchat.client.gui.ChatSettingsScreen;
import com.yourname.customchat.client.gui.CustomChatScreen;
import com.yourname.customchat.client.ChatHudOverlay;
import com.yourname.customchat.command.ChatCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

@Mod(CustomChatMod.MODID)
public class CustomChatMod {
    public static final String MODID = "customchat";
    public static final String VERSION = "2.1.0";
    public static final String AUTHOR = "GidexPlayYT";
    
    private static final Logger LOGGER = LogUtils.getLogger();

    public CustomChatMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerKeyBindings);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
        MinecraftForge.EVENT_BUS.register(GameEvents.class);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Custom Chat v{} by {} загружен!", VERSION, AUTHOR);
    }
    
    private void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.OPEN_SETTINGS);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientEvents {
        
        @SubscribeEvent
        public static void onScreenOpen(ScreenEvent.Opening event) {
            if (event.getScreen() instanceof ChatScreen && !(event.getScreen() instanceof CustomChatScreen)) {
                event.setCanceled(true);
                Minecraft.getInstance().setScreen(new CustomChatScreen(""));
            }
        }
        
        @SubscribeEvent
        public static void onChatReceived(ClientChatReceivedEvent event) {
            ChatHistory.addMessage(event.getMessage());
        }
        
        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onRenderChatPre(RenderGuiOverlayEvent.Pre event) {
            if (event.getOverlay() == VanillaGuiOverlay.CHAT_PANEL.type()) {
                event.setCanceled(true);
            }
        }
        
        @SubscribeEvent
        public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
            if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
                Minecraft mc = Minecraft.getInstance();
                if (!(mc.screen instanceof CustomChatScreen) && !(mc.screen instanceof ChatSettingsScreen)) {
                    ChatHudOverlay.render(event.getPoseStack(), mc);
                }
            }
        }
        
        @SubscribeEvent
        public static void onRegisterCommands(RegisterClientCommandsEvent event) {
            ChatCommand.register(event.getDispatcher());
        }
        
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Minecraft mc = Minecraft.getInstance();
                
                // F7
                if (mc.screen == null && KeyBindings.OPEN_SETTINGS.consumeClick()) {
                    mc.setScreen(new ChatSettingsScreen());
                }
                
                // Свечение
                if (mc.player != null && SpeedrunManager.isActive() && SpeedrunManager.isGlowingEnabled()) {
                    mc.player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false));
                }
                
                // Проверка предметов в инвентаре
                if (mc.player != null && SpeedrunManager.isActive() && 
                    SpeedrunManager.getGoalType() == SpeedrunManager.GoalType.ITEM) {
                    checkInventoryForGoal(mc);
                }
            }
        }
        
        private static void checkInventoryForGoal(Minecraft mc) {
            if (mc.player == null) return;
            
            String targetItem = SpeedrunManager.getGoalTarget();
            int count = 0;
            
            for (ItemStack stack : mc.player.getInventory().items) {
                if (!stack.isEmpty()) {
                    String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                    if (itemId.equals(targetItem)) {
                        count += stack.getCount();
                    }
                }
            }
            
            SpeedrunManager.setCurrentProgress(count);
        }
    }
    
    // События игры (убийства)
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class GameEvents {
        
        @SubscribeEvent
        public static void onEntityDeath(LivingDeathEvent event) {
            if (!SpeedrunManager.isActive()) return;
            if (SpeedrunManager.getGoalType() != SpeedrunManager.GoalType.KILL) return;
            
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            
            // Проверяем, что игрок убил моба
            if (event.getSource().getEntity() == mc.player) {
                LivingEntity killed = event.getEntity();
                String entityId = ForgeRegistries.ENTITY_TYPES.getKey(killed.getType()).toString();
                
                if (SpeedrunManager.matchesKillGoal(entityId)) {
                    SpeedrunManager.addProgress(1);
                }
            }
        }
    }
}