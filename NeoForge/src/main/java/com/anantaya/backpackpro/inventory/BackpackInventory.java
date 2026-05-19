package com.anantaya.backpackpro.inventory;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public class BackpackInventory extends SimpleContainer {

    private final ItemStack stack;
    private final RegistryAccess registryAccess;

    public BackpackInventory(ItemStack stack, int size, RegistryAccess registryAccess) {
        super(size);
        this.stack = stack;
        this.registryAccess = registryAccess;
        loadFromData();
    }

    public void loadFromData() {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;

        CompoundTag tag = customData.copyTag();

        if (!tag.contains("Inventory")) return;

        CompoundTag invTag = tag.getCompound("Inventory").orElse(new CompoundTag());

        for (int i = 0; i < getContainerSize(); i++) {
            String key = "Slot" + i;

            if (!invTag.contains(key)) continue;

            Optional<CompoundTag> itemTagOptional = invTag.getCompound(key);

            if (itemTagOptional.isPresent()) {
                ItemStack item = ItemStack.CODEC
                        .parse(
                                registryAccess.createSerializationContext(NbtOps.INSTANCE),
                                itemTagOptional.get()
                        )
                        .result()
                        .orElse(ItemStack.EMPTY);

                setItem(i, item);
            }
        }
    }

    public void saveToData() {
        CustomData customData = stack.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        );

        CompoundTag tag = customData.copyTag();
        CompoundTag invTag = new CompoundTag();

        for (int i = 0; i < getContainerSize(); i++) {
            final int slot = i;
            ItemStack item = getItem(i);

            if (!item.isEmpty()) {
                ItemStack.CODEC
                        .encodeStart(
                                registryAccess.createSerializationContext(NbtOps.INSTANCE),
                                item
                        )
                        .result()
                        .ifPresent(tagData -> invTag.put("Slot" + slot, tagData));
            }
        }

        tag.put("Inventory", invTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        saveToData();
    }
}