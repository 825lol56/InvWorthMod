package com.lol.invworth;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;

import static com.lol.invworth.InvWorth.yapper;

public class InvWorthCheck {

    public static HashMap<Integer, float[]> shopData = new HashMap<>();
    private static final DecimalFormat DF = new DecimalFormat("#.##");
    private static final DecimalFormat DFP = new DecimalFormat("#.##%");

    public static void loadShopData() {
        String splitBy = ",";

        try {
            URL url = new URL("https://pastebin.com/raw/im3M4zfB");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;

            while ((line = br.readLine()) != null) { if (line.trim().isEmpty()) continue;
                String[] data = line.split(splitBy);
                // safety check
                if (data.length < 4) continue;
                try { float[] val = {Float.parseFloat(data[1]), Float.parseFloat(data[2]), Float.parseFloat(data[3])};
                    shopData.put(data[0].hashCode(), val);
                } catch (NumberFormatException e) {
                    yapper.warn("Invalid number in line: " + line);
                }
            }

            br.close();
            conn.disconnect();
            yapper.info("Loaded " + shopData.size() + " shop entries.");

        } catch (IOException e) {
            yapper.warn("Failed to Load Shop Data:", e);
        }
    }

    public static float[] getWorth(ItemStack stack) {
        if (stack == null)
            return null;
        float[] data;
        if ((data = shopData.get(Integer.valueOf((stack.getItem().getRegistryName() + ":" + stack.getItem().getDamage(stack)).hashCode()))) == null && (
                data = shopData.get(Integer.valueOf((stack.getItem().getRegistryName() + ":0").hashCode()))) == null)
            return null;
        float[] copy = (float[])data.clone();
        if (stack.getItem().isDamageable())
            copy[1] = copy[1] * (float)(1.0D - stack.getItem().getDurabilityForDisplay(stack));
        return copy;
    }

    @SubscribeEvent
    public void onToolTip(ItemTooltipEvent event) {
        ItemStack stack = event.itemStack;
        if (stack == null)
            return;
        boolean newLine = false;
        float[] data = getWorth(stack);
        if (data != null) {
            int count = 1;
            String countStr = "";
            if (GuiScreen.isShiftKeyDown()) { count = stack.stackSize;countStr = "x" + count; }
            if (newLine) event.toolTip.add("");
            if (data[0] > 0.0F)
                event.toolTip.add("§6Price " + countStr + ": " + DF.format((data[0] * count)));
            event.toolTip.add("§6Price " + countStr + ": " + DF.format((data[1] * count)));
        }
    }

    public static int MoneyCountingJew(IInventory chest) {
        float totalWorth = 0.0F;
        for (int i = 0; i < chest.getSizeInventory(); i++) {
            ItemStack stack = chest.getStackInSlot(i);
            float[] worth;
            if ((worth = getWorth(stack)) != null)
                totalWorth += worth[1] * stack.stackSize;
        }
        return (int) totalWorth;
    }

    @SubscribeEvent
    public void onGUIDrawnEvent(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (event != null && event.gui instanceof net.minecraft.client.gui.inventory.GuiChest) {
            GuiContainer chestGui = (GuiContainer) event.gui;
            IInventory chest = ((ContainerChest) chestGui.inventorySlots).getLowerChestInventory();

            int totalWorth = MoneyCountingJew(chest);

            int guiLeft, guiTop, xSize;
            try {
                Field guiLeftField = GuiContainer.class.getDeclaredField("guiLeft");
                Field guiTopField = GuiContainer.class.getDeclaredField("guiTop");
                Field xSizeField = GuiContainer.class.getDeclaredField("xSize");
                guiLeftField.setAccessible(true);
                guiTopField.setAccessible(true);
                xSizeField.setAccessible(true);
                guiLeft = (int) guiLeftField.get(chestGui);
                guiTop = (int) guiTopField.get(chestGui);
                xSize = (int) xSizeField.get(chestGui);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return;
            }

            GlStateManager.disableLighting();
            GlStateManager.pushMatrix();
            GlStateManager.translate(guiLeft + xSize / 2.0F, guiTop, 1.0F);
            InvWorth.mc.fontRendererObj.drawString("Worth: $" + DF.format(totalWorth), 4.0F, 6.0F, 4210752, false);
            GlStateManager.popMatrix();
        }
    }
}

