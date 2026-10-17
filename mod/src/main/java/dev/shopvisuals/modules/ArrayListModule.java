package dev.shopvisuals.modules;

import dev.shopvisuals.Theme;
import dev.shopvisuals.ShopVisuals;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArrayListModule extends HudModule {
    public ArrayListModule() { super("Module List", true); }

    @Override public boolean listed() { return false; }

    @Override
    public void render(DrawContext g, MinecraftClient mc, float delta) {
        TextRenderer tr = mc.textRenderer;
        List<HudModule> list = new ArrayList<>();
        for (HudModule m : ShopVisuals.MODULES) if (m.enabled && m.listed()) list.add(m);
        list.sort(Comparator.comparingInt((HudModule m) -> tr.getWidth(label(m))).reversed());

        int sw = g.getScaledWindowWidth();
        int y = 4, rowH = 12, i = 0;
        for (HudModule m : list) {
            String name = m.name, info = m.info();
            int w = tr.getWidth(label(m)) + 8;
            int x = sw - w - 3;
            int accent = Theme.accent(i * 40);
            g.fill(x, y, sw - 2, y + rowH, 0x90101014);
            g.fill(sw - 2, y, sw, y + rowH, accent); // right accent bar
            g.drawText(tr, name, x + 4, y + 2, accent, true);
            if (!info.isEmpty()) {
                g.drawText(tr, " " + info, x + 4 + tr.getWidth(name), y + 2, 0xFFAAAAAA, true);
            }
            y += rowH;
            i++;
        }
    }

    private static String label(HudModule m) {
        return m.info().isEmpty() ? m.name : m.name + " " + m.info();
    }
}
