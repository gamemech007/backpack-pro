package com.anantaya.backpackpro.item;

import com.anantaya.backpackpro.inventory.BackpackMenu;
import com.anantaya.backpackpro.inventory.BackpackTier;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class BackpackItem extends Item {

    private static final String DURABILITY_KEY = "BackpackDurability";

    private final BackpackTier tier;

    public BackpackItem(BackpackTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public BackpackTier getTier() {
        return tier;
    }

    public static int getDurability(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = (data != null) ? data.copyTag() : new CompoundTag();

        if (!tag.contains(DURABILITY_KEY)) {
            BackpackTier t = ((BackpackItem) stack.getItem()).tier;
            tag.putInt(DURABILITY_KEY, t.maxDurability);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            return t.maxDurability;
        }

        return tag.getInt(DURABILITY_KEY).orElse(0);
    }

    public static void setDurability(ItemStack stack, int value) {
        CustomData data = stack.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        );

        CompoundTag tag = data.copyTag();
        tag.putInt(DURABILITY_KEY, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getDurability(stack) < tier.maxDurability;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getDurability(stack) / tier.maxDurability);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float fraction = (float) getDurability(stack) / tier.maxDurability;

        int r = (int) Math.min(255, 255 * (1.0f - fraction) * 2);
        int g = (int) Math.min(255, 255 * fraction * 2);

        return (r << 16) | (g << 8);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {
        int dur = getDurability(stack);
        int maxDur = tier.maxDurability;

        ChatFormatting colour = dur <= 1 ? ChatFormatting.RED
                : dur <= maxDur / 2 ? ChatFormatting.YELLOW
                : ChatFormatting.GREEN;

        tooltip.accept(
                Component.literal("Durability: " + dur + " / " + maxDur)
                        .withStyle(colour)
        );

        tooltip.accept(
                Component.literal("Each death consumes 1 durability")
                        .withStyle(ChatFormatting.GRAY)
        );
    }

    public static void openBackpack(ServerPlayer serverPlayer, ItemStack stack, int slotCode) {
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof BackpackItem backpackItem)) return;

        serverPlayer.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Backpack");
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInv, Player player) {
                return new BackpackMenu(
                        syncId,
                        playerInv,
                        stack,
                        backpackItem.getTier(),
                        player.level().registryAccess()
                );
            }
        }, buf -> buf.writeVarInt(slotCode));
    }

    public static boolean openFirstBackpack(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();

        if (!mainHand.isEmpty() && mainHand.getItem() instanceof BackpackItem) {
            openBackpack(player, mainHand, BackpackMenu.MAIN_HAND_SLOT);
            return true;
        }

        ItemStack offhand = player.getOffhandItem();

        if (!offhand.isEmpty() && offhand.getItem() instanceof BackpackItem) {
            openBackpack(player, offhand, BackpackMenu.OFF_HAND_SLOT);
            return true;
        }

        Inventory inv = player.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (!stack.isEmpty() && stack.getItem() instanceof BackpackItem) {
                openBackpack(player, stack, i);
                return true;
            }
        }

        return false;
    }

    private static int getSlotCodeForStack(Player player, ItemStack target) {
        if (player.getMainHandItem() == target) {
            return BackpackMenu.MAIN_HAND_SLOT;
        }

        if (player.getOffhandItem() == target) {
            return BackpackMenu.OFF_HAND_SLOT;
        }

        Inventory inv = player.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i) == target) {
                return i;
            }
        }

        return BackpackMenu.MAIN_HAND_SLOT;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            openBackpack(serverPlayer, stack, getSlotCodeForStack(player, stack));
        }

        return level.isClientSide()
                ? InteractionResult.SUCCESS
                : InteractionResult.SUCCESS_SERVER;
    }
}