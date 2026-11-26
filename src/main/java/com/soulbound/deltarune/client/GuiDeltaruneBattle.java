package com.soulbound.deltarune.client;

import com.soulbound.deltarune.SoulboundMod;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.*;

public class GuiDeltaruneBattle extends GuiScreen {
    private static final ResourceLocation BOX_TEX = new ResourceLocation("turnbattle", "textures/gui/deltarune_text_box.png");
    private static final ResourceLocation COL_TEX = new ResourceLocation("turnbattle", "textures/gui/playercolumn.png");
    private static final ResourceLocation TP_BG = new ResourceLocation("turnbattle", "textures/gui/tp_bar.png");
    private static final ResourceLocation TP_FG = new ResourceLocation("turnbattle", "textures/gui/tp_bar_prog.png");

    private final EntityLivingBase enemy;
    private final BattleDodgeArena arena = new BattleDodgeArena();
    private int cmd = 0, subSel = 0, tp = 0, mercy = 0, state = 0, anim = 0, pose = 0, poseTicks = 0;
    private float barPos = 0f;
    private boolean defending = false;
    private String msg;
    private final List<Integer> foodSlots = new ArrayList<>();

    public GuiDeltaruneBattle(EntityLivingBase enemy) {
        this.enemy = enemy;
        this.msg = "* " + enemy.getName() + " blocks your way!";
    }

    public void addTp(int amt) { tp = Math.min(100, tp + amt); }

    private ResourceLocation getBtnTex(int i, boolean sel) {
        if (i == 0) return new ResourceLocation("turnbattle", sel ? "textures/gui/bashsel.png" : "textures/gui/bash.png");
        if (i == 1) return new ResourceLocation("turnbattle", sel ? "textures/gui/skillsel.png" : "textures/gui/skills.png");
        if (i == 2) return new ResourceLocation("turnbattle", sel ? "textures/gui/goodssel.png" : "textures/gui/goods.png");
        if (i == 3) return new ResourceLocation("turnbattle", sel ? "textures/gui/mercysel.png" : "textures/gui/mercy.png");
        return new ResourceLocation("turnbattle", sel ? "textures/gui/defendsel.png" : "textures/gui/defend.png");
    }

    @Override
    public void onGuiClosed() { SoulboundMod.inBattle = false; }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        if (enemy == null || enemy.isDead || mc.player == null || mc.player.isDead) { mc.displayGuiScreen(null); return; }
        anim++;
        if (poseTicks > 0 && --poseTicks == 0) pose = 0;

        drawRect(0, 0, width, height, 0xFF000000);
        int off = (anim / 3) % 28;
        for (int x = -28 + off; x < width; x += 28) drawRect(x, 0, x + 1, height, 0xFF2A123D);
        for (int y = -28 + off; y < height; y += 28) drawRect(0, y, width, y + 1, 0xFF2A123D);

        int dockTop = height - 92, tpH = dockTop - 44;
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(TP_BG);
        drawModalRectWithCustomSizedTexture(14, 28, 0, 0, 16, tpH, 16, tpH);
        int fill = (int) (tpH * (tp / 100f));
        if (fill > 0) {
            mc.getTextureManager().bindTexture(TP_FG);
            drawModalRectWithCustomSizedTexture(14, 28 + tpH - fill, 0, 0, 16, fill, 16, tpH);
        }
        drawString(fontRenderer, "TP " + tp + "%", 34, 38, 0xFFFFAA00);

        int stageY = dockTop - 22;
        GuiInventory.drawEntityOnScreen(width / 4 + 12 + (pose == 1 ? 26 : 0), stageY - (pose == 2 ? 6 : 0), 42, -40f, 0f, mc.player);
        int ex = (width * 3) / 4 - 12 + (pose == 1 ? (poseTicks % 4 - 2) * 3 : 0);
        GuiInventory.drawEntityOnScreen(ex, stageY, 42, 40f, 0f, enemy);

        drawString(fontRenderer, enemy.getName(), ex - 35, stageY - 104, 0xFFFFFFFF);
        drawRect(ex - 35, stageY - 92, ex + 35, stageY - 86, 0xFF551111);
        drawRect(ex - 35, stageY - 92, ex - 35 + (int) (70 * Math.max(0f, enemy.getHealth() / enemy.getMaxHealth())), stageY - 86, 0xFF33FF33);
        drawRect(ex - 35, stageY - 84, ex - 35 + (int) (70 * (mercy / 100f)), stageY - 79, 0xFFFFFF00);

        if (state == 1) {
            barPos += 2.2f;
            int bx = width / 2 - 75, by = dockTop - 28;
            drawRect(bx, by, bx + 150, by + 20, 0xFF001122);
            drawRect(bx + 12, by + 2, bx + 22, by + 18, 0xFF00FFFF);
            int curX = bx + 145 - (int) barPos;
            drawRect(curX, by, curX + 4, by + 20, Math.abs(curX - (bx + 15)) < 8 ? 0xFFFFFF00 : 0xFFFFFFFF);
            if (barPos >= 145) performSlash(0.3f);
        }
        if (state == 3 && arena.tickAndDraw(mc, width, height, enemy, defending, this)) {
            defending = false; state = 0; msg = "* " + enemy.getName() + " awaits your next move.";
        }

