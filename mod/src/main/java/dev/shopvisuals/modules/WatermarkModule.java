package dev.shopvisuals.modules;

import dev.shopvisuals.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class WatermarkModule extends HudModule {
    public WatermarkModule() { super("Watermark", true); }

    @Override public boolean listed() { return false; }

    @Override
    public void render(DrawContext g, MinecraftClient mc, float delta) {
        TextRenderer tr = mc.textRenderer;
        String ping = "";
        if (mc.player != null && mc.getNetworkHandler() != null) {
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (e != null) ping = e.getLatency() + "ms";
        }
        String name = mc.getSession().getUsername();
        String text = "ShopVisuals  |  " + name + "  |  " + mc.getCurrentFps() + " fps"
                + (ping.isEmpty() ? "" : "  |  " + ping);

        int x = 6, y = 6, w = tr.getWidth(text) + 10, h = 15;
        g.fill(x, y, x + w, y + h, 0xA0101014);
        // animated gradient top line
        int step = Math.max(1, w / 32);
        for (int i = 0; i < w; i += step) {
            g.fill(x + i, y, Math.min(x + i + step, x + w), y + 1, Theme.accent(i * 3));
        }
        g.drawText(tr, text, x + 5, y + 4, 0xFFFFFFFF, true);
    }
}
