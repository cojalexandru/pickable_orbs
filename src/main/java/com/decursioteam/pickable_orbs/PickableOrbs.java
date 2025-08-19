package com.decursioteam.pickable_orbs;

import com.decursioteam.pickable_orbs.client.HPClient;
import com.decursioteam.pickable_orbs.config.CommonConfig;
import com.decursioteam.pickable_orbs.config.Readme;
import com.decursioteam.pickable_orbs.datagen.OrbsData;
import com.decursioteam.pickable_orbs.entities.OrbEntity;
import com.decursioteam.pickable_orbs.registries.OrbsRegistry;
import com.decursioteam.pickable_orbs.registries.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

@Mod("pickable_orbs")
@Mod.EventBusSubscriber(modid = PickableOrbs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PickableOrbs {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "pickable_orbs";

    public PickableOrbs() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.config, "pickable_orbs/common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Readme.config, "pickable_orbs/readme.toml");
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        Registry.REGISTRY.register(bus);
        Registry.setupOrbs();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> HPClient::register);
        }

        MinecraftForge.EVENT_BUS.addListener(this::entityDropEvent);
        MinecraftForge.EVENT_BUS.addListener(this::blockBreakEvent);

        CommonConfig.loadConfig(CommonConfig.config, FMLPaths.CONFIGDIR.get().resolve("pickable_orbs/common.toml").toString());

        if (CommonConfig.generate_defaults.get()) {
            Registry.setupDefaultOrbs();
            CommonConfig.generate_defaults.set(false);
            CommonConfig.generate_defaults.save();
        }
        OrbsRegistry.ORB_TYPES.register(bus);
        Registry.registerOrbTypes();
    }

    private void blockBreakEvent(BlockEvent.BreakEvent e){
        Level world = (Level) e.getLevel();
        OrbsRegistry.getOrbs().forEach((s, entityType) ->{
            List<ResourceLocation> blockSet = OrbsData.getOrbData(s).getData().getBlockSet();
            String blockListType = OrbsData.getOrbData(s).getData().getBlockListType();
            double blockDropChance = OrbsData.getOrbData(s).getData().getBlockDropChance();
            Random random = new Random();
            if(blockListType.equalsIgnoreCase("whitelist")) {
                if (blockSet.contains(ForgeRegistries.BLOCKS.getKey(e.getState().getBlock()))) {
                    if (blockDropChance != 0.0 && blockDropChance >= random.nextDouble() * 100) {
                        // For debugging
                        // e.getPlayer().displayClientMessage(new StringTextComponent(e.getPos().getX() + " " + e.getPos().getY() + " " + e.getPos().getZ()), true);
                        world.addFreshEntity(new OrbEntity((EntityType<OrbEntity>) entityType.get(), world, e.getPos().getX(), e.getPos().getY(),
                                e.getPos().getZ(), s, OrbsData.getOrbData(s)));
                    }
                }
            }
            else if(blockListType.equalsIgnoreCase("blacklist")){
                if (!blockSet.contains(ForgeRegistries.BLOCKS.getKey(e.getState().getBlock()))) {
                    if (blockDropChance != 0.0 && blockDropChance >= random.nextDouble() * 100) {
                        // For debugging
                        // e.getPlayer().displayClientMessage(new StringTextComponent(e.getPos().getX() + " " + e.getPos().getY() + " " + e.getPos().getZ()), true);
                        world.addFreshEntity(new OrbEntity((EntityType<OrbEntity>) entityType.get(), world, e.getPos().getX(), e.getPos().getY(),
                                e.getPos().getZ(), s, OrbsData.getOrbData(s)));
                    }
                }
            }
        });
    }
    private void entityDropEvent(LivingDeathEvent e){
        LivingEntity entity = e.getEntity();
        Level world = entity.getCommandSenderWorld();
        Entity sourceEntity = e.getSource().getEntity();
        if(sourceEntity instanceof Player) {
            OrbsRegistry.getOrbs().forEach((s, entityType) -> {
                List<ResourceLocation> entitySet = OrbsData.getOrbData(s).getData().getEntitySet();
                String entityListType = OrbsData.getOrbData(s).getData().getEntityListType();
                double entityDropChance = OrbsData.getOrbData(s).getData().getEntityDropChance();
                Random random = new Random();
                if(entityListType.equalsIgnoreCase("whitelist")){
                    if (entitySet.contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()))) {
                        if (entityDropChance != 0.0 && entityDropChance >= random.nextDouble()*100) {
                            // For debugging
                            // playerEntity.displayClientMessage(new TextComponent(entity.getX() + " " + entity.getY() + " " + entity.getZ()), true);
                            world.addFreshEntity(new OrbEntity((EntityType<OrbEntity>) entityType.get(), world, entity.getX(), entity.getY(),
                                    entity.getZ(), s, OrbsData.getOrbData(s)));
                        }
                    }
                }
                else if(entityListType.equalsIgnoreCase("blacklist")){
                    if (!entitySet.contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()))) {
                        if (entityDropChance != 0.0 && entityDropChance >= random.nextDouble()*100) {
                            // For debugging
                            // playerEntity.displayClientMessage(new TextComponent(entity.getX() + " " + entity.getY() + " " + entity.getZ()), true);
                            world.addFreshEntity(new OrbEntity((EntityType<OrbEntity>) entityType.get(), world, entity.getX(), entity.getY(),
                                    entity.getZ(), s, OrbsData.getOrbData(s)));
                        }
                    }
                }
            });
        }
    }
}
