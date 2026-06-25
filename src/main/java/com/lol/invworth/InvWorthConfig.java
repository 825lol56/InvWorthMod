package com.lol.invworth;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import java.io.File;

public class InvWorthConfig {
    public static Configuration config;

    public static boolean NoBye;
    public static boolean NoCredits;

    public static void init(FMLPreInitializationEvent event) {
        File configDir = event.getModConfigurationDirectory();
        File configFile = new File(configDir, "InvWorth.cfg");
        config = new Configuration(configFile);
        loadConfig();
    }

    private static void loadConfig() {
        try {
            config.load();

            NoCredits = config.getBoolean("NoCredits", "general", false, "Removes the credits message upon joining. Only change to true if you have thoroughly read the credits");
            NoBye = config.getBoolean("NoBye", "general", false, "removes the bye!! message upon leaving");

        } catch (Exception e) {
            System.out.println("Error loading config file!");
            e.printStackTrace();
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
    public static void saveConfig() {

        config.get("general", "NoCredits", false).set(NoCredits);
        config.get("general", "NoBye", false).set(NoBye);
        if (config.hasChanged()) {
            config.save();
        }
    }


}
