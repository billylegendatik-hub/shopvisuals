package dev.shopvisuals.modules;

import dev.shopvisuals.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

import java.util.ArrayDeque;
import java.util.Deque;

public class KeystrokesModule extends HudModule {
    private static final class Box {
        float t; // 0..1 press animation
        void update(boolean down) { t += ((down ? 1f : 0f) - t) * 0.3f; }
    }

    private final Box w = new Box(), a = new Box(), s = new Box(), d = new Box();
    private final Box lmb = new Box(), rmb = new Box(), space = new Box();

    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();
    private boolean lastLeft, lastRight;

    public KeystrokesModule() { super("Keystrokes", true); }

    @Override public boolean listed() { return false; }

    @Override
    public void tick(MinecraftClient mc) {
        GameOptions o = mc.options;
        long now = System.currentTimeMillis();
        boolean l = o.attackKey.isPressed(), r = o.useKey.isPressed();
        if (l && !lastLeft) leftClicks.add(now);
        if (r && !lastRight) rightClicks.add(now);
        lastLeft = l;
        lastRight = r;
        while (!leftClicks.isEmpty() && now - leftClicks.peekFirst() > 1000) leftClicks.pollFirst();
        while (!rightClicks.isEmpty() && now - rightClicks.peekFirst() > 1000) rightClicks.pollFirst();
    }

    @Override
    public void render(DrawContext g, MinecraftClient mc, float delta) {
        GameOptions o = mc.options;
        TextRenderer tr = mc.textRenderer;
        w.update(o.forwardKey.isPressed());
        a.update(o.leftKey.isPressed());
        s.update(o.backKey.isPressed());
        d.update(o.rightKey.isPressed());
        lmb.update(o.attackKey.isPressed());
        rmb.update(o.useKey.isPressed());
        space.update(o.jumpKey.isPressed());

        int x = 6, y = g.getScaledWindowHeight() / 2 - 40, k = 22, gap = 2;
        draw(g, tr, x + k + gap, y, k, k, key(o.forwardKey), w, null);
        draw(g, tr, x, y + k + gap, k, k, key(o.leftKey), a, null);
        draw(g, tr, x + k + gap, y + k + gap, k, k, key(o.backKey), s, null);
        draw(g, tr, x + 2 * (k + gap), y + k + gap, k, k, key(o.rightKey), d, null);

        int bw = (3 * k + 2 * gap - gap) / 2;
        int by = y + 2 * (k + gap);
        draw(g, tr, x, by, bw, k, "LMB", lmb, leftClicks.size() + " CPS");
        draw(g, tr, x + bw + gap, by, bw, k, "RMB", rmb, rightClicks.size() + " CPS");
        draw(g, tr, x, by + k + gap, 3 * k + 2 * gap, 12, "", space, null);
        // space bar glyph
        int cx = x + (3 * k + 2 * gap) / 2, cy = by + k + gap + 6;
        g.fill(cx - 8, cy, cx + 8, cy + 1, 0xFFFFFFFF);
    }

    private static String key(KeyBinding kb) {
        String s = kb.getBoundKeyLocalizedText().getString();
        return s.length() > 3 ? s.substring(0, 3) : s.toUpperCase();
    }

    private static void draw(DrawContext g, TextRenderer tr, int x, int y, int w, int h,
                             String label, Box box, String sub) {
        int bgAlpha = 0x70 + (int) (0x70 * box.t);
        int bg = (bgAlpha << 24) | 0x101014;
        g.fill(x, y, x + w, y + h, bg);
        if (box.t > 0.02f) {
            int a = (int) (0xFF * box.t);
            int ac = Theme.withAlpha(Theme.accent(x * 5), a);
            g.fill(x, y + h - 1, x + w, y + h, ac);
        }
        if (sub == null) {
            if (!label.isEmpty())
                g.drawText(tr, label, x + (w - tr.getWidth(label)) / 2, y + (h - 8) / 2, 0xFFFFFFFF, true);
        } else {
            g.drawText(tr, label, x + (w - tr.getWidth(label)) / 2, y + 3, 0xFFFFFFFF, true);
            g.drawText(tr, sub, x + (w - tr.getWidth(sub)) / 2, y + 12, 0xFFAAAAAA, false);
        }
    }
}
