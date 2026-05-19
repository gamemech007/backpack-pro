package com.anantaya.backpackpro.network;

import com.anantaya.backpackpro.BackPackPro;
import com.anantaya.backpackpro.item.BackpackItem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenBackpackPayload() implements CustomPacketPayload {

    public static final OpenBackpackPayload INSTANCE = new OpenBackpackPayload();

    public static final CustomPacketPayload.Type<OpenBackpackPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(BackPackPro.MODID, "open_backpack")
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBackpackPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenBackpackPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        // Do not open while another menu is already open
        if (player.containerMenu != player.inventoryMenu) return;

        BackpackItem.openFirstBackpack(player);
    }
}