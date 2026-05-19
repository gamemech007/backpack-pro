package com.anantaya.backpackpro.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenBackpackPayload() implements CustomPacketPayload {

    public static final Type<OpenBackpackPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("anantaya", "open_backpack"));

    public static final StreamCodec<FriendlyByteBuf, OpenBackpackPayload> CODEC =
            StreamCodec.unit(new OpenBackpackPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}