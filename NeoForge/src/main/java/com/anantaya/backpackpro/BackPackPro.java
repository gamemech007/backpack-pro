package com.anantaya.backpackpro;

import com.anantaya.backpackpro.registry.ModItems;
import com.anantaya.backpackpro.registry.ModMenus;
import com.anantaya.backpackpro.event.BackpackEvents;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import com.anantaya.backpackpro.network.ModNetwork;

import org.slf4j.Logger;

@Mod(BackPackPro.MODID)
public class BackPackPro {

    public static final String MODID = "backpackpro";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BackPackPro(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
ModNetwork.register(modEventBus);
        BackpackEvents.register();

        LOGGER.info("Backpack Pro loaded!");
    }
}