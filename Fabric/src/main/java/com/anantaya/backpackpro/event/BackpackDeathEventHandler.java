package com.anantaya.backpackpro.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.anantaya.backpackpro.BackpackItem;

import java.util.Optional;

public class BackpackDeathEventHandler {

    private static final Map<UUID, ItemStack> savedBackpacks = new HashMap<>();

    static {
        System.out.println("[Backpack] BackpackDeathEventHandler class loaded");
    }

    public static void register() {
        System.out.println("[Backpack] Registering death event handlers...");

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof ServerPlayer player) {
                saveBackpackBeforeDeath(player);
            }
            return true;
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            UUID playerId = oldPlayer.getUUID();
            ItemStack backpack = savedBackpacks.remove(playerId);

            if (backpack == null || backpack.isEmpty()) {
                System.out.println("[Backpack] No saved backpack for "
                        + newPlayer.getName().getString());
                return;
            }

            MinecraftServer server = newPlayer.level().getServer();
            HolderLookup.Provider registries = server.registryAccess();

            server.execute(() -> {
                ServerPlayer currentPlayer = server.getPlayerList().getPlayer(playerId);

                if (currentPlayer == null) {
                    System.out.println("[Backpack] Player " + playerId
                            + " not online at restore time — skipping.");
                    return;
                }

                int currentDur = BackpackItem.getDurability(backpack);
                int newDur     = currentDur - 1;

                System.out.println("[Backpack] Durability for "
                        + currentPlayer.getName().getString()
                        + ": " + currentDur + " -> " + newDur);

                if (newDur <= 0) {
                    System.out.println("[Backpack] Durability reached 0 — destroying backpack");

                    // 1.21: read NBT via CustomData component
                    CustomData customData = backpack.get(DataComponents.CUSTOM_DATA);
if (customData != null) {
    CompoundTag tag = customData.copyTag();

    if (tag.contains("Inventory")) {
        CompoundTag invTag = tag.getCompound("Inventory").orElse(new CompoundTag());
        int slotCount = ((BackpackItem) backpack.getItem()).getTier().slotCount;

        for (int s = 0; s < slotCount; s++) {
            String key = "Slot" + s;

            if (invTag.contains(key)) {
                ItemStack toDrop = ItemStack.EMPTY;

                Optional<CompoundTag> itemTagOptional = invTag.getCompound(key);

                if (itemTagOptional.isPresent()) {
                    toDrop = ItemStack.CODEC
                            .parse(registries.createSerializationContext(NbtOps.INSTANCE), itemTagOptional.get())
                            .result()
                            .orElse(ItemStack.EMPTY);
                }

                if (!toDrop.isEmpty()) {
                    currentPlayer.drop(toDrop, false);
                }
            }
        }
    }
}

                    currentPlayer.sendSystemMessage(
                        Component.literal("Your backpack has broken and disappeared!")
                            .setStyle(Style.EMPTY.withColor(0xFF5555))
                    );
                    return;
                }

                BackpackItem.setDurability(backpack, newDur);

                if (newDur == 1) {
                    currentPlayer.sendSystemMessage(
                        Component.literal("Warning: Your backpack has only 1 durability left!")
                            .setStyle(Style.EMPTY.withColor(0xFF5555))
                    );
                } else if (newDur <= ((BackpackItem) backpack.getItem()).getTier().maxDurability / 2) {
                    currentPlayer.sendSystemMessage(
                        Component.literal("Your backpack durability is low: " + newDur + " remaining.")
                            .setStyle(Style.EMPTY.withColor(0xFFAA00))
                    );
                }

                System.out.println("[Backpack] Restoring backpack for "
                        + currentPlayer.getName().getString());

                boolean added = currentPlayer.addItem(backpack);
                System.out.println("[Backpack] addItem returned: " + added);

                if (added) {
                    currentPlayer.inventoryMenu.broadcastChanges();
                    System.out.println("[Backpack] ✓ Restored and synced for "
                            + currentPlayer.getName().getString());
                } else {
                    System.out.println("[Backpack] Inventory full — dropping at spawn");
                    currentPlayer.drop(backpack, false);
                }
            });
        });

        System.out.println("[Backpack] Death event handlers registered!");
    }

    private static void saveBackpackBeforeDeath(ServerPlayer player) {
        System.out.println("[Backpack] ALLOW_DEATH fired for " + player.getName().getString());

        HolderLookup.Provider registries = player.level().getServer().registryAccess();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BackpackItem) {
                System.out.println("[Backpack] Found backpack at slot " + i);

                // 1.21: get NBT via CustomData component
                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                if (customData != null) {
                    CompoundTag tag = customData.copyTag();

                    if (tag.contains("Inventory")) {
                        CompoundTag invTag = tag.getCompound("Inventory").orElse(new CompoundTag());

                        List<String> occupiedSlots = new ArrayList<>();
                        for (int s = 0; s < 5; s++) {
                            if (invTag.contains("Slot" + s)) {
                                occupiedSlots.add("Slot" + s);
                            }
                        }

                        if (!occupiedSlots.isEmpty()) {
                            String randomKey = occupiedSlots.get(
                                    player.getRandom().nextInt(occupiedSlots.size())
                            );

                            // 1.21: parse ItemStack with registry access
                            ItemStack toDrop = ItemStack.EMPTY;

Optional<CompoundTag> itemTagOptional = invTag.getCompound(randomKey);

if (itemTagOptional.isPresent()) {
    toDrop = ItemStack.CODEC
            .parse(registries.createSerializationContext(NbtOps.INSTANCE), itemTagOptional.get())
            .result()
            .orElse(ItemStack.EMPTY);
}

if (!toDrop.isEmpty()) {
    player.drop(toDrop, true);
}
                            invTag.remove(randomKey);

                            // Write modified tag back to the component
                            tag.put("Inventory", invTag);
                            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                            System.out.println("[Backpack] Dropped from backpack: " + toDrop.getItem());
                        }
                    }
                }

                ItemStack backpackCopy = stack.copy();
                savedBackpacks.put(player.getUUID(), backpackCopy);
                player.getInventory().setItem(i, ItemStack.EMPTY);

                System.out.println("[Backpack] ✓ Saved backpack for "
                        + player.getName().getString());
                break;
            }
        }
    }
}