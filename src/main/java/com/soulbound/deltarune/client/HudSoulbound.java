package com.soulbound.deltarune.client;

import com.soulbound.deltarune.SoulboundMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HudSoulbound extends Gui {
    private static final ResourceLocation SOUL = new ResourceLocation("turnbattle", "textures/gui/spr_statsoul.png");
    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR
                || event.getType() == RenderGameOverlayEvent.ElementType.HEALTH
                || event.getType() == RenderGameOverlayEvent.ElementType.ARMOR
                || event.getType() == RenderGameOverlayEvent.ElementType.FOOD
                || event.getType() == RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            event.setCanceled(true);
            if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
                renderHud(event.getResolution());
            }
        }
    }

    private void renderHud(ScaledResolution res) {
        EntityPlayer p = mc.player;
        if (p == null) return;
        int w = 210, h = 46, x = res.getScaledWidth() / 2 - w / 2, y = res.getScaledHeight() - h - 4;
        box(x, y, w, h);
        mc.fontRenderer.drawString(p.getName().toUpperCase(), x + 7, y + 6, 0xFFFFFFFF);
        mc.fontRenderer.drawString("LV " + SoulboundMod.lv, x + 94, y + 6, 0xFFFFFFFF);
        mc.fontRenderer.drawString("HP", x + 125, y + 6, 0xFFFFFFFF);
        float r = Math.max(0f, Math.min(1f, p.getHealth() / p.getMaxHealth()));
        drawRect(x + 140, y + 5, x + 174, y + 13, 0xFF770000);
        drawRect(x + 140, y + 5, x + 140 + (int) (34 * r), y + 13, 0xFFFFFF00);
        mc.fontRenderer.drawString((int) Math.ceil(p.getHealth()) + "/" + (int) Math.ceil(p.getMaxHealth()), x + 176, y + 6, 0xFFFFFFFF);

        for (int i = 0; i < 9; i++) {
            int sx = x + 7 + i * 22, sy = y + 18;
            boolean sel = p.inventory.currentItem == i;
            drawRect(sx, sy, sx + 19, sy + 19, sel ? 0xFFFFFF00 : 0xFF555555);
            drawRect(sx + 1, sy + 1, sx + 18, sy + 18, 0xFF000000);
            ItemStack st = p.inventory.mainInventory.get(i);
            if (!st.isEmpty()) {
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(st, sx + 2, sy + 2);
                mc.getRenderItem().renderItemOverlays(mc.fontRenderer, st, sx + 2, sy + 2);
                RenderHelper.disableStandardItemLighting();
            }
            if (sel) {
                GlStateManager.color(1f, 1f, 1f, 1f);
                mc.getTextureManager().bindTexture(SOUL);
                drawModalRectWithCustomSizedTexture(sx - 2, sy - 2, 0, 0, 8, 8, 8, 8);
            }
        }
        drawRect(x + 7, y + 40, x + w - 7, y + 43, 0xFF332211);
        drawRect(x + 7, y + 40, x + 7 + (int) ((w - 14) * Math.min(1f, SoulboundMod.exp / 100f)), y + 43, 0xFFFF8822);

        box(x - 28, y + 20, 24, 26);
        if (!p.getHeldItemOffhand().isEmpty()) {
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(p.getHeldItemOffhand(), x - 24, y + 25);
            RenderHelper.disableStandardItemLighting();
        }
        box(x + w + 4, y + 20, 24, 26);
        String lvl = String.valueOf(p.experienceLevel);
        mc.fontRenderer.drawString(lvl, x + w + 16 - mc.fontRenderer.getStringWidth(lvl) / 2, y + 26, 0xFF55FF55);
        drawRect(x + w + 8, y + 38, x + w + 24, y + 41, 0xFF224422);
        drawRect(x + w + 8, y + 38, x + w + 8 + (int) (16 * p.experience), y + 41, 0xFF55FF55);
    }

    private void box(int x, int y, int w, int h) {
        drawRect(x, y, x + w, y + h, 0xFFFFFFFF);
        drawRect(x + 2, y + 2, x + w - 2, y + h - 2, 0xFF000000);
    }
}
