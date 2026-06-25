package com.lol.invworth;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InvWorthCfgCmd extends CommandBase {

    @Override
    public String getCommandName() {
        return "invworth825";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/invworth825 <help|reload|blacklistadd>";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("iw8", "iw825", "invworth8");
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, net.minecraft.util.BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "reload", "help", "blacklistadd");
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        // Catch no arguments or 'help' to print the menu safely
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMenu(mc);
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            InvWorthCheck.loadShopData();
            mc.thePlayer.addChatMessage(new ChatComponentText(InvWorth.prefix + EnumChatFormatting.GREEN + "Shop worth configurations reloaded successfully."));
            return;
        }

        // Blacklist Insertion Handler
        if (args[0].equalsIgnoreCase("blacklistadd")) {
            String itemID = "";

            if (args.length > 1) {
                // Manual input format: /invworth825 blacklistadd minecraft:dirt
                itemID = args[1].toLowerCase();
            } else {
                // Hand extraction format: grabs the exact ID of whatever item you're holding
                ItemStack heldItem = mc.thePlayer.getHeldItem();
                if (heldItem != null && heldItem.getItem() != null) {
                    itemID = Item.itemRegistry.getNameForObject(heldItem.getItem()).toString();
                } else {
                    mc.thePlayer.addChatMessage(new ChatComponentText(InvWorth.prefix + EnumChatFormatting.RED + "Please hold an item or specify an ID! Example: /invworth825 blacklistadd minecraft:dirt"));
                    return;
                }
            }

            // Convert array to temporary list to handle fluid additions dynamically
            List<String> currentList = new ArrayList<>();
            if (InvWorthConfig.ignoredItems != null) {
                currentList.addAll(Arrays.asList(InvWorthConfig.ignoredItems));
            }

            if (currentList.contains(itemID)) {
                mc.thePlayer.addChatMessage(new ChatComponentText(InvWorth.prefix + EnumChatFormatting.YELLOW + itemID + " is already on the blacklist!"));
            } else {
                currentList.add(itemID);
                InvWorthConfig.ignoredItems = currentList.toArray(new String[0]);

                // Triggers saving directly into your InvWorth.cfg file
                InvWorthConfig.saveConfig();
                mc.thePlayer.addChatMessage(new ChatComponentText(InvWorth.prefix + EnumChatFormatting.GREEN + "Successfully blacklisted " + EnumChatFormatting.AQUA + itemID + EnumChatFormatting.GREEN + " from low-value scans."));
            }
            return;
        }

        mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown parameter. Run /invworth825 help for instructions."));
    }

    public void sendHelpMenu(Minecraft mc) {
        mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GREEN + "======== " + EnumChatFormatting.GOLD + "InvWorth Command Menu" + EnumChatFormatting.DARK_GREEN + " ========"));
        mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "/invworth825 help " + EnumChatFormatting.GRAY + "- Displays this command usage blueprint."));
        mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "/invworth825 reload " + EnumChatFormatting.GRAY + "- Refreshes shop and item pricing configurations."));
        mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "/invworth825 blacklistadd [id] " + EnumChatFormatting.GRAY + "- Appends current hand item or manually specified ID to filters."));
        mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "/leastworths " + EnumChatFormatting.GRAY + "- Opens inventory rendering red overlays on the lowest value items."));
        mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "/invworth [smart]" + EnumChatFormatting.GRAY + "- Tells you how much your inventory is worth."));
        mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GREEN + "========================================"));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}