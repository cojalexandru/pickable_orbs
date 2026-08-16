package com.decursioteam.pickable_orbs.datagen;

import com.decursioteam.pickable_orbs.PickableOrbs;
import com.decursioteam.pickable_orbs.codec.ExtraOptions;
import com.decursioteam.pickable_orbs.codec.OrbData;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.antlr.v4.runtime.misc.NotNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Orbs{

    public static final Orbs DEFAULT = new Orbs(OrbData.DEFAULT, ExtraOptions.DEFAULT);


    public static Codec<Orbs> codec(String name) {
        return RecordCodecBuilder.create(instance -> instance.group(
                OrbData.codec(name).fieldOf("OrbData").orElseGet((Consumer<String>) s -> PickableOrbs.LOGGER.error("OrbData is REQUIRED!"), null).forGetter(Orbs::getData),
                ExtraOptions.CODEC.fieldOf("ExtraData").orElse(ExtraOptions.DEFAULT).forGetter(Orbs::getExtraData)
        ).apply(instance, Orbs::new));
    }

    protected OrbData orbData;
    protected ExtraOptions extraData;
    protected JsonObject rawData;
    protected ResourceLocation registryID;
    protected EntityType<?> entityType;
    protected MutableComponent displayName;

    private Orbs(OrbData orbData, ExtraOptions extraData) {
        this.orbData = orbData;
        this.extraData = extraData;
        this.rawData = OrbsData.getRegistry().getRawOrbsData(orbData.getName());
        this.registryID = ResourceLocation.fromNamespaceAndPath(PickableOrbs.MOD_ID, orbData.getName() + "_orb");
        this.displayName = Component.translatable("entity.com.decursioteam.pickable_orbs." + orbData.getName() + "_orb");
    }

    private Orbs(Mutable mutable) {
        this.extraData = mutable.extraData.toImmutable();
        this.orbData = mutable.orbData.toImmutable();
        this.rawData = mutable.rawData;
        this.registryID = mutable.registryID;
        this.entityType = mutable.entityType;
        this.displayName = mutable.displayName;
    }

    public OrbData getData() {
        return orbData;
    }

    public ExtraOptions getExtraData() {
        return extraData;
    }

    public @NotNull
    EntityType<?> getEntityType() {
        if (entityType == null) {
            this.entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(registryID.toString()));;
        }
        return entityType == null ? EntityType.EXPERIENCE_ORB : entityType;
    }

    public ResourceLocation getRegistryID() {
        return registryID;
    }

    @Nullable
    public JsonObject getRawData() {
        return rawData;
    }

    public MutableComponent getDisplayName() {
        return displayName;
    }

    public Orbs toImmutable() {
        return this;
    }

    //TODO: javadoc this sub class
    public static class Mutable extends Orbs {
        public Mutable(OrbData orbData, ExtraOptions extraData) {
            super(orbData, extraData);
        }

        public Mutable() {
            super(OrbData.DEFAULT, ExtraOptions.DEFAULT);
        }

        public Mutable setData(OrbData orbData) {
            this.orbData = orbData;
            return this;
        }

        public Mutable setExtraData(ExtraOptions extraData) {
            this.extraData = extraData;
            return this;
        }

        public Mutable setRegistryID(ResourceLocation registryID) {
            this.registryID = registryID;
            return this;
        }

        public Mutable setDisplayName(TranslatableContents displayName) {
            this.displayName = MutableComponent.create(displayName);
            return this;
        }

        public Mutable setRawData(JsonObject rawData) {
            this.rawData = rawData;
            return this;
        }

        public Mutable setEntityType(EntityType<?> entityType) {
            this.entityType = entityType;
            return this;
        }

        @Override
        public Orbs toImmutable() {
            return new Orbs(this);
        }
    }
}
