package com.decursioteam.pickable_orbs.registries;

import com.decursioteam.pickable_orbs.PickableOrbs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Registry {

    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, PickableOrbs.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> GET_HEART_SOUND = REGISTRY.register("empty",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(PickableOrbs.MOD_ID, "get_heart_sound")));

}