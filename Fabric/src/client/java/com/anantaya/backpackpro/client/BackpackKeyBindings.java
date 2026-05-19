package com.anantaya.backpackpro.client;

import com.anantaya.backpackpro.network.OpenBackpackPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import net.minecraft.resources.Identifier;

public class BackpackKeyBindings {


    private static final KeyMapping.Category BACKPACK_CATEGORY =
        KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("backpack_pro", "general")
        );

    public static final KeyMapping OPEN_BACKPACK =
            KeyMappingHelper.registerKeyMapping(
                    new KeyMapping(
                            "key.backpack_pro.open_backpack",
                            InputConstants.Type.KEYSYM,
                            GLFW.GLFW_KEY_B,
                            BACKPACK_CATEGORY
                    )
            );

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_BACKPACK.consumeClick()) {
                if (client.player != null && client.getConnection() != null) {
                    ClientPlayNetworking.send(new OpenBackpackPayload());
                }
            }
        });
    }
}