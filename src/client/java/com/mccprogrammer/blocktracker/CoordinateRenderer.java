package com.mccprogrammer.blocktracker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class CoordinateRenderer {

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        ModState state = ModState.getInstance();
        if (!state.isCoordinateHudActive) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        BlockPos pos = client.player.getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        String coordText = String.format("XYZ: %d, %d, %d", x, y, z);

        // Direction logic similar to F3
        Direction facing = client.player.getHorizontalFacing();
        String directionText = "Facing: " + facing.asString();

        String movementText = "";
        switch (facing) {
            case NORTH:
                movementText = " (Z a diminuir)";
                break;
            case SOUTH:
                movementText = " (Z a aumentar)";
                break;
            case WEST:
                movementText = " (X a diminuir)";
                break;
            case EAST:
                movementText = " (X a aumentar)";
                break;
            default:
                break;
        }

        String fullDirectionText = directionText + movementText;

        int screenWidth = client.getWindow().getScaledWidth();
        int margin = 5;
        int color = 0xFFFFFFFF; // White

        // Center position
        int coordWidth = client.textRenderer.getWidth(coordText);
        int dirWidth = client.textRenderer.getWidth(fullDirectionText);

        float coordScale = 1.3f;
        float dirScale = 1.1f;

        // Draw Coordinates
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(coordScale, coordScale);

        float centerXCoord = (screenWidth / 2.0f) / coordScale;
        context.drawText(client.textRenderer, coordText, (int) (centerXCoord - (coordWidth / 2.0f)),
                (int) (margin / coordScale), color, true);

        context.getMatrices().popMatrix();

        // Draw Direction
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(dirScale, dirScale);

        float centerXDir = (screenWidth / 2.0f) / dirScale;
        // Place it below coordinates. margin + 10 * coordScale = total Y of coord line.
        // Then divide by dirScale for the context.
        float dirY = (margin + 12 * coordScale) / dirScale;

        context.drawText(client.textRenderer, fullDirectionText, (int) (centerXDir - (dirWidth / 2.0f)), (int) dirY,
                color, true);

        context.getMatrices().popMatrix();
    }
}
