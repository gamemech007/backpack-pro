package com.anantaya.backpackpro.inventory;

import com.anantaya.backpackpro.item.BackpackItem;
import com.anantaya.backpackpro.registry.ModMenus;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackpackMenu extends AbstractContainerMenu {

    public static final int MAIN_HAND_SLOT = -1;
    public static final int OFF_HAND_SLOT = -2;

    private final BackpackInventory inventory;
    private final ItemStack backpackStack;
    public final BackpackTier tier;

    private static ItemStack getStackFromSlotCode(Inventory playerInventory, int slotCode) {
        if (slotCode == MAIN_HAND_SLOT) {
            return playerInventory.player.getMainHandItem();
        }

        if (slotCode == OFF_HAND_SLOT) {
            return playerInventory.player.getOffhandItem();
        }

        if (slotCode >= 0 && slotCode < playerInventory.getContainerSize()) {
            return playerInventory.getItem(slotCode);
        }

        return ItemStack.EMPTY;
    }

    public BackpackMenu(
            int syncId,
            Inventory playerInventory,
            RegistryFriendlyByteBuf buf,
            BackpackTier tier
    ) {
        this(
                syncId,
                playerInventory,
                getStackFromSlotCode(playerInventory, buf.readVarInt()),
                tier,
                playerInventory.player.level().registryAccess()
        );
    }

    public BackpackMenu(
            int syncId,
            Inventory playerInventory,
            ItemStack stack,
            BackpackTier tier,
            RegistryAccess registryAccess
    ) {
        super(menuTypeForTier(tier), syncId);

        this.backpackStack = stack;
        this.tier = tier;
        this.inventory = new BackpackInventory(stack, tier.slotCount, registryAccess);

        for (int row = 0; row < tier.rows; row++) {
            for (int col = 0; col < tier.cols; col++) {
                this.addSlot(new Slot(
                        inventory,
                        col + row * tier.cols,
                        tier.backpackSlotX + col * 18,
                        tier.backpackSlotY + row * 18
                ) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return !(stack.getItem() instanceof BackpackItem);
                    }
                });
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        tier.playerInvX + col * 18,
                        tier.playerInvY + row * 18
                ));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(
                    playerInventory,
                    i,
                    tier.playerInvX + i * 18,
                    tier.hotbarY
            ));
        }
    }

    private static MenuType<BackpackMenu> menuTypeForTier(BackpackTier tier) {
        return switch (tier) {
            case IRON -> ModMenus.BACKPACK_MENU_IRON.get();
            case DIAMOND -> ModMenus.BACKPACK_MENU_DIAMOND.get();
            case NETHERITE -> ModMenus.BACKPACK_MENU_NETHERITE.get();
        };
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();

        if (stackInSlot.getItem() instanceof BackpackItem) {
            return ItemStack.EMPTY;
        }

        ItemStack result = stackInSlot.copy();

        boolean moved;

        if (index < tier.slotCount) {
            moved = this.moveItemStackTo(
                    stackInSlot,
                    tier.slotCount,
                    this.slots.size(),
                    true
            );
        } else {
            moved = this.moveItemStackTo(
                    stackInSlot,
                    0,
                    tier.slotCount,
                    false
            );
        }

        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stackInSlot.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stackInSlot);
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.getMainHandItem() == backpackStack) {
            return true;
        }

        if (player.getOffhandItem() == backpackStack) {
            return true;
        }

        Inventory inv = player.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (!stack.isEmpty() && stack == backpackStack) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if (!player.level().isClientSide()) {
            inventory.saveToData();
        }
    }
}