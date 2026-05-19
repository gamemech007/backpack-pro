package com.anantaya.backpackpro.registry;

import com.anantaya.backpackpro.BackPackPro;
import com.anantaya.backpackpro.inventory.BackpackTier;
import com.anantaya.backpackpro.item.BackpackItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(BackPackPro.MODID);

    public static final DeferredItem<Item> BACKPACK_IRON =
            ITEMS.register("backpack_iron",
                    () -> new BackpackItem(
                            BackpackTier.IRON,
                            new Item.Properties()
                                    .setId(ResourceKey.create(
                                            Registries.ITEM,
                                            Identifier.fromNamespaceAndPath(
                                                    BackPackPro.MODID,
                                                    "backpack_iron"
                                            )
                                    ))
                                    .stacksTo(1)
                    ));

    public static final DeferredItem<Item> BACKPACK_DIAMOND =
            ITEMS.register("backpack_diamond",
                    () -> new BackpackItem(
                            BackpackTier.DIAMOND,
                            new Item.Properties()
                                    .setId(ResourceKey.create(
                                            Registries.ITEM,
                                            Identifier.fromNamespaceAndPath(
                                                    BackPackPro.MODID,
                                                    "backpack_diamond"
                                            )
                                    ))
                                    .stacksTo(1)
                    ));

    public static final DeferredItem<Item> BACKPACK_NETHERITE =
            ITEMS.register("backpack_netherite",
                    () -> new BackpackItem(
                            BackpackTier.NETHERITE,
                            new Item.Properties()
                                    .setId(ResourceKey.create(
                                            Registries.ITEM,
                                            Identifier.fromNamespaceAndPath(
                                                    BackPackPro.MODID,
                                                    "backpack_netherite"
                                            )
                                    ))
                                    .stacksTo(1)
                    ));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}