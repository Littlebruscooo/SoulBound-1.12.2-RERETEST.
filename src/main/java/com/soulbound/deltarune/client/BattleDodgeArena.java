package com.soulbound.deltarune.client;

import com.soulbound.deltarune.SoulboundMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import java.util.*;

public class BattleDodgeArena extends Gui {
    private static final ResourceLocation SOUL = new ResourceLocation("turnbattle", "textures/gui/spr_statsoul.png");
    private static final ResourceLocation GRAZE = new ResourceLocation("turnbattle", "textures/gui/graze_ring.png");
    private static final ResourceLocation BULLET = new ResourceLocation("turnbattle", "textures/gui/bullet.png");

    private static class Bullet {
        float x, y, vx, vy;
        boolean grazed;
        Bullet(float x, float y, float vx, float vy) { this.x = x; this.y = y; this.vx = vx; this.vy = vy; }
    }

    public float soulX, soulY;
    public int ticks = 0, invuln = 0;
    private final List<Bullet> bullets = new ArrayList<>();
    private final Random rand = new Random();

    public void reset(int w, int h) {
        ticks = 0;
        bullets.clear();
        soulX = w / 2f - 5;
        soulY = h / 2f - 18;
    }

    public boolean tickAndDraw(Minecraft mc, int w, int h, EntityLivingBase enemy, boolean defending, GuiDeltaruneBattle parent) {
        int bw = 116, bh = 92, bx1 = w / 2 - bw / 2, by1 = h / 2 - bh / 2 - 18, bx2 = bx1 + bw, by2 = by1 + bh;
        drawRect(bx1 - 2, by1 - 2, bx2 + 2, by2 + 2, 0xFF00EE00);
        drawRect(bx1, by1, bx2, by2, 0xFF000000);

        float spd = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 0.85f : 1.45f;
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A)) soulX -= spd;
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D)) soulX += spd;
        if (Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W)) soulY -= spd;
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S)) soulY += spd;
        soulX = Math.max(bx1 + 2, Math.min(bx2 - 12, soulX));
        soulY = Math.max(by1 + 2, Math.min(by2 - 12, soulY));

        GlStateManager.color(1f, 1f, 1f, 0.5f);
        mc.getTextureManager().bindTexture(GRAZE);
        drawModalRectWithCustomSizedTexture((int) soulX - 6, (int) soulY - 6, 0, 0, 22, 22, 22, 22);
        GlStateManager.color(1f, 1f, 1f, 1f);
        if (invuln > 0) invuln--;
        if (invuln % 4 < 2) {
            mc.getTextureManager().bindTexture(SOUL);
            drawModalRectWithCustomSizedTexture((int) soulX, (int) soulY, 0, 0, 10, 10, 10, 10);
        }

        if (++ticks % 18 == 0) {
            bullets.add(new Bullet(bx1 + 10 + rand.nextInt(bw - 20), by1 + 2, (rand.nextFloat() - 0.5f) * 0.8f, 1.15f));
        }
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.x += b.vx; b.y += b.vy;
            mc.getTextureManager().bindTexture(BULLET);
            drawModalRectWithCustomSizedTexture((int) b.x - 4, (int) b.y - 4, 0, 0, 8, 8, 8, 8);
            float dist = (float) Math.hypot(b.x - (soulX + 5), b.y - (soulY + 5));
            if (dist < 16f && !b.grazed) { b.grazed = true; parent.addTp(5); }
            if (dist < 6f && invuln == 0) {
                invuln = 30;
                mc.player.playSound(SoulboundMod.SND_DAMAGE, 1f, 1f);
                float dmg = Math.max(1f, (defending ? 1.5f : 3f) - SoulboundMod.defense * 0.3f);
                mc.player.attackEntityFrom(DamageSource.causeMobDamage(enemy), dmg);
            }
            if (b.y > by2) it.remove();
        }
        return ticks >= 170;
    }
}
