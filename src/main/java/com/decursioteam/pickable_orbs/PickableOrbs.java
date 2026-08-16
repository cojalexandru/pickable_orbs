package com.decursioteam.pickable_orbs;

import com.decursioteam.pickable_orbs.datagen.OrbsData;
import com.decursioteam.pickable_orbs.datagen.OrbsReloadListener;
import com.decursioteam.pickable_orbs.entities.OrbEntity;
import com.decursioteam.pickable_orbs.registries.OrbsRegistry;
import com.decursioteam.pickable_orbs.registries.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
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
        Registry.REGISTRY.register(modEventBus);
        OrbsRegistry.ORB_TYPES.register(modEventBus);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(PickableOrbs.MOD_ID, "orbs"), new OrbsReloadListener());
    }

    @SubscribeEvent
    private static void blockBreakEvent(BreakBlockEvent e) {
        Level world = (Level) e.getLevel();
        OrbsData.getRegistry().getOrbs().forEach((s, orbData) -> {
            List<Identifier> blockSet = orbData.getData().getBlockSet();
            String blockListType = orbData.getData().getBlockListType();
            double blockDropChance = orbData.getData().getBlockDropChance();
            Random random = new Random();

            Identifier blockKey = BuiltInRegistries.BLOCK.getKey(e.getState().getBlock());

            if (blockListType.equalsIgnoreCase("whitelist")) {
                if (blockSet.contains(blockKey)) {
                    if (blockDropChance != 0.0 && blockDropChance >= random.nextDouble() * 100) {
                        world.addFreshEntity(new OrbEntity(OrbsRegistry.ORB.get(), world,
                                e.getPos().getX(), e.getPos().getY(),
                                e.getPos().getZ(), s));
                    }
                }
            } else if (blockListType.equalsIgnoreCase("blacklist")) {
                if (!blockSet.contains(blockKey)) {
                    if (blockDropChance != 0.0 && blockDropChance >= random.nextDouble() * 100) {
                        world.addFreshEntity(new OrbEntity(OrbsRegistry.ORB.get(), world,
                                e.getPos().getX(), e.getPos().getY(),
                                e.getPos().getZ(), s));
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
            OrbsData.getRegistry().getOrbs().forEach((s, orbData) -> {
                List<Identifier> entitySet = orbData.getData().getEntitySet();
                String entityListType = orbData.getData().getEntityListType();
                double entityDropChance = orbData.getData().getEntityDropChance();
                Random random = new Random();

                Identifier entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

                if (entityListType.equalsIgnoreCase("whitelist")) {
                    if (entitySet.contains(entityKey)) {
                        if (entityDropChance != 0.0 && entityDropChance >= random.nextDouble() * 100) {
                            world.addFreshEntity(new OrbEntity(OrbsRegistry.ORB.get(), world,
                                    entity.getX(), entity.getY(),
                                    entity.getZ(), s));
                        }
                    }
                } else if (entityListType.equalsIgnoreCase("blacklist")) {
                    if (!entitySet.contains(entityKey)) {
                        if (entityDropChance != 0.0 && entityDropChance >= random.nextDouble() * 100) {
                            world.addFreshEntity(new OrbEntity(OrbsRegistry.ORB.get(), world,
                                    entity.getX(), entity.getY(),
                                    entity.getZ(), s));
                        }
                    }
                }
            });
        }
    }
}