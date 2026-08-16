package com.decursioteam.pickable_orbs.registries;

import com.decursioteam.pickable_orbs.PickableOrbs;
import com.decursioteam.pickable_orbs.entities.OrbEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class OrbsRegistry {

    public static final DeferredRegister<EntityType<?>> ORB_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, PickableOrbs.MOD_ID);

    private static final Map<String, Supplier<EntityType<OrbEntity>>> ORBS = new HashMap<>();

    public static Set<Supplier<EntityType<OrbEntity>>> getOrbsHashSet() {
        return new HashSet<>(ORBS.values());
    }

    public static Map<String, Supplier<EntityType<OrbEntity>>> getOrbs() {
        return ORBS;
    }
}