package com.mccprogrammer.blocktracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("blocktracker_state.json")
            .toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save() {
        ModState state = ModState.getInstance();
        SaveData data = new SaveData();
        data.playerRelations = state.playerRelations;
        // Add other persistent data here if needed (e.g. toggles)

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Type type = new TypeToken<SaveData>() {
            }.getType();
            SaveData data = GSON.fromJson(reader, type);

            if (data != null && data.playerRelations != null) {
                ModState.getInstance().playerRelations.putAll(data.playerRelations);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class SaveData {
        public Map<String, String> playerRelations = new HashMap<>();
    }
}
