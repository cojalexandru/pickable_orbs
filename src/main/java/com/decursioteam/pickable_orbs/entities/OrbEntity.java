package com.decursioteam.pickable_orbs.entities;

import com.decursioteam.pickable_orbs.PickableOrbs;
import com.decursioteam.pickable_orbs.datagen.Orbs;
import com.decursioteam.pickable_orbs.datagen.OrbsData;
import com.decursioteam.pickable_orbs.registries.Registry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerEntity;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public class OrbEntity extends Entity {
    public int tickCount;
    private int age;
    private int throwTime;
    private int health = 5;
    protected final Orbs orbData;
    protected final String orbType;
    private Player followingPlayer;
    private int followingTime;

    public OrbEntity(EntityType<OrbEntity> type, Level world, double x, double y, double z, String orbType, Orbs orbData) {
        super(type, world);
        this.orbData = orbData;
        this.orbType = orbType;
        this.setPos(x, y, z);
        this.yRotO = (float) (this.random.nextDouble() * 360.0D);
        this.setDeltaMovement((this.random.nextDouble() * 0.2F - 0.1F) * 2.0D, this.random.nextDouble() * 0.2D * 2.0D, (this.random.nextDouble() * 0.2F - 0.1F) * 2.0D);
    }

    public OrbEntity(EntityType<OrbEntity> type, Level world, String orbType) {
        super(type, world);
        this.orbType = orbType;
        this.orbData = OrbsData.getOrbData(orbType);
    }

    @Override
    public void tick() {
        super.tick();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        if (this.isEyeInFluid(FluidTags.WATER)) {
            this.setUnderwaterMovement();
        } else if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));
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
            BlockPos pos = new BlockPos((int) this.getX(), (int) (this.getY() - 1.0D), (int) this.getZ());
            friction = this.level().getBlockState(pos).getFriction(this.level(), pos, this) * 0.98F;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 0.98D, friction));

        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, -0.9D, 1.0D));
        }

        if (orbData.getExtraData().getFollowPlayer()) {
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
            this.discard(); // 'remove(RemovalReason.DISCARDED)' is now just 'discard()'
        }
    }

    private void setUnderwaterMovement() {
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x * 0.99F, Math.min(movement.y + 5.0E-4F, 0.06F), movement.z * 0.99F);
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (this.level().isClientSide || this.isRemoved()) return false;
        if (this.isInvulnerableTo(source)) return false;
        this.health -= damage;
        if (this.health <= 0) {
            this.kill(); // 'remove(RemovalReason.KILLED)' is now just 'kill()'
        }
        return true;
    }

    @Override
    public @NotNull InteractionResult interact(Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).is(Items.GLASS_BOTTLE) && orbData.getExtraData().getBottleable()) {
            try {
                // ForgeRegistries -> BuiltInRegistries
                var mobEffect = BuiltInRegistries.MOB_EFFECT.get(orbData.getData().getType());
                if (mobEffect != null) {
                    MobEffectInstance mobEffectInstance = new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(mobEffect), orbData.getData().getEffectDuration(), orbData.getData().getEffectMultiplier());
                    ItemStack stack = new ItemStack(Items.POTION);

                    PotionContents contents = new PotionContents(Optional.empty(), Optional.empty(), Collections.singletonList(mobEffectInstance));
                    stack.set(DataComponents.POTION_CONTENTS, contents);

                    stack.set(DataComponents.CUSTOM_NAME, Component.literal("Potion of " + orbData.getData().getName()).withStyle(ChatFormatting.RESET));

                    ItemHandlerHelper.giveItemToPlayer(player, stack);
                    this.discard();
                    player.getItemInHand(hand).shrink(1); // 'setCount(count - 1)' -> 'shrink(1)'
                }
            } catch (Exception e) {
                // PickableOrbs.LOGGER.debug("[PickableOrbs] - The orb type of: " + orbData.getData().getType() + " is invalid.");
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void playerTouch(Player playerEntity) {
        if (!this.level().isClientSide) {
            if (this.throwTime == 0) {
                if (this.age >= orbData.getExtraData().getPickupDelay()) {
                    playerEntity.take(this, 1);
                    int effectMultiplier = orbData.getData().getEffectMultiplier();
                    int effectDuration = orbData.getData().getEffectDuration();

                    // ResourceLocation instantiation changed to fromNamespaceAndPath
                    if(Objects.equals(orbData.getData().getType(), ResourceLocation.fromNamespaceAndPath("pickable_orbs", "percentage_healing"))){
                        playerEntity.heal((float) (playerEntity.getMaxHealth() * ((float)orbData.getData().getEffectMultiplier()) / 100.0));
                    }
                    else try {
                        var mobEffect = BuiltInRegistries.MOB_EFFECT.get(orbData.getData().getType());
                        if (mobEffect != null) {
                            playerEntity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(mobEffect), effectDuration, effectMultiplier));
                        }
                    } catch (Exception e) {
                        PickableOrbs.LOGGER.error("[PickableOrbs] - The orb type of: " + orbData.getData().getName() + " is invalid.");
                    }

                    if(!orbData.getExtraData().getPickupMessage().isEmpty())
                        playerEntity.displayClientMessage(Component.literal(orbData.getExtraData().getPickupMessage()), true);
                    if(orbData.getExtraData().getSound()) this.playSound(Registry.GET_HEART_SOUND.get(), 0.4F, 0.95F);
                    this.discard();
                }
            }
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }
}