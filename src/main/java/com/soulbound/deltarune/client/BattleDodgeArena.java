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
    private float elapsed = 0f, spawnAcc = 0f, invuln = 0f;
    private long lastNs = 0L;
    private final List<Bullet> bullets = new ArrayList<>();
    private final Random rand = new Random();

    public void reset(int w, int h) {
        elapsed = 0f; spawnAcc = 0f; invuln = 0f;
        lastNs = System.nanoTime();
        bullets.clear();
        soulX = w / 2f - 5;
        soulY = h / 2f - 18;
    }

    public boolean tickAndDraw(Minecraft mc, int w, int h, EntityLivingBase enemy, boolean defending, GuiDeltaruneBattle parent) {
        long now = System.nanoTime();
        float dt = lastNs == 0L ? 0.016f : Math.min(0.05f, (now - lastNs) / 1_000_000_000f);
        lastNs = now;
        elapsed += dt;
        spawnAcc += dt;
        if (invuln > 0f) invuln = Math.max(0f, invuln - dt);

        int bw = 120, bh = 94, bx1 = w / 2 - bw / 2, by1 = h / 2 - bh / 2 - 20, bx2 = bx1 + bw, by2 = by1 + bh;
        drawRect(bx1 - 2, by1 - 2, bx2 + 2, by2 + 2, 0xFF00EE00);
        drawRect(bx1, by1, bx2, by2, 0xFF000000);

        float spd = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 45f : 80f) * dt;
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
        if ((int) (invuln * 16) % 2 == 0) {
            mc.getTextureManager().bindTexture(SOUL);
            drawModalRectWithCustomSizedTexture((int) soulX, (int) soulY, 0, 0, 10, 10, 10, 10);
        }

        if (spawnAcc >= 0.45f) {
            spawnAcc = 0f;
            bullets.add(new Bullet(bx1 + 10 + rand.nextInt(bw - 20), by1 + 2, (rand.nextFloat() - 0.5f) * 32f, 54f));
        }
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.x += b.vx * dt; b.y += b.vy * dt;
            mc.getTextureManager().bindTexture(BULLET);
            drawModalRectWithCustomSizedTexture((int) b.x - 4, (int) b.y - 4, 0, 0, 8, 8, 8, 8);
            float dist = (float) Math.hypot(b.x - (soulX + 5), b.y - (soulY + 5));
            if (dist < 16f && !b.grazed) { b.grazed = true; parent.addTp(5); }
            if (dist < 6f && invuln <= 0f) {
                invuln = 1.0f;
                mc.player.playSound(SoulboundMod.SND_DAMAGE, 1f, 1f);
                float dmg = Math.max(1f, (defending ? 1.5f : 3f) - SoulboundMod.defense * 0.3f);
                mc.player.attackEntityFrom(DamageSource.causeMobDamage(enemy), dmg);
            }
            if (b.y > by2) it.remove();
        }
        return elapsed >= 6.5f;
    }
}

