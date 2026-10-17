package dev.shopvisuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Coordinates + speed readout, bottom-left. */
public class InfoModule extends HudModule {
    private double speed;
    private double lx, lz;

    public InfoModule() { super("Info", true); }

    @Override
    public String info() { return String.format("%.1f b/s", speed); }

    @Override
    public void tick(MinecraftClient mc) {
        if (mc.player == null) return;
        double dx = mc.player.getX() - lx, dz = mc.player.getZ() - lz;
        speed = Math.sqrt(dx * dx + dz * dz) * 20.0; // 20 ticks/s
        lx = mc.player.getX();
        lz = mc.player.getZ();
    }

    @Override
    public void render(DrawContext g, MinecraftClient mc, float delta) {
        if (mc.player == null) return;
        String xyz = String.format("XYZ  %.1f  %.1f  %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
        String spd = String.format("Speed  %.2f b/s", speed);
        int h = g.getScaledWindowHeight();
        g.drawText(mc.textRenderer, xyz, 6, h - 22, 0xFFFFFFFF, true);
        g.drawText(mc.textRenderer, spd, 6, h - 12, 0xFFBBBBBB, true);
    }
}
