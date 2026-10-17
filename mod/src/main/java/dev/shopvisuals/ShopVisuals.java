package dev.shopvisuals;

import dev.shopvisuals.modules.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ShopVisuals implements ClientModInitializer {
    public static final List<HudModule> MODULES = new ArrayList<>();
    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        MODULES.add(new WatermarkModule());
        MODULES.add(new ArrayListModule());
        MODULES.add(new KeystrokesModule());
        MODULES.add(new InfoModule());
        Config.load();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shopvisuals.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.shopvisuals"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            for (HudModule m : MODULES) m.tick(mc);
            while (menuKey.wasPressed()) mc.setScreen(new ModuleScreen());
        });

        HudRenderCallback.EVENT.register((g, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options.hudHidden || mc.getDebugHud().shouldShowDebugHud()) return;
            float delta = tickCounter.getTickDelta(false);
            for (HudModule m : MODULES) if (m.enabled) m.render(g, mc, delta);
        });
    }
}
