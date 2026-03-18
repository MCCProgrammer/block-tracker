package com.mccprogrammer.blocktracker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

import java.util.List;

public class RadarRenderer {

    private static final int RADAR_SIZE_SMALL = 48;
    private static final int RADAR_SIZE_LARGE = 80;

    private static String lastEnemySpotted = null;
    private static int enemyNotifyTicks = 0;

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        try {
            ModState state = ModState.getInstance();
            if (!state.isRadarActive)
                return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null)
                return;

            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            int radarSize = state.isRadarZoomed ? RADAR_SIZE_LARGE : RADAR_SIZE_SMALL;
            int margin = 10;

            int centerX = screenWidth - radarSize - margin;
            int centerY = screenHeight - radarSize - margin;

            // Draw Radar Background
            context.fill(centerX - radarSize, centerY - radarSize, centerX + radarSize, centerY + radarSize,
                    0x90000000);

            // Draw Border
            int color = 0xFFFFFFFF;
            context.fill(centerX - radarSize, centerY - radarSize, centerX + radarSize, centerY - radarSize + 1, color);
            context.fill(centerX - radarSize, centerY + radarSize - 1, centerX + radarSize, centerY + radarSize, color);
            context.fill(centerX - radarSize, centerY - radarSize, centerX - radarSize + 1, centerY + radarSize, color);
            context.fill(centerX + radarSize - 1, centerY - radarSize, centerX + radarSize, centerY + radarSize, color);

            // Draw Player (Center)
            context.fill(centerX - 1, centerY - 1, centerX + 1, centerY + 1, 0xFFFFFFFF);

            Vec3d playerPos = new Vec3d(client.player.getBlockX(), client.player.getBlockY(),
                    client.player.getBlockZ());
            float yaw = client.player.getYaw();
            double angle = Math.toRadians(-yaw - 180);

            double scale = state.isRadarZoomed ? 4.0 : 2.0;

            // Draw Blocks
            for (String category : ModState.CATEGORY_ORDER) {
                List<BlockPos> blocks = state.foundBlocks.get(category);
                if (blocks == null)
                    continue;
                int blockColor = state.categoryColors.getOrDefault(category, 0xFFFFFFFF);

                for (BlockPos pos : blocks) {
                    if (pos == null)
                        continue;
                    double dx = pos.getX() - playerPos.x;
                    double dz = pos.getZ() - playerPos.z;

                    double rx = dx * Math.cos(angle) - dz * Math.sin(angle);
                    double rz = dx * Math.sin(angle) + dz * Math.cos(angle);

                    int px = (int) (rx / scale);
                    int py = (int) (rz / scale);

                    if (px * px + py * py < radarSize * radarSize) {
                        context.fill(centerX + px - 1, centerY + py - 1, centerX + px + 1, centerY + py + 1,
                                blockColor);
                    }
                }
            }

            // Draw Other Players
            boolean anyOtherInRadar = false;
            String firstOtherName = "";
            int otherCount = 0;

            for (net.minecraft.client.network.AbstractClientPlayerEntity otherPlayer : client.world.getPlayers()) {
                if (otherPlayer == null || otherPlayer == client.player)
                    continue;

                String pName = otherPlayer.getName().getString();
                String relation = state.playerRelations.getOrDefault(pName, "neutral");

                // Rules: Friends = Green, Others = White (max 5)
                boolean shouldRender = false;
                int pColor = 0xFFFFFFFF; // White by default

                if ("friend".equals(relation)) {
                    pColor = 0xFF00FF00; // Green
                    shouldRender = true;
                } else {
                    // This is an "Other" (neutral or enemy)
                    if (otherCount < 20) {
                        pColor = 0xFFFFFFFF; // White
                        anyOtherInRadar = true;
                        if (firstOtherName.isEmpty())
                            firstOtherName = pName;
                        shouldRender = true;
                        otherCount++;
                    }
                }

                if (shouldRender) {
                    double dx = otherPlayer.getX() - playerPos.x;
                    double dz = otherPlayer.getZ() - playerPos.z;

                    double rx = dx * Math.cos(angle) - dz * Math.sin(angle);
                    double rz = dx * Math.sin(angle) + dz * Math.cos(angle);

                    int px = (int) (rx / scale);
                    int py = (int) (rz / scale);

                    if (px * px + py * py < radarSize * radarSize) {
                        context.fill(centerX + px - 1, centerY + py - 1, centerX + px + 1, centerY + py + 1, pColor);
                    }
                }
            }

            // Other/Enemy Notification Logic
            // Show alert continuously if any non-friend is in radar
            if (anyOtherInRadar) {
                String text = "§cPLAYER DETETADO: " + firstOtherName;
                if (otherCount > 1) {
                    text += " (+" + (otherCount - 1) + ")";
                }

                int tw = client.textRenderer.getWidth(text);
                context.drawText(client.textRenderer, text, centerX - tw / 2, centerY - radarSize - 15, 0xFFFF0000,
                        true);
            }

            context.drawText(client.textRenderer, "Radar", centerX - 15, centerY - radarSize - 10, 0xFFFFFFFF, true);
        } catch (Exception e) {
            // Silently ignore or log to avoid crash
        }
    }
}
