        int fill = (int) (tpH * (tp / 100f));
        if (fill > 0) {
            mc.getTextureManager().bindTexture(TP1);
            drawModalRectWithCustomSizedTexture(14, 28 + tpH - fill, 0, 0, 16, fill, 16, tpH);
        }
        mc.fontRenderer.drawStringWithShadow("TP " + tp + "%", 34, 38, 0xFFFFAA00);
        int stageY = dockTop - 36;
        int px = w / 4 + 12 + (pose == 1 ? 32 : 0);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GuiInventory.drawEntityOnScreen(px, stageY - (pose == 2 ? 8 : 0), 42, -40f, 0f, mc.player);
        int ex = (w * 3) / 4 - 12 + (pose == 1 ? (poseTicks % 4 - 2) * 3 : 0);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GuiInventory.drawEntityOnScreen(ex, stageY, 42, 40f, 0f, enemy);
        GlStateManager.disableLighting();
        GlStateManager.color(1f, 1f, 1f, 1f);
        if (pose == 1) {
            int sy = stageY - 62 + (16 - poseTicks) * 3;
            drawRect(ex - 26, sy, ex + 26, sy + 4, crit ? 0xFFFFFF00 : 0xFF00FFFF);
            drawCenteredString(mc.fontRenderer, (crit ? "CRIT -" : "-") + lastDmg, ex, stageY - 118, crit ? 0xFFFFFF00 : 0xFFFF5555);
        }
        mc.fontRenderer.drawStringWithShadow(enemy.getName(), ex - 35, stageY - 104, 0xFFFFFFFF);
        drawRect(ex - 35, stageY - 92, ex + 35, stageY - 86, 0xFF551111);
        drawRect(ex - 35, stageY - 92, ex - 35 + (int) (70 * Math.max(0f, enemy.getHealth() / enemy.getMaxHealth())), stageY - 86, 0xFF33FF33);
        drawRect(ex - 35, stageY - 84, ex + 35, stageY - 79, 0xFF443300);
        drawRect(ex - 35, stageY - 84, ex - 35 + (int) (70 * (mercy / 100f)), stageY - 79, 0xFFFFFF00);
        int colW = 216, colH = 44, colX = w / 2 - colW / 2, colY = dockTop - colH + 2;
        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.getTextureManager().bindTexture(COL);
        drawModalRectWithCustomSizedTexture(colX, colY, 0, 0, colW, colH, colW, colH);
        mc.fontRenderer.drawString(mc.player.getName().toUpperCase(), colX + 24, colY + 5, 0xFFFFFFFF);
        mc.fontRenderer.drawString((int) Math.ceil(mc.player.getHealth()) + "/" + (int) Math.ceil(mc.player.getMaxHealth()), colX + 150, colY + 5, 0xFFFFFFFF);
    }
}
package com.soulbound.deltarune.client;

import com.soulbound.deltarune.SoulboundMod;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.*;

public class GuiDeltaruneBattle extends GuiScreen {
    private static final ResourceLocation BOX_TEX = new ResourceLocation("turnbattle", "textures/gui/deltarune_text_box.png");
    private final EntityLivingBase enemy;
    private final BattleDodgeArena arena = new BattleDodgeArena();
    private final BattleRenderHelper helper = new BattleRenderHelper();
    private int cmd = 0, subSel = 0, tp = 0, mercy = 0, state = 0, pose = 0, lastDmg = 0;
    private float barProg = 0f, poseTime = 0f, gridOff = 0f;
    private long lastNs = 0L;
    private boolean defending = false, wasCrit = false;
    private String msg;
    private final List<Integer> foodSlots = new ArrayList<>();

    public GuiDeltaruneBattle(EntityLivingBase enemy) {
        this.enemy = enemy;
        this.msg = "* " + enemy.getName() + " blocks your way!";
    }

    public void addTp(int amt) { tp = Math.min(100, tp + amt); }

    @Override
    public void onGuiClosed() { SoulboundMod.inBattle = false; }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        if (enemy == null || enemy.isDead || mc.player == null || mc.player.isDead) { mc.displayGuiScreen(null); return; }
        long now = System.nanoTime();
        float dt = lastNs == 0L ? 0.016f : Math.min(0.05f, (now - lastNs) / 1_000_000_000f);
        lastNs = now;
        gridOff = (gridOff + 18f * dt) % 28f;
        if (poseTime > 0f && (poseTime -= dt) <= 0f) pose = 0;

        int dockTop = height - 88;
        helper.drawStage(mc, width, dockTop, gridOff, tp, mercy, pose, (int) (poseTime * 30f), lastDmg, wasCrit, enemy);
