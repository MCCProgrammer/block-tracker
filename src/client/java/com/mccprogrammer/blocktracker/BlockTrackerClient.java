package com.mccprogrammer.blocktracker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class BlockTrackerClient implements ClientModInitializer {

    // Keybindings
    private static KeyBinding menuKey;
    private static KeyBinding zoomKey;
    private static KeyBinding playerMenuKey;
    private static KeyBinding refreshKey;
    private static final String KEY_MENU = "key.blocktracker.menu";
    private static final String KEY_ZOOM = "key.blocktracker.zoom";
    private static final String KEY_PLAYER_MENU = "key.blocktracker.player_menu";
    private static final String KEY_REFRESH = "key.blocktracker.refresh";

    // State
    private int tickCounter = 0;
    private boolean lastWasScanning = false;

    @Override
    public void onInitializeClient() {
        // Register Keybindings
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_MENU,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KeyBinding.Category.MISC));

        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_ZOOM,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_ADD, // This is '+' on numpad, GLFW_KEY_EQUAL is '+' on top row
                KeyBinding.Category.MISC));

        playerMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_PLAYER_MENU,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                KeyBinding.Category.MISC));

        refreshKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_REFRESH,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K, // User didn't specify, I'll use K for 'Klean' or 'Re-Kontar'
                KeyBinding.Category.MISC));

        // Register HUD Renderer
        HudRenderCallback.EVENT.register(this::renderHud);

        // World Rendering is now handled via Mixin to avoid dependency issues

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null)
                return;

            // Handle Menu Key (O)
            while (menuKey.wasPressed()) {
                client.setScreen(new BlockConfigScreen());
            }

            // Handle Zoom Key (+)
            while (zoomKey.wasPressed()) {
                ModState state = ModState.getInstance();
                state.isRadarZoomed = !state.isRadarZoomed;
                client.player.sendMessage(Text.literal("Radar Zoom: " + (state.isRadarZoomed ? "ON" : "OFF")), true);
            }

            // Handle Player Menu Key (P)
            while (playerMenuKey.wasPressed()) {
                client.setScreen(new PlayerListScreen());
            }

            // Handle Refresh Key (K)
            while (refreshKey.wasPressed()) {
                forceScan(client);
                client.player.sendMessage(Text.literal("§aRadar e blocos atualizados!"), true);
            }

            // Auto-scan every 20 ticks (1 second)
            tickCounter++;
            if (tickCounter >= 20) {
                scanForBlocks(client);

                // Pathfinding logic
                ModState state = ModState.getInstance();
                if (state.isPathfindingActive) {
                    BlockPos playerPos = client.player.getBlockPos();
                    BlockPos closest = null;
                    double minSqDist = Double.MAX_VALUE;

                    for (List<BlockPos> list : state.foundBlocks.values()) {
                        BlockPos c = getClosest(playerPos, list);
                        if (c != null) {
                            // 2D Horizontal Distance
                            double dx = c.getX() - playerPos.getX();
                            double dz = c.getZ() - playerPos.getZ();
                            double d = dx * dx + dz * dz;

                            if (d < minSqDist) {
                                minSqDist = d;
                                closest = c;
                            }
                        }
                    }

                    if (closest != null) {
                        state.currentPath = PathFinder.findPath(playerPos, closest);
                    } else {
                        state.currentPath.clear();
                    }
                } else {
                    state.currentPath.clear();
                }

                tickCounter = 0;
            }
        });

        // Register Radar (part of HUD)
        HudRenderCallback.EVENT.register(RadarRenderer::render);

        // Register Coordinate HUD
        HudRenderCallback.EVENT.register(CoordinateRenderer::render);
    }

    public static void forceScan(MinecraftClient client) {
        if (client.player == null || client.world == null)
            return;

        ModState state = ModState.getInstance();
        // Clear previous results
        for (List<BlockPos> list : state.foundBlocks.values()) {
            list.clear();
        }

        ClientWorld world = client.world;
        int renderDistance = client.options.getClampedViewDistance();
        int playerChunkX = client.player.getChunkPos().x;
        int playerChunkZ = client.player.getChunkPos().z;

        for (int chunkX = playerChunkX - renderDistance; chunkX <= playerChunkX + renderDistance; chunkX++) {
            for (int chunkZ = playerChunkZ - renderDistance; chunkZ <= playerChunkZ + renderDistance; chunkZ++) {
                WorldChunk chunk = world.getChunk(chunkX, chunkZ);
                if (chunk == null)
                    continue;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    BlockPos pos = be.getPos();
                    String category = null;

                    if (be instanceof ChestBlockEntity) {
                        category = ModState.CAT_CHEST;
                    } else if (be instanceof ShulkerBoxBlockEntity) {
                        category = ModState.CAT_SHULKER;
                    } else if (be instanceof MobSpawnerBlockEntity) {
                        category = ModState.CAT_SPAWNER;
                    } else if (be instanceof EnchantingTableBlockEntity) {
                        category = ModState.CAT_ENCHANTING;
                    }

                    if (category != null) {
                        state.foundBlocks.get(category).add(pos);
                    }
                }
            }
        }
    }

    private void scanForBlocks(MinecraftClient client) {
        forceScan(client);
    }

    private void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null)
            return;

        ModState state = ModState.getInstance();

        int screenWidth = client.getWindow().getScaledWidth();
        int y = 10;
        int lineHeight = 10;
        int paddingStub = 5;

        for (String category : ModState.CATEGORY_ORDER) {
            List<BlockPos> blocks = state.foundBlocks.get(category);
            int color = state.categoryColors.getOrDefault(category, 0xFFFFFFFF);
            String text;

            if (blocks.isEmpty()) {
                text = category + ": None around";
            } else {
                int count = blocks.size();
                BlockPos closest = getClosest(client.player.getBlockPos(), blocks);
                if (closest != null) {
                    text = String.format("%s (%d): %d, %d, %d", category, count, closest.getX(), closest.getY(),
                            closest.getZ());
                } else {
                    text = category + ": Error";
                }
            }

            int textWidth = client.textRenderer.getWidth(text);
            int x = screenWidth - textWidth - paddingStub;

            context.drawText(client.textRenderer, text, x, y, color, true);
            y += lineHeight;
        }
    }

    private BlockPos getClosest(BlockPos playerPos, List<BlockPos> blocks) {
        BlockPos closest = null;
        double minSqDist = Double.MAX_VALUE;

        for (BlockPos pos : blocks) {
            // Horizontal Squared Distance (dx*dx + dz*dz)
            double dx = pos.getX() - playerPos.getX();
            double dz = pos.getZ() - playerPos.getZ();
            double sqDist = dx * dx + dz * dz;

            if (sqDist < minSqDist) {
                minSqDist = sqDist;
                closest = pos;
            }
        }
        return closest;
    }
}