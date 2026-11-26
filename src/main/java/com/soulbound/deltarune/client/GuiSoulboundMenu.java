package com.soulbound.deltarune.client;

import com.soulbound.deltarune.SoulboundMod;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

public class GuiSoulboundMenu extends GuiScreen {
    private int tab = 0, statSel = 0;

    private String getTab(int i) {
        if (i == 0) return "STATS";
        if (i == 1) return "SKILLS";
        if (i == 2) return "PARTY";
        if (i == 3) return "EQUIP";
        if (i == 4) return "ITEMS";
        return "SETTINGS";
    }

    private String getStatName(int i) {
        if (i == 0) return "OFFENSE";
        if (i == 1) return "DEFENSE";
        if (i == 2) return "MAGIC";
        if (i == 3) return "VITALITY";
        if (i == 4) return "IQ";
        return "LUCK";
    }

    private int getStatVal(int i) {
        if (i == 0) return SoulboundMod.offense;
        if (i == 1) return SoulboundMod.defense;
        if (i == 2) return SoulboundMod.magic;
        if (i == 3) return SoulboundMod.vitality;
        if (i == 4) return SoulboundMod.iq;
        return SoulboundMod.luck;
    }

    private ResourceLocation getIcon(int i) {
        if (i == 0) return new ResourceLocation("turnbattle", "textures/gui/spr_offenseicon.png");
        if (i == 1) return new ResourceLocation("turnbattle", "textures/gui/spr_defenseicon.png");
        if (i == 2) return new ResourceLocation("turnbattle", "textures/gui/spr_magicicon.png");
        if (i == 3) return new ResourceLocation("turnbattle", "textures/gui/spr_vitalityicon.png");
        if (i == 4) return new ResourceLocation("turnbattle", "textures/gui/spr_iqicon.png");
        return new ResourceLocation("turnbattle", "textures/gui/spr_luckicon.png");
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawRect(0, 0, width, height, 0xEE000000);
        drawRect(10, 24, width - 10, height - 10, 0xFFFFFFFF);
        drawRect(12, 26, width - 12, height - 12, 0xFF000000);

        drawString(fontRenderer, getTab(tab), 14, 10, 0xFFFFFFFF);
        drawString(fontRenderer, "D$ " + SoulboundMod.gold, width - 60, 10, 0xFFFFFF55);

        for (int i = 0; i < 6; i++) {
            int tx = 20 + i * 68;
            drawString(fontRenderer, getTab(i), tx, 32, tab == i ? 0xFFFFFF55 : 0xFFAAAAAA);
            if (tab == i) drawRect(tx, 42, tx + 42, 44, 0xFFFF0000);
        }
        drawRect(12, 46, width - 12, 47, 0xFFFFFFFF);

        int mid = width / 2;
        drawString(fontRenderer, mc.player.getName().toUpperCase(), 22, 56, 0xFFFFFFFF);
        drawString(fontRenderer, "LV " + SoulboundMod.lv, mid - 65, 56, 0xFFFFFF55);
        GuiInventory.drawEntityOnScreen(62, 138, 34, -25f, 0f, mc.player);

        drawString(fontRenderer, "HP  " + (int) mc.player.getHealth() + "/" + (int) mc.player.getMaxHealth(), 22, 152, 0xFFFFFFFF);
        drawRect(22, 163, mid - 25, 169, 0xFFFF0000);
        drawString(fontRenderer, "EXP " + SoulboundMod.exp + "/100", 22, 175, 0xFFFFFFFF);
        drawRect(22, 186, mid - 25, 192, 0xFF444400);
        drawRect(22, 186, 22 + (int) ((mid - 47) * Math.min(1f, SoulboundMod.exp / 100f)), 192, 0xFFFFFF00);

        drawRect(mid - 15, 47, mid - 14, height - 46, 0xFFFFFFFF);
        for (int i = 0; i < 6; i++) {
            int sy = 64 + i * 18;
            mc.getTextureManager().bindTexture(getIcon(i));
            drawModalRectWithCustomSizedTexture(mid - 4, sy - 1, 0, 0, 10, 10, 10, 10);
            drawString(fontRenderer, (SoulboundMod.sp > 0 ? "+ " : "  ") + getStatName(i), mid + 10, sy, statSel == i ? 0xFFFFFF55 : 0xFFFFFFFF);
            drawString(fontRenderer, String.valueOf(getStatVal(i)), width - 36, sy, 0xFFFFFF55);
        }
        drawString(fontRenderer, "SP " + SoulboundMod.sp, mid - 4, height - 60, 0xFFFFFF55);

        drawRect(12, height - 46, width - 12, height - 45, 0xFFFFFFFF);
        drawString(fontRenderer, "INFO", 20, height - 39, 0xFF8888AA);
        drawString(fontRenderer, mc.player.getName() + " has " + SoulboundMod.sp + " SP. [ENTER] Spend SP  [LEFT/RIGHT] Tab", 20, height - 26, 0xFFFFFF55);
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE || key == Keyboard.KEY_C) mc.displayGuiScreen(null);
        else if (key == Keyboard.KEY_LEFT) tab = (tab + 5) % 6;
        else if (key == Keyboard.KEY_RIGHT) tab = (tab + 1) % 6;
        else if (key == Keyboard.KEY_UP || key == Keyboard.KEY_W) statSel = (statSel + 5) % 6;
        else if (key == Keyboard.KEY_DOWN || key == Keyboard.KEY_S) statSel = (statSel + 1) % 6;
        else if ((key == Keyboard.KEY_RETURN || key == Keyboard.KEY_SPACE) && SoulboundMod.sp > 0) {
            SoulboundMod.sp--;
            if (statSel == 0) SoulboundMod.offense++;
            else if (statSel == 1) SoulboundMod.defense++;
            else if (statSel == 2) SoulboundMod.magic++;
            else if (statSel == 3) { SoulboundMod.vitality++; mc.player.heal(2f); }
            else if (statSel == 4) SoulboundMod.iq++;
            else if (statSel == 5) SoulboundMod.luck++;
        }
    }
}
