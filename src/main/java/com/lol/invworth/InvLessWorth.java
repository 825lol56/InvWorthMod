package com.lol.invworth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InvLessWorth extends CommandBase {

    private static boolean shouldHighlight = false;
    private static boolean openInvNextTick = false;

    @Override
    public String getCommandName() {
        return "leastworths";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/leastworths";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("leastworth", "lws", "lw");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            openInvNextTick = true;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && openInvNextTick) {
            openInvNextTick = false;
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null) {
                shouldHighlight = true;
                mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (shouldHighlight) {
            if (event.gui == null || !(event.gui instanceof GuiInventory)) {
                if (Minecraft.getMinecraft().currentScreen instanceof GuiInventory) {
                    shouldHighlight = false;
                }
            }
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!shouldHighlight || !(event.gui instanceof GuiInventory)) return;

        GuiContainer guiContainer = (GuiContainer) event.gui;
        Minecraft mc = Minecraft.getMinecraft();

        int guiLeft, guiTop;
        try {
            guiLeft = ReflectionHelper.getPrivateValue(GuiContainer.class, guiContainer, "guiLeft", "field_147003_i");
            guiTop = ReflectionHelper.getPrivateValue(GuiContainer.class, guiContainer, "guiTop", "field_147009_r");
        } catch (Exception e) {
            shouldHighlight = false;
            return;
        }

        // Green status text remains right above the inventory box frame
        mc.fontRendererObj.drawString("§aLeastItemWorth mode ON!!", guiLeft + 4, guiTop - 12, 0xFFFFFF);

        List<Slot> validSlots = new ArrayList<>();
        for (Slot slot : guiContainer.inventorySlots.inventorySlots) {
            if (slot != null && slot.inventory == mc.thePlayer.inventory) {
                int index = slot.getSlotIndex();
                if (index >= 0 && index < 36 && slot.getHasStack()) {
                    ItemStack stack = slot.getStack();

                    // Feature 1: Skip if the item has no recorded selling price
                    float[] worth = InvWorthCheck.getWorth(stack);
                    if (worth == null || worth[1] <= 0.0f) {
                        continue;
                    }

                    // Feature 2: Skip if the item's registry name is on the config blacklist
                    if (stack.getItem() != null && InvWorthConfig.ignoredItems != null) {
                        String registryName = Item.itemRegistry.getNameForObject(stack.getItem()).toString();
                        boolean isBlacklisted = false;
                        for (String blacklistedName : InvWorthConfig.ignoredItems) {
                            if (registryName.equalsIgnoreCase(blacklistedName.trim())) {
                                isBlacklisted = true;
                                break;
                            }
                        }
                        if (isBlacklisted) {
                            continue;
                        }
                    }

                    validSlots.add(slot);
                }
            }
        }

        // Sort items by value ascending
        validSlots.sort((s1, s2) -> {
            ItemStack stack1 = s1.getStack();
            ItemStack stack2 = s2.getStack();

            float[] worth1 = InvWorthCheck.getWorth(stack1);
            float[] worth2 = InvWorthCheck.getWorth(stack2);

            float val1 = (worth1 != null) ? (worth1[1] * stack1.stackSize) : 0.0f;
            float val2 = (worth2 != null) ? (worth2[1] * stack2.stackSize) : 0.0f;

            return Float.compare(val1, val2);
        });

        // Highlights limited to a maximum of 5 slots
        int limit = Math.min(5, validSlots.size());
        for (int i = 0; i < limit; i++) {
            Slot slot = validSlots.get(i);
            int x = guiLeft + slot.xDisplayPosition;
            int y = guiTop + slot.yDisplayPosition;

            Gui.drawRect(x, y, x + 16, y + 16, 0x60FF3333);
        }
    }
}