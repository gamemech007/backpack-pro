package com.anantaya.backpackpro.client;

import com.anantaya.backpackpro.BackpackScreenHandler;
import com.anantaya.backpackpro.BackpackTier;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class BackpackScreen extends AbstractContainerScreen<BackpackScreenHandler> {

    private final BackpackTier tier;
    private static final String MOD_ID = "backpack-pro";

    public BackpackScreen(BackpackScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, handler.tier.guiHeight);
        this.tier = handler.tier;
    }

    private Identifier getTextureLocation() {
        return switch (tier) {
            case IRON -> Identifier.fromNamespaceAndPath(
                    MOD_ID,
                    "textures/gui/backpack_iron_gui.png"
            );

            case DIAMOND -> Identifier.fromNamespaceAndPath(
                    MOD_ID,
                    "textures/gui/backpack_diamond_gui.png"
            );

            case NETHERITE -> Identifier.fromNamespaceAndPath(
                    MOD_ID,
                    "textures/gui/backpack_netherite_gui.png"
            );
        };
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = tier.playerInvY - 11;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                getTextureLocation(),
                x,
                y,
                0,
                0,
                this.imageWidth,
                this.imageHeight,
                256,
                256
        );

        for (Slot slot : this.menu.slots) {
            int sx = x + slot.x - 1;
            int sy = y + slot.y - 1;

            graphics.fill(sx, sy, sx + 18, sy + 1, 0xFF373737);
            graphics.fill(sx, sy, sx + 1, sy + 18, 0xFF373737);
            graphics.fill(sx + 1, sy + 17, sx + 18, sy + 18, 0xFFFFFFFF);
            graphics.fill(sx + 17, sy + 1, sx + 18, sy + 18, 0xFFFFFFFF);
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
        }

        super.extractContents(graphics, mouseX, mouseY, delta);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // No labels rendered
    }
}