        int colX = width / 2 - 110, colY = dockTop - 32;
        mc.getTextureManager().bindTexture(COL_TEX);
        drawModalRectWithCustomSizedTexture(colX, colY, 0, 0, 220, 34, 220, 34);
        fontRenderer.drawString(mc.player.getName().toUpperCase(), colX + 26, colY + 5, 0xFFFFFFFF);
        fontRenderer.drawString((int) Math.ceil(mc.player.getHealth()) + "/" + (int) Math.ceil(mc.player.getMaxHealth()), colX + 155, colY + 5, 0xFFFFFFFF);
        for (int i = 0; i < 5; i++) {
            mc.getTextureManager().bindTexture(getBtnTex(i, state == 0 && cmd == i));
            drawModalRectWithCustomSizedTexture(colX + 8 + i * 42, colY + 17, 0, 0, 38, 15, 38, 15);
        }

        mc.getTextureManager().bindTexture(BOX_TEX);
        drawModalRectWithCustomSizedTexture(18, dockTop, 0, 0, width - 36, height - dockTop - 6, width - 36, height - dockTop - 6);
        if (state == 2) {
            for (int i = 0; i < Math.min(4, foodSlots.size()); i++) {
                ItemStack st = mc.player.inventory.getStackInSlot(foodSlots.get(i));
                int heal = ((ItemFood) st.getItem()).getHealAmount(st);
                drawString(fontRenderer, (subSel == i ? "> " : "  ") + st.getDisplayName() + " (+" + heal + " HP)", 36, dockTop + 12 + i * 14, subSel == i ? 0xFFFFFF55 : 0xFFFFFFFF);
            }
        } else {
            drawString(fontRenderer, msg, 34, dockTop + 14, 0xFFFFFFFF);
        }
    }

    private void performSlash(float mult) {
        mc.player.swingArm(EnumHand.MAIN_HAND);
        pose = 1; poseTicks = 16;
        boolean crit = mult >= 1.4f;
        mc.player.playSound(crit ? SoulboundMod.SND_CRIT : SoulboundMod.SND_SLASH, 1f, 1f);
        float dmg = (4f + SoulboundMod.offense * 2f) * mult;
        enemy.attackEntityFrom(DamageSource.causePlayerDamage(mc.player), dmg);
        addTp(crit ? 15 : 8);
        if (enemy.isDead || enemy.getHealth() <= 0f) {
            mc.player.playSound(SoulboundMod.SND_DEFEAT, 1f, 1f);
            SoulboundMod.exp += 35; SoulboundMod.gold += 15;
            if (SoulboundMod.exp >= 100) { SoulboundMod.lv++; SoulboundMod.exp -= 100; SoulboundMod.sp += 2; }
            mc.displayGuiScreen(null);
        } else { msg = (crit ? "* CRITICAL! " : "* Slashed ") + enemy.getName() + " for " + (int) dmg + " DMG!"; state = 4; }
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(null); return; }
        if (state == 0) {
            if (key == Keyboard.KEY_LEFT || key == Keyboard.KEY_A) cmd = (cmd + 4) % 5;
            else if (key == Keyboard.KEY_RIGHT || key == Keyboard.KEY_D) cmd = (cmd + 1) % 5;
            else if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_SPACE) {
                if (cmd == 0) { state = 1; barPos = 0f; }
                else if (cmd == 1) { pose = 2; poseTicks = 15; mercy = Math.min(100, mercy + 50); msg = "* You reasoned with " + enemy.getName() + "! Mercy +50%."; state = 4; }
                else if (cmd == 2) {
                    foodSlots.clear();
                    for (int i = 0; i < mc.player.inventory.mainInventory.size(); i++)
                        if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemFood) foodSlots.add(i);
                    if (foodSlots.isEmpty()) { msg = "* You have no food!"; state = 4; } else { subSel = 0; state = 2; }
                } else if (cmd == 3) {
                    if (mercy >= 100) { mc.player.playSound(SoulboundMod.SND_DEFEAT, 1f, 1f); enemy.setDead(); mc.displayGuiScreen(null); }
                    else { msg = "* " + enemy.getName() + "'s name isn't yellow yet!"; state = 4; }
                } else if (cmd == 4) { defending = true; addTp(16); msg = "* You defended! (+16% TP, half damage)"; state = 4; }
            }
        } else if (state == 1 && (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_SPACE)) {
            int dist = Math.abs((width / 2 + 70 - (int) barPos) - (width / 2 - 60));
            performSlash(dist < 8 ? 1.5f : (dist < 25 ? 1.0f : 0.6f));
        } else if (state == 2) {
            if (key == Keyboard.KEY_UP || key == Keyboard.KEY_W) subSel = Math.max(0, subSel - 1);
            else if (key == Keyboard.KEY_DOWN || key == Keyboard.KEY_S) subSel = Math.min(Math.min(3, foodSlots.size() - 1), subSel + 1);
            else if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_SPACE) {
                ItemStack st = mc.player.inventory.getStackInSlot(foodSlots.get(subSel));
                int heal = ((ItemFood) st.getItem()).getHealAmount(st);
                mc.player.heal(heal); st.shrink(1);
                msg = "* Ate " + st.getDisplayName() + "! +" + heal + " HP."; state = 4;
            }
        } else if (state == 4 && (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_SPACE)) {
            state = 3; arena.reset(width, height); msg = "* Dodge! (Hold SHIFT to slow down)";
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return true; }
}
