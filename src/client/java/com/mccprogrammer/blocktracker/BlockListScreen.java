package com.mccprogrammer.blocktracker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockListScreen extends Screen {

    private final ModState state;

    public BlockListScreen() {
        super(Text.literal("Block Tracker Results"));
        this.state = ModState.getInstance();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x90000000);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null)
            return;

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int radarSize = Math.min(this.width, this.height) / 2 - 40;

        // Draw Radar Circle Background
        // context.fill(centerX - radarSize, centerY - radarSize, centerX + radarSize,
        // centerY + radarSize, 0x40000000);

        // Draw Axis
        context.fill(centerX - radarSize, centerY, centerX + radarSize, centerY + 1, 0x40FFFFFF);
        context.fill(centerX, centerY - radarSize, centerX + 1, centerY + radarSize, 0x40FFFFFF);

        // Player Center
        context.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, 0xFFFFFFFF);

        double playerX = client.player.getX();
        double playerZ = client.player.getZ();
        float yaw = client.player.getYaw();
        double angle = Math.toRadians(-yaw - 180);

        // Scale: 1 pixel = 1 block (adjusted to fit radarSize)
        // Let's use a scale that fits the render distance
        int renderDistance = client.options.getClampedViewDistance() * 16;
        double scale = (double) renderDistance / radarSize;

        String hoverInfo = null;
        int hoverX = 0, hoverY = 0;

        // Draw Blocks
        for (String category : ModState.CATEGORY_ORDER) {
            List<BlockPos> blocks = state.foundBlocks.get(category);
            int color = state.categoryColors.getOrDefault(category, 0xFFFFFF);

            for (BlockPos pos : blocks) {
                double dx = pos.getX() - playerX;
                double dz = pos.getZ() - playerZ;

                // Rotation
                double rx = dx * Math.cos(angle) - dz * Math.sin(angle);
                double rz = dx * Math.sin(angle) + dz * Math.cos(angle);

                int px = (int) (rx / scale);
                int py = (int) (rz / scale);

                if (px * px + py * py < radarSize * radarSize) {
                    int screenX = centerX + px;
                    int screenY = centerY + py;
                    context.fill(screenX - 1, screenY - 1, screenX + 1, screenY + 1, color);

                    // Hover check
                    if (Math.abs(mouseX - screenX) < 3 && Math.abs(mouseY - screenY) < 3) {
                        hoverInfo = String.format("§6%s§f\nPos: %d, §b%d§f, %d\ndist: %.1f",
                                category, pos.getX(), pos.getY(), pos.getZ(), Math.sqrt(dx * dx + dz * dz));
                        hoverX = mouseX;
                        hoverY = mouseY;
                    }
                }
            }
        }

        // Draw Players
        for (net.minecraft.client.network.AbstractClientPlayerEntity player : client.world.getPlayers()) {
            if (player == client.player)
                continue;

            String name = player.getName().getString();
            String relation = state.playerRelations.getOrDefault(name, "neutral");
            int color = 0xFFFFFFFF;
            if ("friend".equals(relation))
                color = 0xFF00FF00;
            else if ("enemy".equals(relation))
                color = 0xFFFF0000;

            double dx = player.getX() - playerX;
            double dz = player.getZ() - playerZ;

            double rx = dx * Math.cos(angle) - dz * Math.sin(angle);
            double rz = dx * Math.sin(angle) + dz * Math.cos(angle);

            int px = (int) (rx / scale);
            int py = (int) (rz / scale);

            if (px * px + py * py < radarSize * radarSize) {
                int screenX = centerX + px;
                int screenY = centerY + py;
                context.fill(screenX - 2, screenY - 2, screenX + 2, screenY + 2, color);

                if (Math.abs(mouseX - screenX) < 4 && Math.abs(mouseY - screenY) < 4) {
                    hoverInfo = String.format("§e%s§f\nPos: %d, §b%d§f, %d\nRel: %s",
                            name, player.getBlockX(), player.getBlockY(), player.getBlockZ(), relation);
                    hoverX = mouseX;
                    hoverY = mouseY;
                }
            }
        }

        // Tooltip
        if (hoverInfo != null) {
            String[] lines = hoverInfo.split("\n");
            int tw = 0;
            for (String l : lines)
                tw = Math.max(tw, this.textRenderer.getWidth(l));

            int tx = hoverX + 12;
            int ty = hoverY + 12;

            // Boundary check for tooltip
            if (tx + tw > this.width)
                tx = hoverX - tw - 12;
            if (ty + lines.length * 10 > this.height)
                ty = hoverY - lines.length * 10 - 12;

            context.fill(tx - 3, ty - 3, tx + tw + 3, ty + (lines.length * 10) + 3, 0xD0000000);

            // Manual border using fill
            int borderColor = 0xFFAAAAAA;
            int totalH = lines.length * 10;
            context.fill(tx - 4, ty - 4, tx + tw + 4, ty - 3, borderColor); // Top
            context.fill(tx - 4, ty + totalH + 3, tx + tw + 4, ty + totalH + 4, borderColor); // Bottom
            context.fill(tx - 4, ty - 3, tx - 3, ty + totalH + 3, borderColor); // Left
            context.fill(tx + tw + 3, ty - 3, tx + tw + 4, ty + totalH + 3, borderColor); // Right

            for (int i = 0; i < lines.length; i++) {
                context.drawText(this.textRenderer, lines[i], tx, ty + i * 10, 0xFFFFFFFF, true);
            }
        }

        // Legend
        int ly = this.height - 30;
        context.drawText(this.textRenderer, "Hover over dots to see Coords (Y is blue)", 10, ly, 0xFFAAAAAA, true);
        context.drawText(this.textRenderer, "Scale: 1:" + String.format("%.1f", scale), 10, ly + 10, 0xFFAAAAAA, true);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
