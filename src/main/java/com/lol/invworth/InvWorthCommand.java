package com.lol.invworth;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class InvWorthCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "invworth";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/invworth [smart]";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("iw");
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, net.minecraft.util.BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "smart");
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        boolean smart = args.length > 0 && args[0].equalsIgnoreCase("smart");
        displayWorth(smart);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        // Always allow — this is a client-side only command
        return true;
    }

    public void displayWorth(boolean smart) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.thePlayer == null)
            return;

        float totalWorth = 0.0F;
        ItemStack[] inventory = mc.thePlayer.inventory.mainInventory;

        for (int i = smart ? 9 : 0; i < inventory.length; i++) {
            if (inventory[i] == null)
                continue;
            float[] itemData = InvWorthCheck.getWorth(inventory[i]);
            if (itemData != null)
                totalWorth += itemData[1] * inventory[i].stackSize;
        }

        if (!smart) {
            ItemStack[] armorInventory = mc.thePlayer.inventory.armorInventory;
            for (ItemStack itemStack : armorInventory) {
                if (itemStack == null)
                    continue;
                float[] itemData = InvWorthCheck.getWorth(itemStack);
                if (itemData != null)
                    totalWorth += itemData[1] * itemStack.stackSize;
            }
        }

        DecimalFormat roundDisplay = new DecimalFormat("#.##");
        String message = InvWorth.prefix + "Your " + (smart ? "Smart " : "") + "Inventory is worth $"
                + roundDisplay.format(totalWorth) + " to Scrap Dealer!";

        mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(message));
    }
}