package dev.shopvisuals;

import dev.shopvisuals.modules.HudModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ModuleScreen extends Screen {
    public ModuleScreen() { super(Text.literal("ShopVisuals")); }

    @Override
    protected void init() {
        int i = 0;
        for (HudModule m : ShopVisuals.MODULES) {
            addDrawableChild(ButtonWidget.builder(label(m), b -> {
                m.toggle();
                b.setMessage(label(m));
                Config.save();
            }).dimensions(width / 2 - 70, 44 + i * 24, 140, 20).build());
            i++;
        }
    }

    private static Text label(HudModule m) {
        return Text.literal(m.name + ": " + (m.enabled ? "\u00a7aON" : "\u00a7cOFF"));
    }

    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredTextWithShadow(textRenderer, "ShopVisuals", width / 2, 24, Theme.accent(0));
    }
}
