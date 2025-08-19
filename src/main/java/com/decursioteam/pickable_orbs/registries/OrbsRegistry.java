package com.decursioteam.pickable_orbs.registries;

import com.decursioteam.pickable_orbs.PickableOrbs;
import com.decursioteam.pickable_orbs.entities.OrbEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OrbsRegistry {

    public static DeferredRegister<EntityType<?>> ORB_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PickableOrbs.MOD_ID);
    private static final Map<String, RegistryObject<EntityType<? extends OrbEntity>>> ORBS = new HashMap<>();

    public static Set<RegistryObject<EntityType<? extends OrbEntity>>> getOrbsHashSet() {
        return new HashSet<>(ORBS.values());
    }
    public static Map<String, RegistryObject<EntityType<? extends OrbEntity>>> getOrbs() {
        return ORBS;
    }
}
