package com.anantaya.backpackpro.client;

import com.anantaya.backpackpro.BackPackPro;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import org.lwjgl.glfw.GLFW;

public class BackpackKeybinds {

    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(BackPackPro.MODID, "main")
    );

    public static final KeyMapping OPEN_BACKPACK = new KeyMapping(
            "key.backpackpro.open",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY
    );
}