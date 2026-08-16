package com.decursioteam.pickable_orbs.config;

import com.decursioteam.pickable_orbs.PickableOrbs;
import com.decursioteam.pickable_orbs.registries.Registry;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.io.File;

public class CommonConfig {

    public static final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    public static final ModConfigSpec config;
    public static ModConfigSpec.BooleanValue generate_defaults;

    static {
        builder.push("General Options");
        generate_defaults = builder
                .comment("Set this value to 'true' whenever you want the default orbs to be regenerated. [true/false]")
                .define("generate_defaults", true);
        builder.pop();
        config = builder.build();
    }

    public static void loadConfigAndCheckDefaults(String path) {
        PickableOrbs.LOGGER.info("Loading config manually for early generation: " + path);
        File configFile = new File(path);

        try (CommentedFileConfig file = CommentedFileConfig.builder(configFile).sync().writingMode(WritingMode.REPLACE)
                .build()) {
            boolean shouldGenerate = true;

            if (configFile.exists()) {
                file.load();
                shouldGenerate = file.getOrElse(java.util.List.of("General Options", "generate_defaults"), true);
            }

            if (shouldGenerate) {
                Registry.setupDefaultOrbs();
                file.set(java.util.List.of("General Options", "generate_defaults"), false);
                file.save();
            }
        }
    }
}