package com.decursioteam.pickable_orbs.client;

import com.decursioteam.pickable_orbs.PickableOrbs;
import com.decursioteam.pickable_orbs.registries.OrbsRegistry;
import com.decursioteam.pickable_orbs.renderers.OrbEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = PickableOrbs.MOD_ID, value = Dist.CLIENT)
public class HPClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(OrbsRegistry.ORB.get(), OrbEntityRenderer::new);
    }
}