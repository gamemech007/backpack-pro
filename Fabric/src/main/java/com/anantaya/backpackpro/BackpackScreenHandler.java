package com.anantaya.backpackpro;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackpackScreenHandler extends AbstractContainerMenu {

    private final BackpackInventory inventory;
    private final ItemStack backpackStack;
    public final BackpackTier tier;

    public BackpackScreenHandler(int syncId, Inventory playerInventory,
                                 ItemStack stack, BackpackTier tier,
                                 RegistryAccess registryAccess) {
        super(menuTypeForTier(tier), syncId);
        this.backpackStack = stack;
        this.tier          = tier;
        this.inventory     = new BackpackInventory(stack, tier.slotCount, registryAccess); // ← fixed

        // ── Backpack slots ─────────────────────────────────────────────────
        for (int row = 0; row < tier.rows; row++) {
            for (int col = 0; col < tier.cols; col++) {
                this.addSlot(new Slot(inventory,
                        col + row * tier.cols,
                        tier.backpackSlotX + col * 18,
                        tier.backpackSlotY + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return !(stack.getItem() instanceof BackpackItem);
                    }
                });
            }
        }

        // ── Player inventory (3 rows × 9 columns) ─────────────────────────
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory,
                        col + row * 9 + 9,
                        tier.playerInvX + col * 18,
                        tier.playerInvY + row * 18));
            }
        }

        // ── Hotbar (9 slots) ───────────────────────────────────────────────
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i,
                    tier.playerInvX + i * 18,
                    tier.hotbarY));
        }
    }

    private static MenuType<BackpackScreenHandler> menuTypeForTier(BackpackTier tier) {
        return switch (tier) {
            case IRON      -> BackpackPro.BACKPACK_MENU_IRON;
            case DIAMOND   -> BackpackPro.BACKPACK_MENU_DIAMOND;
            case NETHERITE -> BackpackPro.BACKPACK_MENU_NETHERITE;
        };
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        if (stackInSlot.getItem() instanceof BackpackItem) return ItemStack.EMPTY;

        ItemStack result = stackInSlot.copy();

        boolean moved;
        if (index < tier.slotCount) {
            moved = this.moveItemStackTo(stackInSlot, tier.slotCount, this.slots.size(), true);
        } else {
            moved = this.moveItemStackTo(stackInSlot, 0, tier.slotCount, false);
        }

        if (!moved) return ItemStack.EMPTY;

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stackInSlot.getCount() == result.getCount()) return ItemStack.EMPTY;

        slot.onTake(player, stackInSlot);
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (!s.isEmpty() && s == backpackStack) return true;
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        inventory.saveToData(); // ← fixed (was saveToNbt)
    }
}