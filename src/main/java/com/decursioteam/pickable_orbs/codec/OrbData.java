package com.decursioteam.pickable_orbs.codec;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

public class OrbData {

    public static final OrbData DEFAULT = new OrbData("error");

    public static Codec<OrbData> codec(String name) {
        return RecordCodecBuilder.create(instance -> instance.group(
                MapCodec.of(Encoder.empty(), Decoder.unit(() -> name)).forGetter(OrbData::getName),
                ResourceLocation.CODEC.fieldOf("type").orElse(ResourceLocation.parse("minecraft:instant_health")).forGetter(OrbData::getType),
                Codec.intRange(0, 100).fieldOf("effectMultiplier").orElse(1).forGetter(OrbData::getEffectMultiplier),
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("effectDuration").orElse(100).forGetter(OrbData::getEffectDuration),
                Codec.STRING.fieldOf("color").forGetter(OrbData::getColor),
                ResourceLocation.CODEC.fieldOf("texture").orElse(ResourceLocation.parse("pickable_orbs:textures/entity/plain_orb.png")).forGetter(OrbData::getTexture),
                ResourceLocation.CODEC.listOf().fieldOf("blockList").orElse(ImmutableList.of(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(Blocks.SPAWNER)))).forGetter(OrbData::getBlockSet),
                Codec.STRING.fieldOf("blockListType").orElse("whitelist").forGetter(OrbData::getBlockListType),
                Codec.doubleRange(0.0, 100.0).fieldOf("blockDropChance").orElse(0.0).forGetter(OrbData::getBlockDropChance),
                ResourceLocation.CODEC.listOf().fieldOf("entityList").orElse(ImmutableList.of(Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.RABBIT)))).forGetter(OrbData::getEntitySet),
                Codec.STRING.fieldOf("entityListType").orElse("whitelist").forGetter(OrbData::getEntityListType),
                Codec.doubleRange(0.0, 100.0).fieldOf("entityDropChance").orElse(0.0).forGetter(OrbData::getEntityDropChance)
        ).apply(instance, OrbData::new));
    }


    protected double blockDropChance;
    protected double entityDropChance;
    protected int effectMultiplier;
    protected int effectDuration;
    protected ResourceLocation texture;
    protected List<ResourceLocation> blockSet;
    protected List<ResourceLocation> entitySet;
    protected String color;
    protected String name;
    protected ResourceLocation type;
    protected String blockListType;
    protected String entityListType;

    private OrbData(String name, ResourceLocation type, int effectMultiplier, int effectDuration, String color, ResourceLocation texture, List<ResourceLocation> blockSet, String blockListType, double blockDropChance, List<ResourceLocation> entitySet, String entityListType, double entityDropChance){
        this.name = name;
        this.effectDuration = effectDuration;
        this.effectMultiplier = effectMultiplier;
        this.type = type;
        this.blockDropChance = blockDropChance;
        this.entityDropChance = entityDropChance;
        this.blockListType = blockListType;
        this.entityListType = entityListType;
        this.color = color;
        this.texture = texture;
        this.blockSet = blockSet;
        this.entitySet = entitySet;
    }

    private OrbData(String name) {
        this.name = name;
        this.type = ResourceLocation.parse("minecraft:instant_health");
        this.effectMultiplier = 1;
        this.effectDuration = 100;
        this.blockDropChance = 1.0;
        this.entityDropChance = 50.0;

        this.blockListType = "whitelist";
        this.entityListType = "blacklist";
        this.texture = ResourceLocation.parse("pickable_orbs:textures/entity/plain_orb.png");
        this.color = "#FF4500";
        this.blockSet = new ArrayList<>();
        this.entitySet = new ArrayList<>();
    }

    public int getEffectMultiplier() {
        return effectMultiplier;
    }

    public int getEffectDuration() {
        return effectDuration;
    }

    public List<ResourceLocation> getBlockSet() {
        return blockSet;
    }

    public List<ResourceLocation> getEntitySet() {
        return entitySet;
    }

    public double getBlockDropChance() {
        return blockDropChance;
    }

    public double getEntityDropChance() {
        return entityDropChance;
    }

    public String getColor() {
        return color;
    }

    public String getBlockListType() {
        return blockListType;
    }

    public String getEntityListType() {
        return entityListType;
    }

    public ResourceLocation getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public OrbData toImmutable() {
        return this;
    }

    public static class Mutable extends OrbData {

        public Mutable(String name, ResourceLocation type, int effectMultiplier, int effectDuration, String color, ResourceLocation texture, List<ResourceLocation> blockSet, String blockListType, double blockDropChance, List<ResourceLocation> entitySet, String entityListType, double entityDropChance) {
            super(name, type, effectMultiplier, effectDuration, color, texture, blockSet, blockListType, blockDropChance, entitySet, entityListType, entityDropChance);
        }

        public Mutable(String name) {
            super(name);
        }

        public Mutable setBlockDropChance(double blockDropChance) {
            this.blockDropChance = blockDropChance;
            return this;
        }
        public Mutable setEntityDropChance(double entityDropChance) {
            this.entityDropChance = entityDropChance;
            return this;
        }

        public Mutable setEffectMultiplier(int effectMultiplier) {
            this.effectMultiplier = effectMultiplier;
            return this;
        }

        public Mutable setEffectDuration(int effectDuration) {
            this.effectMultiplier = effectDuration;
            return this;
        }

        public Mutable setColor(String color) {
            this.color = color;
            return this;
        }

        public Mutable setBlockListType(String blockListType) {
            this.blockListType = blockListType;
            return this;
        }

        public Mutable setEntityListType(String entityListType) {
            this.entityListType = entityListType;
            return this;
        }

        public Mutable setBlockSet(List<ResourceLocation> blockSet) {
            this.blockSet = blockSet;
            return this;
        }

        public Mutable setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public OrbData toImmutable() {
            return new OrbData(this.name, this.type, this.effectMultiplier, this.effectDuration, this.color, this.texture, this.blockSet, this.blockListType, this.blockDropChance, this.entitySet, this.entityListType, this.entityDropChance);
        }
    }
}