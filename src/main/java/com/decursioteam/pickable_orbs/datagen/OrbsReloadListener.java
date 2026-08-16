package com.decursioteam.pickable_orbs.datagen;

import com.decursioteam.pickable_orbs.PickableOrbs;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OrbsReloadListener extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {

    private static final Gson GSON = new Gson();
    private static final String FOLDER = "pickable_orbs";

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Identifier, JsonElement> map = new HashMap<>();
        for (Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> entry : resourceManager.listResources(FOLDER, id -> id.getPath().endsWith(".json")).entrySet()) {
            Identifier id = entry.getKey();
            String path = id.getPath();
            Identifier name = Identifier.fromNamespaceAndPath(id.getNamespace(), path.substring(FOLDER.length() + 1, path.length() - 5));
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement jsonElement = net.minecraft.util.GsonHelper.fromJson(GSON, reader, JsonElement.class);
                if (jsonElement != null) {
                    map.put(name, jsonElement);
                }
            } catch (Exception e) {
                PickableOrbs.LOGGER.error("Couldn't parse data file {} from {}", name, id, e);
            }
        }
        return map;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        PickableOrbs.LOGGER.info("Loading Custom Orbs from Datapacks...");
        OrbsData.getRegistry().clearData();

        for (Map.Entry<Identifier, JsonElement> entry : pObject.entrySet()) {
            Identifier id = entry.getKey();
            JsonElement jsonElement = entry.getValue();

            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                String name = id.getPath();
                try {
                    name = Codec.STRING.fieldOf("name").orElse(name).codec().fieldOf("OrbData").codec().parse(JsonOps.INSTANCE, jsonObject).getOrThrow(IllegalArgumentException::new);
                    OrbsData.getRegistry().cacheRawOrbsData(name.toLowerCase(Locale.ENGLISH).replace(" ", "_"), jsonObject);
                } catch (Exception e) {
                    PickableOrbs.LOGGER.error("Failed to parse orb data for {}", id, e);
                }
            }
        }

        OrbsData.getRegistry().regenerateCustomOrbsData();
        PickableOrbs.LOGGER.info("Loaded {} custom orbs.", OrbsData.getRegistry().getOrbs().size());
    }
}
