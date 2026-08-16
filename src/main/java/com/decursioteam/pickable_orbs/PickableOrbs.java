package com.decursioteam.pickable_orbs;

import com.decursioteam.pickable_orbs.config.CommonConfig;
import com.decursioteam.pickable_orbs.config.Readme;
import com.decursioteam.pickable_orbs.datagen.OrbsData;
import com.decursioteam.pickable_orbs.entities.OrbEntity;
import com.decursioteam.pickable_orbs.registries.OrbsRegistry;
import com.decursioteam.pickable_orbs.registries.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

@Mod(PickableOrbs.MOD_ID)
@EventBusSubscriber(modid = PickableOrbs.MOD_ID)
public class PickableOrbs {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "pickable_orbs";

    public PickableOrbs(IEventBus modEventBus, ModContainer modContainer) {
        // Run our manual config load FIRST, before NeoForge's parallel loader starts tracking it
        CommonConfig.loadConfigAndCheckDefaults(FMLPaths.CONFIGDIR.get().resolve("pickable_orbs/common.toml").toString());

        modContainer.registerConfig(ModConfig.Type.CLIENT, Readme.config, "pickable_orbs/readme.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.config, "pickable_orbs/common.toml");

        Registry.REGISTRY.register(modEventBus);
        Registry.setupOrbs();

        OrbsRegistry.ORB_TYPES.register(modEventBus);
        Registry.registerOrbTypes();
    }

    @SubscribeEvent
    private static void blockBreakEvent(BlockEvent.BreakEvent e) {
        Level world = (Level) e.getLevel();
        OrbsRegistry.getOrbs().forEach((s, entityType) -> {
            List<ResourceLocation> blockSet = OrbsData.getOrbData(s).getData().getBlockSet();
            String blockListType = OrbsData.getOrbData(s).getData().getBlockListType();
            double blockDropChance = OrbsData.getOrbData(s).getData().getBlockDropChance();
            Random random = new Random();

            ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(e.getState().getBlock());

            if (blockListType.equalsIgnoreCase("whitelist")) {
                if (blockSet.contains(blockKey)) {
                    if (blockDropChance != 0.0 && blockDropChance >= random.nextDouble() * 100) {
                        world.addFreshEntity(new OrbEntity((EntityType<OrbEntity>) entityType.get(), world,
                                e.getPos().getX(), e.getPos().getY(),
                                e.getPos().getZ(), s, OrbsData.getOrbData(s)));
                    }
                }
            } else if (blockListType.equalsIgnoreCase("blacklist")) {
                if (!blockSet.contains(blockKey)) {
                    if (blockDropChance != 0.0 && blockDropChance >= random.nextDouble() * 100) {
                        world.addFreshEntity(new OrbEntity((EntityType<OrbEntity>) entityType.get(), world,
                                e.getPos().getX(), e.getPos().getY(),
                                e.getPos().getZ(), s, OrbsData.getOrbData(s)));
                    }
                }
            }
        });
    }

    @SubscribeEvent
    private static void entityDropEvent(LivingDeathEvent e) {
        LivingEntity entity = e.getEntity();
        Level world = entity.level();
        Entity sourceEntity = e.getSource().getEntity();

        if (sourceEntity instanceof Player) {
            OrbsRegistry.getOrbs().forEach((s, entityType) -> {
                List<ResourceLocation> entitySet = OrbsData.getOrbData(s).getData().getEntitySet();
                String entityListType = OrbsData.getOrbData(s).getData().getEntityListType();
                double entityDropChance = OrbsData.getOrbData(s).getData().getEntityDropChance();
                Random random = new Random();

                ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

                if (entityListType.equalsIgnoreCase("whitelist")) {
                    if (entitySet.contains(entityKey)) {
                        if (entityDropChance != 0.0 && entityDropChance >= random.nextDouble() * 100) {
                            world.addFreshEntity(new OrbEntity((EntityType<OrbEntity>) entityType.get(), world,
                                    entity.getX(), entity.getY(),
                                    entity.getZ(), s, OrbsData.getOrbData(s)));
                        }
                    }
                } else if (entityListType.equalsIgnoreCase("blacklist")) {
                    if (!entitySet.contains(entityKey)) {
                        if (entityDropChance != 0.0 && entityDropChance >= random.nextDouble() * 100) {
                            world.addFreshEntity(new OrbEntity((EntityType<OrbEntity>) entityType.get(), world,
                                    entity.getX(), entity.getY(),
                                    entity.getZ(), s, OrbsData.getOrbData(s)));
                        }
                    }
                }
            });
        }
    }
}