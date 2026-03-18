package com.mccprogrammer.blocktracker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class BlockConfigScreen extends Screen {

    public BlockConfigScreen() {
        super(Text.literal("Block Tracker Config"));
    }

    @Override
    protected void init() {
        ModState state = ModState.getInstance();
        int centerX = this.width / 2;
        int startY = this.height / 4;
        int btnWidth = 200;
        int btnHeight = 20;
        int padding = 24;

        // Toggle Glow (DISABLED)
        /*
         * this.addDrawableChild(ButtonWidget.builder(
         * Text.literal("Glow: " + (state.isGlowActive ? "ON" : "OFF")),
         * button -> {
         * state.isGlowActive = !state.isGlowActive;
         * button.setMessage(Text.literal("Glow: " + (state.isGlowActive ? "ON" :
         * "OFF")));
         * })
         * .dimensions(centerX - btnWidth / 2, startY, btnWidth, btnHeight)
         * .build());
         */

        // Toggle Waypoints (DISABLED)
        /*
         * this.addDrawableChild(ButtonWidget.builder(
         * Text.literal("Waypoints: " + (state.isWaypointsActive ? "ON" : "OFF")),
         * button -> {
         * state.isWaypointsActive = !state.isWaypointsActive;
         * button.setMessage(Text.literal("Waypoints: " + (state.isWaypointsActive ?
         * "ON" : "OFF")));
         * })
         * .dimensions(centerX - btnWidth / 2, startY + padding, btnWidth, btnHeight)
         * .build());
         */

        // Toggle Radar
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Radar: " + (state.isRadarActive ? "ON" : "OFF")),
                button -> {
                    state.isRadarActive = !state.isRadarActive;
                    button.setMessage(Text.literal("Radar: " + (state.isRadarActive ? "ON" : "OFF")));
                })
                .dimensions(centerX - btnWidth / 2, startY + padding * 2, btnWidth, btnHeight)
                .build());

        // Toggle Coordinate HUD
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Coordinate HUD: " + (state.isCoordinateHudActive ? "ON" : "OFF")),
                button -> {
                    state.isCoordinateHudActive = !state.isCoordinateHudActive;
                    button.setMessage(Text.literal("Coordinate HUD: " + (state.isCoordinateHudActive ? "ON" : "OFF")));
                })
                .dimensions(centerX - btnWidth / 2, startY + padding * 3, btnWidth, btnHeight)
                .build());

        // Toggle Pathfinding (DISABLED)
        /*
         * this.addDrawableChild(ButtonWidget.builder(
         * Text.literal("Pathfinding: " + (state.isPathfindingActive ? "ON" : "OFF")),
         * button -> {
         * state.isPathfindingActive = !state.isPathfindingActive;
         * button.setMessage(Text.literal("Pathfinding: " + (state.isPathfindingActive ?
         * "ON" : "OFF")));
         * })
         * .dimensions(centerX - btnWidth / 2, startY + padding * 3, btnWidth,
         * btnHeight)
         * .build());
         */

        // Force Refresh
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§aForce Refresh (Radar & Blocks)"),
                button -> {
                    if (this.client != null) {
                        BlockTrackerClient.forceScan(this.client);
                    }
                })
                .dimensions(centerX - btnWidth / 2, startY + padding * 4 + 10, btnWidth, btnHeight)
                .build());

        // Open List Screen
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("View Found Blocks List"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new BlockListScreen());
                    }
                })
                .dimensions(centerX - btnWidth / 2, startY + padding * 5 + 15, btnWidth, btnHeight)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Use manual fill to avoid "Can only blur once per frame" crash if multiple
        // screens/blur events coincide
        context.fill(0, 0, this.width, this.height, 0x90000000);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
