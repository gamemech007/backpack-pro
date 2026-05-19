package com.anantaya.backpackpro.inventory;

/**
 * Defines slot count, GUI layout, and durability for each backpack tier.
 *
 *  Tier 1  Iron       5 slots  1 row of 5   3 durability
 *  Tier 2  Diamond    9 slots  1 row of 9   5 durability
 *  Tier 3  Netherite 18 slots  2 rows of 9  20 durability
 */
public enum BackpackTier {

    //              slots  rows  cols  maxDurability
    IRON      (  5,    1,    5,   3 ),
    DIAMOND   (  9,    1,    9,   5 ),
    NETHERITE ( 18,    2,    9,  20 );

    // ── slot counts ────────────────────────────────────────────────────────
    public final int slotCount;
    public final int rows;
    public final int cols;
    public final int maxDurability;

    // ── computed GUI layout (all relative to top-left of the GUI panel) ───
    public final int backpackSlotX;
    public final int backpackSlotY;
    public final int playerInvX;
    public final int playerInvY;
    public final int hotbarY;
    public final int guiHeight;

    BackpackTier(int slotCount, int rows, int cols, int maxDurability) {
        this.slotCount      = slotCount;
        this.rows           = rows;
        this.cols           = cols;
        this.maxDurability  = maxDurability;

        this.backpackSlotX = (176 - cols * 18) / 2;
        this.backpackSlotY = 24;

        this.playerInvX = 7;
        this.playerInvY = backpackSlotY + rows * 18 + 14;
        this.hotbarY    = playerInvY + 3 * 18 + 2;
        this.guiHeight  = hotbarY + 16 + 7;
    }
}