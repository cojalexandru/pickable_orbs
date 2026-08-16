package com.decursioteam.pickable_orbs.entities;

import com.decursioteam.pickable_orbs.PickableOrbs;
import com.decursioteam.pickable_orbs.datagen.Orbs;
import com.decursioteam.pickable_orbs.datagen.OrbsData;
import com.decursioteam.pickable_orbs.registries.Registry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public class OrbEntity extends Entity {

    private static final EntityDataAccessor<String> ORB_TYPE = SynchedEntityData.defineId(OrbEntity.class, EntityDataSerializers.STRING);

    public int tickCount;
    private int age;
    private int throwTime;
    private int health = 5;
    private Player followingPlayer;
    private int followingTime;

    public OrbEntity(EntityType<OrbEntity> type, Level world) {
        super(type, world);
    }

    public OrbEntity(EntityType<OrbEntity> type, Level world, double x, double y, double z, String orbType) {
        super(type, world);
        this.setOrbType(orbType);
        this.setPos(x, y, z);
        this.setYRot(this.random.nextFloat() * 360.0F);
        this.setDeltaMovement((this.random.nextDouble() * 0.2D - 0.1D) * 2.0D, this.random.nextDouble() * 0.2D * 2.0D, (this.random.nextDouble() * 0.2D - 0.1D) * 2.0D);
    }

    public OrbEntity(EntityType<OrbEntity> type, Level world, String orbType) {
        super(type, world);
        this.setOrbType(orbType);
    }

    public String getOrbType() {
        return this.entityData.get(ORB_TYPE);
    }

    public void setOrbType(String orbType) {
        this.entityData.set(ORB_TYPE, orbType);
    }

    public Orbs getOrbData() {
        return OrbsData.getOrbData(this.getOrbType());
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03D;
    }

    @Override
    public void tick() {
        if (this.firstTick && this.level().isClientSide()) {
            this.firstTick = false;
        } else {
            super.tick();

            if (this.getFluidInteraction().isEyeInFluidMatching(this, (var0, type, var2) -> type.getIsWaterLike())) {
                this.setUnderwaterMovement();
            } else if (!this.level().noCollision(this.getBoundingBox())) {
                // Do nothing to gravity if colliding heavily
            } else if (!this.isNoGravity()) {
                this.applyGravity();
            }

            if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
                this.setDeltaMovement((this.random.nextFloat() - this.random.nextFloat()) * 0.2F, 0.2D, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
                this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
            }

            if (!this.level().noCollision(this.getBoundingBox())) {
                this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
            }
            this.move(MoverType.SELF, this.getDeltaMovement());

            float friction = 0.98F;
            if (this.onGround()) {
                BlockPos pos = this.getBlockPosBelowThatAffectsMyMovement();
                friction = this.level().getBlockState(pos).getFriction(this.level(), pos, this) * 0.98F;
            }

            this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 0.98D, friction));

            if (this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, -0.9D, 1.0D));
            }

            if (this.getOrbData().getExtraData().getFollowPlayer()) {
                if (this.followingTime < this.tickCount - 20 + this.getId() % 100) {
                    if (this.followingPlayer == null || this.followingPlayer.distanceToSqr(this) > 64.0D) {
                        this.followingPlayer = this.level().getNearestPlayer(this, 8.0D);
                    }
                    this.followingTime = this.tickCount;
                }

                if (this.followingPlayer != null && this.followingPlayer.isSpectator()) {
                    this.followingPlayer = null;
                }

                if (this.followingPlayer != null) {
                    Vec3 direction = new Vec3(this.followingPlayer.getX() - this.getX(), this.followingPlayer.getY() + (double) this.followingPlayer.getEyeHeight() / 2.0D - this.getY(), this.followingPlayer.getZ() - this.getZ());
                    double distanceSquared = direction.lengthSqr();
                    if (distanceSquared < 64.0D) {
                        double factor = 1.0D - Math.sqrt(distanceSquared) / 8.0D;
                        this.setDeltaMovement(this.getDeltaMovement().add(direction.normalize().scale(factor * factor * 0.1D)));
                    }
                }
            }

            this.age++;
            if (this.age >= 6000) {
                this.discard();
            }
        }
    }

    private void setUnderwaterMovement() {
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x * 0.99F, Math.min(movement.y + 5.0E-4F, 0.06F), movement.z * 0.99F);
    }

    @Override
    public final boolean hurtClient(DamageSource source) {
        return !this.isInvulnerableToBase(source);
    }

    @Override
    public final boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (this.isInvulnerableToBase(source)) {
            return false;
        } else {
            this.markHurt();
            this.health = (int) ((float) this.health - damage);
            if (this.health <= 0) {
                this.discard();
            }
            return true;
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (player.getItemInHand(hand).is(Items.GLASS_BOTTLE) && this.getOrbData().getExtraData().getBottleable()) {
            try {
                var mobEffect = BuiltInRegistries.MOB_EFFECT.get(this.getOrbData().getData().getType());
                if (mobEffect.isPresent()) {
                    MobEffectInstance mobEffectInstance = new MobEffectInstance(
                            BuiltInRegistries.MOB_EFFECT.wrapAsHolder(mobEffect.get().value()),
                            this.getOrbData().getData().getEffectDuration(),
                            this.getOrbData().getData().getEffectMultiplier()
                    );

                    ItemStack stack = new ItemStack(Items.POTION);

                    PotionContents contents = new PotionContents(
                            Optional.empty(),
                            Optional.empty(),
                            Collections.singletonList(mobEffectInstance),
                            Optional.empty()
                    );

                    stack.set(DataComponents.POTION_CONTENTS, contents);
                    stack.set(DataComponents.CUSTOM_NAME, Component.literal("Potion of " + this.getOrbData().getData().getName()).withStyle(ChatFormatting.RESET));

                    player.getInventory().add(stack);

                    this.discard();
                    player.getItemInHand(hand).shrink(1);
                }
            } catch (Exception e) {
                // Ignore
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void playerTouch(Player playerEntity) {
        if (playerEntity instanceof ServerPlayer serverPlayer) {
            if (this.throwTime == 0) {
                if (this.age >= this.getOrbData().getExtraData().getPickupDelay()) {
                    serverPlayer.take(this, 1);
                    int effectMultiplier = this.getOrbData().getData().getEffectMultiplier();
                    int effectDuration = this.getOrbData().getData().getEffectDuration();

                    if (Objects.equals(this.getOrbData().getData().getType(), Identifier.fromNamespaceAndPath("pickable_orbs", "percentage_healing"))) {
                        serverPlayer.heal((float) (serverPlayer.getMaxHealth() * ((float) effectMultiplier) / 100.0));
                    } else try {
                        var mobEffect = BuiltInRegistries.MOB_EFFECT.get(this.getOrbData().getData().getType());
                        if (mobEffect != null && mobEffect.isPresent()) {
                            serverPlayer.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(mobEffect.get().value()), effectDuration, effectMultiplier));
                        }
                    } catch (Exception e) {
                        PickableOrbs.LOGGER.error("[PickableOrbs] - The orb type of: " + this.getOrbData().getData().getName() + " is invalid.");
                    }

                    if (!this.getOrbData().getExtraData().getPickupMessage().isEmpty())
                        serverPlayer.sendSystemMessage(Component.literal(this.getOrbData().getExtraData().getPickupMessage()), true);
                    if (this.getOrbData().getExtraData().getSound())
                        this.playSound(Registry.GET_HEART_SOUND.get(), 0.4F, 0.95F);
                    this.discard();
                }
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ORB_TYPE, "health");
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putShort("Health", (short) this.health);
        output.putShort("Age", (short) this.age);
        output.putString("OrbType", this.getOrbType());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.health = input.getShortOr("Health", (short) 5);
        this.age = input.getShortOr("Age", (short) 0);
        this.setOrbType(input.getStringOr("OrbType", "health"));
    }
}