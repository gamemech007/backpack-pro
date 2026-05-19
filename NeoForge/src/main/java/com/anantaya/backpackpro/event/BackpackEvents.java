package com.anantaya.backpackpro.event;

import com.anantaya.backpackpro.BackPackPro;
import com.anantaya.backpackpro.item.BackpackItem;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BackpackEvents {

    private static final Map<UUID, ItemStack> SAVED_BACKPACKS = new HashMap<>();
    private static final Map<UUID, Boolean> LAST_DURABILITY = new HashMap<>();
    private static final Map<UUID, ItemStack> RESPAWN_DROP_BACKPACKS = new HashMap<>();
    private static final Map<UUID, Integer> RESPAWN_DROP_TICK_COUNTER = new HashMap<>();
    private static final Map<UUID, Long> LAST_ONE_BACKPACK_MESSAGE_TICK = new HashMap<>();

    public static void register() {
        NeoForge.EVENT_BUS.register(new BackpackEvents());
    }

    @SubscribeEvent
public void onPlayerTick(PlayerTickEvent.Post event) {
    if (!(event.getEntity() instanceof ServerPlayer player)) return;
    if (player.level().isClientSide()) return;

    enforceOneBackpack(player);
}

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        MinecraftServer server = player.level().getServer();
if (server == null) return;

RecipeManager recipes = server.getRecipeManager();

        List<RecipeHolder<?>> recipesToUnlock = new ArrayList<>();

        addRecipeIfExists(recipes, recipesToUnlock, "backpack_iron");
        addRecipeIfExists(recipes, recipesToUnlock, "backpack_diamond");
        addRecipeIfExists(recipes, recipesToUnlock, "backpack_netherite");

        if (!recipesToUnlock.isEmpty()) {
            player.awardRecipes(recipesToUnlock);
        }
    }

    private static void addRecipeIfExists(
            RecipeManager recipes,
            List<RecipeHolder<?>> recipesToUnlock,
            String recipePath
    ) {
        recipes.byKey(
                ResourceKey.create(
                        Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(BackPackPro.MODID, recipePath)
                )
        ).ifPresent(recipesToUnlock::add);
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        MinecraftServer server = player.level().getServer();
        if (server == null) return;

        HolderLookup.Provider registries = server.registryAccess();

        for (ItemEntity itemEntity : new ArrayList<>(event.getDrops())) {
            ItemStack stack = itemEntity.getItem();

            if (!stack.isEmpty() && stack.getItem() instanceof BackpackItem backpackItem) {
                int currentDur = BackpackItem.getDurability(stack);
                boolean isLastDurability = (currentDur - 1) <= 0;

                dropOneRandomBackpackItem(player, stack, backpackItem, registries);

                SAVED_BACKPACKS.put(player.getUUID(), stack.copy());
                LAST_DURABILITY.put(player.getUUID(), isLastDurability);

                event.getDrops().remove(itemEntity);
                break;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        UUID playerId = event.getOriginal().getUUID();
        ItemStack backpack = SAVED_BACKPACKS.remove(playerId);
        Boolean isLastDurability = LAST_DURABILITY.remove(playerId);

        if (backpack == null || backpack.isEmpty()) return;

        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;

        MinecraftServer server = newPlayer.level().getServer();
        if (server == null) return;

        if (isLastDurability != null && isLastDurability) {
            RESPAWN_DROP_BACKPACKS.put(playerId, backpack);
            return;
        }

        int currentDur = BackpackItem.getDurability(backpack);
        int newDur = currentDur - 1;

        BackpackItem.setDurability(backpack, newDur);

        if (newDur == 1) {
            newPlayer.sendSystemMessage(
                    Component.literal("Warning: Your backpack has only 1 durability left!")
                            .setStyle(Style.EMPTY.withColor(0xFF5555))
            );
        } else if (newDur <= ((BackpackItem) backpack.getItem()).getTier().maxDurability / 2) {
            newPlayer.sendSystemMessage(
                    Component.literal("Your backpack durability is low: " + newDur + " remaining.")
                            .setStyle(Style.EMPTY.withColor(0xFFAA00))
            );
        }

        boolean added = newPlayer.addItem(backpack);

        if (added) {
            newPlayer.inventoryMenu.broadcastChanges();
        } else {
            newPlayer.drop(backpack, false);
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        List<UUID> toRemove = new ArrayList<>();

        for (UUID playerId : RESPAWN_DROP_BACKPACKS.keySet()) {
            int tickCount = RESPAWN_DROP_TICK_COUNTER.getOrDefault(playerId, 0);

            if (tickCount >= 5) {
                ItemStack backpack = RESPAWN_DROP_BACKPACKS.get(playerId);
                ServerPlayer player = server.getPlayerList().getPlayer(playerId);

                if (player != null && backpack != null) {
                    HolderLookup.Provider registries = server.registryAccess();
                    dropAllBackpackContents(player, backpack, registries);

                    player.sendSystemMessage(
                            Component.literal("Your backpack has broken and disappeared!")
                                    .setStyle(Style.EMPTY.withColor(0xFF5555))
                    );
                }

                toRemove.add(playerId);
            } else {
                RESPAWN_DROP_TICK_COUNTER.put(playerId, tickCount + 1);
            }
        }

        for (UUID playerId : toRemove) {
            RESPAWN_DROP_BACKPACKS.remove(playerId);
            RESPAWN_DROP_TICK_COUNTER.remove(playerId);
        }
    }

    private static void dropOneRandomBackpackItem(
            ServerPlayer player,
            ItemStack backpack,
            BackpackItem backpackItem,
            HolderLookup.Provider registries
    ) {
        CustomData customData = backpack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;

        CompoundTag tag = customData.copyTag();
        if (!tag.contains("Inventory")) return;

        CompoundTag invTag = tag.getCompound("Inventory").orElse(new CompoundTag());

        List<String> occupiedSlots = new ArrayList<>();

        for (int s = 0; s < backpackItem.getTier().slotCount; s++) {
            String key = "Slot" + s;

            if (invTag.contains(key)) {
                occupiedSlots.add(key);
            }
        }

        if (occupiedSlots.isEmpty()) return;

        String randomKey = occupiedSlots.get(player.getRandom().nextInt(occupiedSlots.size()));
        ItemStack toDrop = readItem(invTag, randomKey, registries);

        if (!toDrop.isEmpty()) {
            player.drop(toDrop, true);
        }

        invTag.remove(randomKey);
        tag.put("Inventory", invTag);
        backpack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static void dropAllBackpackContents(
            ServerPlayer player,
            ItemStack backpack,
            HolderLookup.Provider registries
    ) {
        CustomData customData = backpack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;

        CompoundTag tag = customData.copyTag();
        if (!tag.contains("Inventory")) return;

        CompoundTag invTag = tag.getCompound("Inventory").orElse(new CompoundTag());
        int slotCount = ((BackpackItem) backpack.getItem()).getTier().slotCount;

        for (int s = 0; s < slotCount; s++) {
            String key = "Slot" + s;

            if (invTag.contains(key)) {
                ItemStack toDrop = readItem(invTag, key, registries);

                if (!toDrop.isEmpty()) {
                    player.drop(toDrop, false);
                }
            }
        }
        }

    private static void enforceOneBackpack(ServerPlayer player) {
    boolean foundOne = false;
    boolean droppedExtra = false;

    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
        ItemStack stack = player.getInventory().getItem(i);

        if (stack.isEmpty()) continue;
        if (!(stack.getItem() instanceof BackpackItem)) continue;

        if (!foundOne) {
            foundOne = true;
            continue;
        }

        ItemStack extraBackpack = stack.copy();
        player.getInventory().setItem(i, ItemStack.EMPTY);
        player.drop(extraBackpack, false);
        droppedExtra = true;
    }

    if (droppedExtra) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        sendOneBackpackMessage(player);
    }
}

    private static void sendOneBackpackMessage(ServerPlayer player) {
        long currentTick = player.level().getGameTime();
        long lastTick = LAST_ONE_BACKPACK_MESSAGE_TICK.getOrDefault(player.getUUID(), -200L);

        if (currentTick - lastTick < 40) return;

        LAST_ONE_BACKPACK_MESSAGE_TICK.put(player.getUUID(), currentTick);

        player.sendSystemMessage(
                Component.literal("You can only carry one backpack at once!")
                        .setStyle(Style.EMPTY.withColor(0xFF5555))
        );
    }

    private static ItemStack readItem(
            CompoundTag invTag,
            String key,
            HolderLookup.Provider registries
    ) {
        Optional<CompoundTag> itemTagOptional = invTag.getCompound(key);

        if (itemTagOptional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return ItemStack.CODEC
                .parse(
                        registries.createSerializationContext(NbtOps.INSTANCE),
                        itemTagOptional.get()
                )
                .result()
                .orElse(ItemStack.EMPTY);
    }
}