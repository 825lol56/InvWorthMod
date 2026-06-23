package com.lol.invworth;

import com.lol.invworth.InvWorth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionCheck {

    private boolean credits = false;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onPlayerLoggedIn(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;
        mc.addScheduledTask(() -> {
            EntityPlayerSP player = mc.thePlayer;
            if (player == null) return;
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
                        mc.addScheduledTask(() -> {
                            EntityPlayerSP p = mc.thePlayer;
                            if (p != null) {
                                p.addChatMessage(new ChatComponentText(InvWorth.prefix + "A new version of " + EnumChatFormatting.RED + "InvWorth" + EnumChatFormatting.GREEN + " is available: " + pastebinVersion + ". Please dm @825lol56 or check Modrinth for the latest version."));
                            }
                        });
                        InvWorth.yapper.warn("A new version of InvWorth is available: {}", pastebinVersion);
                    }

                } catch (IOException e) {
                    InvWorth.yapper.error("Failed to check for updates: {}", e.getMessage());
                }
            }).start();
        });
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity == Minecraft.getMinecraft().thePlayer && event.world.isRemote && !credits) {
            event.entity.addChatMessage(new ChatComponentText(InvWorth.prefix).appendSibling(new ChatComponentText("§7Please hover ").appendSibling(new ChatComponentText("§d[here] ").setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§7All credits for this mod go to §6Inforno, §7the original mod creator.\n" + "§d825lol56 §7only scraped what was left of a now discontinued mod.\n" + "§7Hope you enjoy checking how much your inventory is worth!!!"))))).appendSibling(new ChatComponentText("§7for InvWorth's credits"))));
            credits ^= true;
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onDisconnect(net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Minecraft.getMinecraft().thePlayer.sendChatMessage("bye!");
        credits = false;
    }
}
