package com.anantaya.backpackpro.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import com.anantaya.backpackpro.BackpackItem;
import com.anantaya.backpackpro.BackpackPro;
import com.anantaya.backpackpro.BackpackScreenHandler;
import com.anantaya.backpackpro.BackpackTier;

public class BackpackNetworking {

    public static void register() {
        // Register the payload type first (Client to Server - SERVERBOUND)
        PayloadTypeRegistry.serverboundPlay().register(OpenBackpackPayload.TYPE, OpenBackpackPayload.CODEC);
        
        ServerPlayNetworking.registerGlobalReceiver(
                OpenBackpackPayload.TYPE,
                (payload, context) -> {

                    ServerPlayer player = context.player();

                    // run on server thread
                    context.server().execute(() -> openBackpackMenu(player));
                }
        );
    }

    private static void openBackpackMenu(ServerPlayer player) {
        Inventory inv = player.getInventory();

        ItemStack foundStack = null;
        int foundSlot        = -1;

        // ── Check main inventory ───────────────────────────────────────────
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BackpackItem) {
                foundStack = stack;
                foundSlot  = i;
                break;
            }
        }

        // ── Check offhand ──────────────────────────────────────────────────
        if (foundStack == null) {
            ItemStack offhand = inv.getItem(40);
            if (!offhand.isEmpty() && offhand.getItem() instanceof BackpackItem) {
                foundStack = offhand;
                foundSlot  = 40;
            }
        }

        if (foundStack == null) {
            BackpackPro.LOGGER.warn("[Backpack] Player {} pressed B but has no backpack!",
                    player.getName().getString());
            return;
        }

        BackpackTier tier   = ((BackpackItem) foundStack.getItem()).getTier();
        final int slot      = foundSlot;
        final ItemStack ref = foundStack;

        String title = switch (tier) {
            case IRON      -> "Backpack";
            case DIAMOND   -> "Backpack II";
            case NETHERITE -> "Backpack III";
        };

        player.openMenu(new SimpleMenuProvider(
                (syncId, playerInv, p) -> {
                    ItemStack latest = p.getInventory().getItem(slot);
                    if (latest.isEmpty() || !(latest.getItem() instanceof BackpackItem)) {
                        latest = ref;
                    }
                    return new BackpackScreenHandler(syncId, playerInv, latest, tier,
        p.level().registryAccess());
                },
                Component.literal(title)
        ));
    }
}