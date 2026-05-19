package com.anantaya.backpackpro.client;

import com.anantaya.backpackpro.BackPackPro;
import com.anantaya.backpackpro.network.OpenBackpackPayload;

import net.minecraft.client.Minecraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = BackPackPro.MODID, value = Dist.CLIENT)
public class BackpackKeyInput {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        while (BackpackKeybinds.OPEN_BACKPACK.consumeClick()) {
            if (mc.player != null && mc.screen == null) {
                ClientPacketDistributor.sendToServer(OpenBackpackPayload.INSTANCE);
            }
        }
    }
}