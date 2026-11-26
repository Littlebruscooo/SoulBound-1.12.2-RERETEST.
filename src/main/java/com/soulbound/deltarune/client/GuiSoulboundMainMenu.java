package com.soulbound.deltarune.client;

import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiModList;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.Random;

public class GuiSoulboundMainMenu extends GuiScreen {
    private static final ResourceLocation LOGO = new ResourceLocation("turnbattle", "textures/gui/soulboundlogo_title.png");
    private static final ResourceLocation SOUL = new ResourceLocation("turnbattle", "textures/gui/spr_statsoul.png");
    private int sel = 0;

    private String getItem(int i) {
        if (i == 0) return "SINGLEPLAYER";
        if (i == 1) return "MULTIPLAYER";
        if (i == 2) return "MODS";
        if (i == 3) return "OPTIONS";
        return "QUIT";
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xFF040714, 0xFF0B1530);
        drawRect(width / 2 - 45, 0, width / 2 + 45, height, 0x185588FF);
        Random r = new Random(42L);
        for (int i = 0; i < 70; i++) {
            int sx = r.nextInt(Math.max(1, width));
            int sy = r.nextInt(Math.max(1, height));
            drawRect(sx, sy, sx + 2, sy + 2, 0xAAFFFFFF);
        }
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(LOGO);
        drawModalRectWithCustomSizedTexture(width / 2 - 120, 26, 0, 0, 240, 64, 240, 64);

        int startY = height / 2 - 10;
        for (int i = 0; i < 5; i++) {
            int iy = startY + i * 22;
            String label = getItem(i);
            int tw = fontRenderer.getStringWidth(label);
            int ix = width / 2 - tw / 2;
            if (mouseX >= ix - 20 && mouseX <= ix + tw + 20 && mouseY >= iy - 3 && mouseY <= iy + 13) sel = i;
            drawString(fontRenderer, label, ix, iy, sel == i ? 0xFFFFFF55 : 0xFFFFFFFF);
            if (sel == i) {
                mc.getTextureManager().bindTexture(SOUL);
                drawModalRectWithCustomSizedTexture(ix - 18, iy - 2, 0, 0, 12, 12, 12, 12);
            }
        }
        drawString(fontRenderer, "Minecraft 1.12.2 - Soulbound", 6, height - 12, 0xFFAAAAAA);
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (key == Keyboard.KEY_UP || key == Keyboard.KEY_W) sel = (sel + 4) % 5;
        else if (key == Keyboard.KEY_DOWN || key == Keyboard.KEY_S) sel = (sel + 1) % 5;
        else if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_SPACE) activate();
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        if (btn == 0) activate();
    }

    private void activate() {
        if (sel == 0) mc.displayGuiScreen(new GuiWorldSelection(this));
        else if (sel == 1) mc.displayGuiScreen(new GuiMultiplayer(this));
        else if (sel == 2) mc.displayGuiScreen(new GuiModList(this));
        else if (sel == 3) mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
        else if (sel == 4) mc.shutdown();
    }
}
