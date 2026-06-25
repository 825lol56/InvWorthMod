package com.lol.invworth;


import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = InvWorth.MODID, name = InvWorth.NAME, version = InvWorth.VERSION)
public class InvWorth {
    public static final String MODID = "invworth";
    public static final String NAME = "InvWorth";
    public static final String VERSION = "1.2";
    public static final Logger yapper = LogManager.getLogger(MODID);
    public static InvWorth instance;
    public static String prefix = EnumChatFormatting.GREEN + "[" + EnumChatFormatting.RED + "825bot56" + EnumChatFormatting.GREEN + "] ";
    public static Minecraft mc = Minecraft.getMinecraft();

    /** ****** <<< USEFUL LINKS! >>> ************
     Version link: https://pastebin.com/Hw5BGppe
     Prices link: https://pastebin.com/im3M4zfB
     ******************************************/

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        InvWorthConfig.init(event);
        yapper.info("{} is loading!", NAME);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        yapper.info("{} is initializing!", NAME);
        new handler();
        ClientCommandHandler.instance.registerCommand(new InvWorthCommand());
        ClientCommandHandler.instance.registerCommand(new InvLessWorth());
        MinecraftForge.EVENT_BUS.register(new VersionCheck());
        MinecraftForge.EVENT_BUS.register(new InvWorthCheck());
        MinecraftForge.EVENT_BUS.register(new InvLessWorth());
        InvWorthCheck.loadShopData();



    }
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        yapper.info("{} has finished loading!", NAME);
    }

}
