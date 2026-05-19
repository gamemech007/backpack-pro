package com.anantaya.backpackpro;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anantaya.backpackpro.network.BackpackNetworking;

import java.util.ArrayList;
import java.util.List;

public class BackpackPro implements ModInitializer {

    public static final String MOD_ID = "backpack-pro";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // ── Menu types ─────────────────────────────────────────────────────────
    public static final MenuType<BackpackScreenHandler> BACKPACK_MENU_IRON =
            Registry.register(BuiltInRegistries.MENU,
                    Identifier.fromNamespaceAndPath(MOD_ID, "backpack_iron"),
                    new MenuType<>((syncId, inv) ->
                        new BackpackScreenHandler(syncId, inv,
                                inv.player.getMainHandItem(), BackpackTier.IRON,
                                inv.player.level().registryAccess()),
                            FeatureFlags.DEFAULT_FLAGS));

    public static final MenuType<BackpackScreenHandler> BACKPACK_MENU_DIAMOND =
            Registry.register(BuiltInRegistries.MENU,
                    Identifier.fromNamespaceAndPath(MOD_ID, "backpack_diamond"),
                    new MenuType<>((syncId, inv) ->
                            new BackpackScreenHandler(syncId, inv, inv.player.getMainHandItem(), BackpackTier.DIAMOND,
                                    inv.player.level().registryAccess()),
                            FeatureFlags.DEFAULT_FLAGS));

    public static final MenuType<BackpackScreenHandler> BACKPACK_MENU_NETHERITE =
            Registry.register(BuiltInRegistries.MENU,
                    Identifier.fromNamespaceAndPath(MOD_ID, "backpack_netherite"),
                    new MenuType<>((syncId, inv) ->
                            new BackpackScreenHandler(syncId, inv, inv.player.getMainHandItem(), BackpackTier.NETHERITE,
                                    inv.player.level().registryAccess()),
                            FeatureFlags.DEFAULT_FLAGS));

    // ── Items ──────────────────────────────────────────────────────────────
    public static final Item BACKPACK_IRON =
        Registry.register(
                BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(MOD_ID, "backpack_iron"),
                new BackpackItem(
                        BackpackTier.IRON,
                        new Item.Properties()
                                .setId(ResourceKey.create(
                                        Registries.ITEM,
                                        Identifier.fromNamespaceAndPath(MOD_ID, "backpack_iron")
                                ))
                                .stacksTo(1)
                )
        );

    public static final Item BACKPACK_DIAMOND =
        Registry.register(
                BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(MOD_ID, "backpack_diamond"),
                new BackpackItem(
                        BackpackTier.DIAMOND,
                        new Item.Properties()
                                .setId(ResourceKey.create(
                                        Registries.ITEM,
                                        Identifier.fromNamespaceAndPath(MOD_ID, "backpack_diamond")
                                ))
                                .stacksTo(1)
                )
        );

        

    public static final Item BACKPACK_NETHERITE =
        Registry.register(
                BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(MOD_ID, "backpack_netherite"),
                new BackpackItem(
                        BackpackTier.NETHERITE,
                        new Item.Properties()
                                .setId(ResourceKey.create(
                                        Registries.ITEM,
                                        Identifier.fromNamespaceAndPath(MOD_ID, "backpack_netherite")
                                ))
                                .stacksTo(1)
                )
        );

    @Override
    public void onInitialize() {
        BackpackNetworking.register();
        com.anantaya.backpackpro.event.BackpackDeathEventHandler.register();

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
        .register(output -> {
            output.accept(BACKPACK_IRON);
            output.accept(BACKPACK_DIAMOND);
            output.accept(BACKPACK_NETHERITE);
        });
        // Unlock all backpack recipes for every player on join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            List<RecipeHolder<?>> recipes = new ArrayList<>();
server.getRecipeManager().byKey(
        ResourceKey.create(
                Registries.RECIPE,
                Identifier.fromNamespaceAndPath(MOD_ID, "backpack_iron")
        )
).ifPresent(recipes::add);
server.getRecipeManager().byKey(
        ResourceKey.create(
                Registries.RECIPE,
                Identifier.fromNamespaceAndPath(MOD_ID, "backpack_diamond")
        )
).ifPresent(recipes::add);

server.getRecipeManager().byKey(
        ResourceKey.create(
                Registries.RECIPE,
                Identifier.fromNamespaceAndPath(MOD_ID, "backpack_netherite")
        )
).ifPresent(recipes::add);
player.awardRecipes(recipes);
        });

        // Enforce only one backpack per player — drop extras immediately
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                List<Integer> backpackSlots = new ArrayList<>();

                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.getItem() instanceof BackpackItem) {
                        backpackSlots.add(i);
                    }
                }

                // Keep the first found, drop all others
                if (backpackSlots.size() > 1) {
                    for (int i = 1; i < backpackSlots.size(); i++) {
                        int slot = backpackSlots.get(i);
                        ItemStack toDrop = player.getInventory().getItem(slot).copy();
                        player.getInventory().setItem(slot, ItemStack.EMPTY);
                        player.drop(toDrop, false);
                    }

                    // Show red warning message
                    player.sendSystemMessage(
                        Component.literal("Only one backpack can be equipped at a time!")
                            .setStyle(Style.EMPTY.withColor(0xFF5555))
                    );
                }
            }
        });

        LOGGER.info("Backpack Pro loaded — 3 tiers registered!");
    }
}