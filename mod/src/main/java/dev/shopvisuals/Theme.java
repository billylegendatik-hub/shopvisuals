package dev.shopvisuals;

import net.minecraft.util.math.MathHelper;

public final class Theme {
    private Theme() {}

    /** Animated rainbow accent. offset shifts the hue so rows differ. */
    public static int accent(int offset) {
        float hue = ((System.currentTimeMillis() / 18 + offset) % 360) / 360f;
        return 0xFF000000 | MathHelper.hsvToRgb(hue, 0.55f, 1f);
    }

    public static int withAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }
}
