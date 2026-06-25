package com.lol.invworth;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionCheck {

    private boolean credits = false;
    private volatile boolean needsNotification = false;
    private volatile boolean hasNotified = false;
    private volatile String latestVersion = "";

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onPlayerLoggedIn(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        needsNotification = false;
        hasNotified = false;
        latestVersion = "";
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("https://pastebin.com/raw/Hw5BGppe").openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String pastebinVersion = reader.readLine().trim();
                reader.close();

                InvWorth.yapper.info("Compared version {} of the client against version {} of the cloud", InvWorth.VERSION, pastebinVersion);

                if (!InvWorth.VERSION.equals(pastebinVersion)) {
                    latestVersion = pastebinVersion;
                    needsNotification = true;
                    InvWorth.yapper.warn("A new version of InvWorth is available: {}", pastebinVersion);
                }
            } catch (IOException e) {
                InvWorth.yapper.error("Failed to check for updates: {}", e.getMessage());
            }
        }, "InvWorth-VersionCheck").start();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        // Only act once, at the end of a tick, once the background check has actually found an update
        if (event.phase != TickEvent.Phase.END || !needsNotification || hasNotified) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) return;

        hasNotified = true;
        needsNotification = false;

        mc.thePlayer.addChatMessage(new ChatComponentText(InvWorth.prefix + "A new version of " + EnumChatFormatting.RED + "InvWorth" + EnumChatFormatting.GREEN + " is available: " + latestVersion + "\n"  + EnumChatFormatting.GREEN + "Please click ")
                .appendSibling(new ChatComponentText("[here]")
                        .setChatStyle(new ChatStyle()
                                .setColor(EnumChatFormatting.AQUA)
                                .setUnderlined(true)
                                .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/invworth"))))
                .appendText(EnumChatFormatting.GREEN + " or dm @825lol56 for the latest version."));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (InvWorthConfig.NoCredits) return;
        if (event.entity == Minecraft.getMinecraft().thePlayer && event.world.isRemote && !credits) {
            event.entity.addChatMessage(new ChatComponentText(InvWorth.prefix).appendSibling(new ChatComponentText("§7Please hover ").appendSibling(new ChatComponentText("§d[here] ").setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§7All credits for this mod go to §6Inforno, §7the original mod creator.\n" + "§d825lol56 §7only scraped what was left of a now discontinued mod.\n" + "§7Hope you enjoy checking how much your inventory is worth!!!"))))).appendSibling(new ChatComponentText("§7for InvWorth's credits"))));
            credits ^= true;
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onDisconnect(net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        if (!InvWorthConfig.NoBye && (System.currentTimeMillis() / 1000L) % 2 == 0) Minecraft.getMinecraft().thePlayer.sendChatMessage("bye!");
        credits = false;
        needsNotification = false;
        hasNotified = false;
        latestVersion = "";
    }
}