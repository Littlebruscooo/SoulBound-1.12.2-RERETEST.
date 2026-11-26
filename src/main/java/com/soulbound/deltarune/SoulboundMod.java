package com.soulbound.deltarune;

import com.soulbound.deltarune.client.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@Mod(modid = "soulbound", name = "Soulbound Deltarune", version = "1.0.0")
public class SoulboundMod {
    public static boolean inBattle = false;
    public static int lv = 1, exp = 0, sp = 3, gold = 0;
    public static int offense = 1, defense = 1, magic = 1, vitality = 1, iq = 0, luck = 0;
    public static KeyBinding keyMenu;

    public static final SoundEvent SND_START = regSound("battle_start");
    public static final SoundEvent SND_SLASH = regSound("slash");
    public static final SoundEvent SND_CRIT = regSound("snd_criticalswing");
    public static final SoundEvent SND_DAMAGE = regSound("snd_damage");
    public static final SoundEvent SND_DEFEAT = regSound("defeatsfx");

    private static SoundEvent regSound(String name) {
        ResourceLocation rl = new ResourceLocation("turnbattle", name);
        return new SoundEvent(rl).setRegistryName(rl);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        if (event.getSide().isClient()) {
            keyMenu = new KeyBinding("Soulbound Menu", Keyboard.KEY_C, "Soulbound");
            ClientRegistry.registerKeyBinding(keyMenu);
            MinecraftForge.EVENT_BUS.register(new HudSoulbound());
        }
    }

    @SubscribeEvent
    public void onRegisterSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(SND_START, SND_SLASH, SND_CRIT, SND_DAMAGE, SND_DEFEAT);
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (inBattle) return;
        if (event.getEntityLiving() instanceof EntityPlayer) {
            event.setCanceled(true);
            if (event.getSource().getTrueSource() instanceof EntityLivingBase && event.getEntityLiving().world.isRemote) {
                openBattle((EntityLivingBase) event.getSource().getTrueSource());
            }
        } else if (event.getSource().getTrueSource() instanceof EntityPlayer) {
            event.setCanceled(true);
            if (event.getEntityLiving().world.isRemote) {
                openBattle(event.getEntityLiving());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void openBattle(EntityLivingBase enemy) {
        if (Minecraft.getMinecraft().currentScreen == null && enemy != null && !enemy.isDead) {
            inBattle = true;
            Minecraft.getMinecraft().player.playSound(SND_START, 1.0f, 1.0f);
            Minecraft.getMinecraft().displayGuiScreen(new GuiDeltaruneBattle(enemy));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiMainMenu && !(event.getGui() instanceof GuiSoulboundMainMenu)) {
            event.setGui(new GuiSoulboundMainMenu());
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (keyMenu != null && keyMenu.isPressed() && Minecraft.getMinecraft().currentScreen == null) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiSoulboundMenu());
        }
    }
}
