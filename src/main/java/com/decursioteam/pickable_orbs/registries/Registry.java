package com.decursioteam.pickable_orbs.registries;

import com.decursioteam.pickable_orbs.PickableOrbs;
import com.decursioteam.pickable_orbs.config.CommonConfig;
import com.decursioteam.pickable_orbs.datagen.OrbsData;
import com.decursioteam.pickable_orbs.datagen.utils.FileUtils;
import com.decursioteam.pickable_orbs.entities.OrbEntity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Supplier;

public class Registry {

    public static final Gson GSON = new Gson();

    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, PickableOrbs.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> GET_HEART_SOUND = REGISTRY.register("empty",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(PickableOrbs.MOD_ID, "get_heart_sound")));

    public static void registerOrbTypes(){
        OrbsData.getRegistry().getOrbs().forEach((name, data) -> registerOrb(name));
    }

    public static void registerOrb(String name) {
        Supplier<EntityType<OrbEntity>> registryObject = OrbsRegistry.ORB_TYPES.register(name, () -> EntityType.Builder
                .<OrbEntity>of((type, world) -> new OrbEntity(type, world, name), MobCategory.MISC)
                .sized(0.6f, 0.3f)
                .build(name + "_orb"));

        PickableOrbs.LOGGER.warn("ORBTYPE " + registryObject);
        OrbsRegistry.getOrbs().put(name, registryObject);
    }

    public static void setupDefaultOrbs() {
        FileUtils.setupDefaultFiles("/data/pickable_orbs/default_orbs", createCustomPath("orbs"));

        PickableOrbs.LOGGER.info("Loading Orbs...");
        FileUtils.streamFilesAndParse(createCustomPath("orbs"), Registry::parseOrb, "Could not stream orbs!");

        OrbsData.getRegistry().regenerateCustomOrbsData();
    }

    public static void setupOrbs() {
        PickableOrbs.LOGGER.info("Loading Orbs...");
        FileUtils.streamFilesAndParse(createCustomPath("orbs"), Registry::parseOrb, "Could not stream orbs!");

        OrbsData.getRegistry().regenerateCustomOrbsData();
    }

    private static void parseOrb(Reader reader, String name) {
        JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
        name = Codec.STRING.fieldOf("name").orElse(name).codec().fieldOf("OrbData").codec().parse(JsonOps.INSTANCE, jsonObject).getOrThrow(IllegalArgumentException::new);
        OrbsData.getRegistry().cacheRawOrbsData(name.toLowerCase(Locale.ENGLISH).replace(" ", "_"), jsonObject);
    }

    private static Path createCustomPath(String pathName) {
        Path customPath = Paths.get(FMLPaths.CONFIGDIR.get().toAbsolutePath().toString(), PickableOrbs.MOD_ID, pathName);
        createDirectory(customPath, pathName);
        return customPath;
    }

    private static void createDirectory(Path path, String dirName) {
        try {
            Files.createDirectories(path);
        } catch (FileAlreadyExistsException ignored) {
            //ignored
        } catch (IOException e) {
            PickableOrbs.LOGGER.error("failed to create \"{}\" directory", dirName);
        }
    }
}