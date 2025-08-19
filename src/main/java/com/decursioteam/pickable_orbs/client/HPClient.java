package com.decursioteam.pickable_orbs.client;

import com.decursioteam.pickable_orbs.datagen.OrbsData;
import com.decursioteam.pickable_orbs.registries.OrbsRegistry;
import com.decursioteam.pickable_orbs.renderers.OrbEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class HPClient {
    public static void register() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(HPClient::setupClient);
    }

    protected static void setupClient(FMLClientSetupEvent event) {
        OrbsRegistry.getOrbs().forEach((s, entityType) ->
                EntityRenderers.register(entityType.get(),
                        manager -> new OrbEntityRenderer(manager, OrbsData.getOrbData(s).getData(), OrbsData.getOrbData(s).getExtraData())));
    }
}