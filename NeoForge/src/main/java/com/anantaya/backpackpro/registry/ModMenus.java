package com.anantaya.backpackpro.registry;

import com.anantaya.backpackpro.BackPackPro;
import com.anantaya.backpackpro.inventory.BackpackMenu;
import com.anantaya.backpackpro.inventory.BackpackTier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, BackPackPro.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<BackpackMenu>> BACKPACK_MENU_IRON =
            MENUS.register("backpack_iron", () ->
                    IMenuTypeExtension.create((syncId, inv, buf) ->
                            new BackpackMenu(syncId, inv, buf, BackpackTier.IRON)
                    )
            );

    public static final DeferredHolder<MenuType<?>, MenuType<BackpackMenu>> BACKPACK_MENU_DIAMOND =
            MENUS.register("backpack_diamond", () ->
                    IMenuTypeExtension.create((syncId, inv, buf) ->
                            new BackpackMenu(syncId, inv, buf, BackpackTier.DIAMOND)
                    )
            );

    public static final DeferredHolder<MenuType<?>, MenuType<BackpackMenu>> BACKPACK_MENU_NETHERITE =
            MENUS.register("backpack_netherite", () ->
                    IMenuTypeExtension.create((syncId, inv, buf) ->
                            new BackpackMenu(syncId, inv, buf, BackpackTier.NETHERITE)
                    )
            );

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}