package com.mccprogrammer.blocktracker;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModState {
    private static ModState instance;

    // Toggles
    public boolean isGlowActive = false;
    public boolean isWaypointsActive = false;
    public boolean isRadarActive = false;
    public boolean isRadarZoomed = false;
    public boolean isPathfindingActive = false;
    public boolean isCoordinateHudActive = true;

    // Player Relationships
    // Player Name -> Relationship ("friend", "enemy")
    public final java.util.Map<String, String> playerRelations = new java.util.HashMap<>();

    // Constants
    public static final String CAT_CHEST = "Chest";
    public static final String CAT_SHULKER = "Shulker Box";
    public static final String CAT_SPAWNER = "Spawner";
    public static final String CAT_ENCHANTING = "Enchanting Table";

    public static final List<String> CATEGORY_ORDER = List.of(CAT_CHEST, CAT_SHULKER, CAT_SPAWNER, CAT_ENCHANTING);

    // Data
    public final Map<String, List<BlockPos>> foundBlocks = new HashMap<>();
    public List<BlockPos> currentPath = new ArrayList<>();
    public final Map<String, Integer> categoryColors = Map.of(
            CAT_CHEST, 0xFFFFAA00, // Gold
            CAT_SHULKER, 0xFFFF00FF, // Magenta
            CAT_SPAWNER, 0xFFFF0000, // Red
            CAT_ENCHANTING, 0xFF55FFFF // Aqua
    );

    private ModState() {
        // Initialize lists
        for (String cat : CATEGORY_ORDER) {
            foundBlocks.put(cat, new ArrayList<>());
        }
    }

    public static ModState getInstance() {
        if (instance == null) {
            instance = new ModState();
            ConfigManager.load(); // Load on first access
        }
        return instance;
    }

    public void save() {
        ConfigManager.save();
    }
}
