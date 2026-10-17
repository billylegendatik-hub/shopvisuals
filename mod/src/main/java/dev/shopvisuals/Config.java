package dev.shopvisuals;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.shopvisuals.modules.HudModule;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class Config {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("shopvisuals.json");
    private static final Gson GSON = new Gson();

    private Config() {}

    public static void load() {
        try {
            if (!Files.exists(FILE)) return;
            Map<String, Boolean> map = GSON.fromJson(Files.readString(FILE),
                    new TypeToken<Map<String, Boolean>>() {}.getType());
            if (map == null) return;
            for (HudModule m : ShopVisuals.MODULES) {
                Boolean v = map.get(m.name);
                if (v != null) m.enabled = v;
            }
        } catch (Exception e) {
            System.err.println("[shopvisuals] failed to load config: " + e);
        }
    }

    public static void save() {
        try {
            Map<String, Boolean> map = new HashMap<>();
            for (HudModule m : ShopVisuals.MODULES) map.put(m.name, m.enabled);
            Files.writeString(FILE, GSON.toJson(map));
        } catch (Exception e) {
            System.err.println("[shopvisuals] failed to save config: " + e);
        }
    }
}
