package com.example.examplemod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
    public static final String MOD_ID = "examplemod";
    public static final String MOD_NAME = "ExampleMod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public void onInitialize() {
        ExampleMod.LOGGER.info(MOD_NAME + " successfully loaded!");
    }
}