package com.decursioteam.pickable_orbs.registries;

import com.decursioteam.pickable_orbs.PickableOrbs;
import com.decursioteam.pickable_orbs.entities.OrbEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class OrbsRegistry {

    public static final DeferredRegister<EntityType<?>> ORB_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, PickableOrbs.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<OrbEntity>> ORB = ORB_TYPES.register("orb", () -> {
        ResourceKey<EntityType<?>> entityKey = ResourceKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(PickableOrbs.MOD_ID, "orb")
        );
        return EntityType.Builder
                .<OrbEntity>of(OrbEntity::new, MobCategory.MISC)
                .sized(0.6f, 0.3f)
                .build(entityKey);
    });

}