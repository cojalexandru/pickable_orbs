package com.decursioteam.pickable_orbs.config;

import com.decursioteam.pickable_orbs.PickableOrbs;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.io.File;

public class CommonConfig {

    public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec config;
    public static ForgeConfigSpec.BooleanValue generate_defaults;

    static {
        builder.push("General Options");
        generate_defaults = builder.comment("Set this value to 'true' whenever you want the default orbs to be regenerated. [true/false]")
                .define("generate_defaults", true);
        builder.pop();
        config = builder.build();
    }

    public static void loadConfig(ForgeConfigSpec config, String path) {
        PickableOrbs.LOGGER.info("Loading config: " + path);
        final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
        PickableOrbs.LOGGER.info("Built config: " + path);
        file.load();
        PickableOrbs.LOGGER.info("Loaded config: " + path);
        config.setConfig(file);
    }
}
