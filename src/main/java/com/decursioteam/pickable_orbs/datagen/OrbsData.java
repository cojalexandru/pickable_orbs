package com.decursioteam.pickable_orbs.datagen;

import com.decursioteam.pickable_orbs.datagen.utils.IOrbsData;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;

import java.util.*;

public class OrbsData implements IOrbsData {

    private static final OrbsData INSTANCE = new OrbsData();
    private static final Map<String, JsonObject> RAW_DATA = new LinkedHashMap<>();
    private static final Map<String, com.decursioteam.pickable_orbs.datagen.Orbs> CUSTOM_DATA = new LinkedHashMap<>();


    public static OrbsData getRegistry() {
        return INSTANCE;
    }

    public static com.decursioteam.pickable_orbs.datagen.Orbs getOrbData(Identifier orbType) {
        return CUSTOM_DATA.getOrDefault(orbType.getPath().replaceAll("_orb$", ""), com.decursioteam.pickable_orbs.datagen.Orbs.DEFAULT);
    }

    public static com.decursioteam.pickable_orbs.datagen.Orbs getOrbData(String orbType) {
        return CUSTOM_DATA.getOrDefault(orbType, com.decursioteam.pickable_orbs.datagen.Orbs.DEFAULT);
    }

    public void clearData() {
        RAW_DATA.clear();
        CUSTOM_DATA.clear();
    }

    public void regenerateCustomOrbsData() {
        RAW_DATA.forEach((s, jsonObject) -> CUSTOM_DATA.compute(s, (s1, orbData) ->
                com.decursioteam.pickable_orbs.datagen.Orbs.codec(s).parse(JsonOps.INSTANCE, jsonObject)
                        .getOrThrow()));
    }

    @Override
    public JsonObject getRawOrbsData(String orb) {
        return RAW_DATA.get(orb);
    }

    public void cacheRawOrbsData(String orbType, JsonObject orbData) {
        RAW_DATA.computeIfAbsent(orbType.toLowerCase(Locale.ENGLISH).replace(" ", "_"), s -> Objects.requireNonNull(orbData));
    }

    public Map<String, JsonObject> getRawOrbs() {
        return Collections.unmodifiableMap(RAW_DATA);
    }


    public Map<String, com.decursioteam.pickable_orbs.datagen.Orbs> getOrbs() {
        return Collections.unmodifiableMap(CUSTOM_DATA);
    }

    public Set<com.decursioteam.pickable_orbs.datagen.Orbs> getSetOfOrbs() {
        return Collections.unmodifiableSet(new HashSet<>(CUSTOM_DATA.values()));
    }
}
