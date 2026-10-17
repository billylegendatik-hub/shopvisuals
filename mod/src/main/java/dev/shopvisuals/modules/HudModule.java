package dev.shopvisuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class HudModule {
    public final String name;
    public boolean enabled;

    protected HudModule(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public void toggle() { enabled = !enabled; }

    /** Optional text shown next to the name in the module list. */
    public String info() { return ""; }

    /** Whether this module appears in the on-screen module list. */
    public boolean listed() { return true; }

    public void tick(MinecraftClient mc) {}

    public abstract void render(DrawContext g, MinecraftClient mc, float delta);
}
