package com.lol.invworth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
            Field guiLeftField = GuiContainer.class.getDeclaredField("guiLeft");
            Field guiTopField = GuiContainer.class.getDeclaredField("guiTop");
            guiLeftField.setAccessible(true);
            guiTopField.setAccessible(true);
            guiLeft = (int) guiLeftField.get(guiContainer);
            guiTop = (int) guiTopField.get(guiContainer);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return;
        }

        // Renders the green status text right above the inventory layout bounds
        mc.fontRendererObj.drawString("§aLeastItemWorth mode ON!!", guiLeft + 4, guiTop - 12, 0xFFFFFF);

        List<Slot> validSlots = new ArrayList<>();
        for (Slot slot : guiContainer.inventorySlots.inventorySlots) {
            if (slot != null && slot.inventory == mc.thePlayer.inventory) {
                int index = slot.getSlotIndex();
                if (index >= 0 && index < 36 && slot.getHasStack()) {
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

        // Highlights downsized to 4 slots maximum
        int limit = Math.min(6, validSlots.size());
        for (int i = 0; i < limit; i++) {
            Slot slot = validSlots.get(i);
            int x = guiLeft + slot.xDisplayPosition;
            int y = guiTop + slot.yDisplayPosition;

            Gui.drawRect(x, y, x + 16, y + 16, 0x60FF3333);
        }
    }
}