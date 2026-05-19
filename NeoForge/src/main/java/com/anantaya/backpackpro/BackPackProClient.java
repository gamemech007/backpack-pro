package com.anantaya.backpackpro;

import com.anantaya.backpackpro.client.BackpackKeybinds;
import com.anantaya.backpackpro.client.BackpackScreen;
import com.anantaya.backpackpro.registry.ModMenus;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = BackPackPro.MODID, value = Dist.CLIENT)
public class BackPackProClient {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.BACKPACK_MENU_IRON.get(), BackpackScreen::new);
        event.register(ModMenus.BACKPACK_MENU_DIAMOND.get(), BackpackScreen::new);
        event.register(ModMenus.BACKPACK_MENU_NETHERITE.get(), BackpackScreen::new);
    }

    @SubscribeEvent
    public static void registerKeybinds(RegisterKeyMappingsEvent event) {
        event.registerCategory(BackpackKeybinds.CATEGORY);
        event.register(BackpackKeybinds.OPEN_BACKPACK);
    }
}