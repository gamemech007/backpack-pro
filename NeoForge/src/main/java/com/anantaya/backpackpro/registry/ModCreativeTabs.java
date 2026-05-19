package com.anantaya.backpackpro.registry;

import com.anantaya.backpackpro.BackPackPro;

import net.minecraft.world.item.CreativeModeTabs;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = BackPackPro.MODID)
public class ModCreativeTabs {

    @SubscribeEvent
    public static void addItemsToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.BACKPACK_IRON.get());
            event.accept(ModItems.BACKPACK_DIAMOND.get());
            event.accept(ModItems.BACKPACK_NETHERITE.get());
        }
    }
}