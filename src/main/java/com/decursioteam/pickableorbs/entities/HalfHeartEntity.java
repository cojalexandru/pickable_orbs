package com.decursioteam.pickableorbs.entities;

import com.decursioteam.pickableorbs.PickableOrbs;
import com.decursioteam.pickableorbs.datagen.Orbs;
import com.decursioteam.pickableorbs.datagen.OrbsData;
import com.decursioteam.pickableorbs.registries.Registry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Objects;

public class HalfHeartEntity extends Entity {
    public int tickCount;
    private int age;
    private int throwTime;
    private int health = 5;
    protected final Orbs orbData; // Data for the orb.
    protected final String orbType; // Type of orb.
    private Player followingPlayer; // Player to follow.
    private int followingTime;

    // Constructor to spawn the entity at a specific position with orb data.
    public HalfHeartEntity(EntityType<HalfHeartEntity> type, Level world, double x, double y, double z, String orbType, Orbs orbData) {
        super(type, world);
        this.orbData = orbData;
        this.orbType = orbType;
        this.setPos(x, y, z);
        this.yRotO = (float) (this.random.nextDouble() * 360.0D);
        this.setDeltaMovement((this.random.nextDouble() * 0.2F - 0.1F) * 2.0D, this.random.nextDouble() * 0.2D * 2.0D, (this.random.nextDouble() * 0.2F - 0.1F) * 2.0D);
    }

    // Alternate constructor for other use cases (e.g., when loading from saved data).
    public HalfHeartEntity(EntityType<HalfHeartEntity> type, Level world, String orbType) {
        super(type, world);
        this.orbType = orbType;
        this.orbData = OrbsData.getOrbData(orbType);
    }

    // Set bounding box for interaction.
    @Override
    protected AABB getBoundingBoxForPose(Pose pose) {
        return new AABB(-0.3D, 0.0D, -0.3D, 0.3D, 0.3D, 0.3D); // Adjusted to ensure proper interaction.
    }

    @Override
    public void tick() {
        super.tick();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        // Handle water physics.
        if (this.isEyeInFluid(FluidTags.WATER)) {
            this.setUnderwaterMovement();
        } else if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));
        }

        // Handle lava effects.
        if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
            this.setDeltaMovement((this.random.nextFloat() - this.random.nextFloat()) * 0.2F, 0.2D, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
        }

        // Move entity and handle collision.
        if (!this.level().noCollision(this.getBoundingBox())) {
            this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
        }
        this.move(MoverType.SELF, this.getDeltaMovement());

        // Adjust motion based on ground friction.
        float friction = 0.98F;
        if (this.onGround()) {
            BlockPos pos = new BlockPos((int) this.getX(), (int) (this.getY() - 1.0D), (int) this.getZ());
            friction = this.level().getBlockState(pos).getFriction(this.level(), pos, this) * 0.98F;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 0.98D, friction));

        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, -0.9D, 1.0D));
        }

        // Follow player logic
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

        // Age and removal logic
        this.age++;
        if (this.age >= 6000) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    private void setUnderwaterMovement() {
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x * 0.99F, Math.min(movement.y + 5.0E-4F, 0.06F), movement.z * 0.99F);
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (this.level().isClientSide || this.isRemoved()) return false; // Prevent damage on client side
        if (this.isInvulnerableTo(source)) return false; // Check for invulnerability
        this.health -= damage;
        if (this.health <= 0) {
            this.remove(RemovalReason.KILLED);
        }
        return true; // Return true to indicate it was hurt
    }

    @Override
    public @NotNull InteractionResult interact(Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).is(Items.GLASS_BOTTLE) && orbData.getExtraData().getBottleable()) {
            try {
                MobEffectInstance mobEffectInstance = new MobEffectInstance(Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getValue(orbData.getData().getType())), orbData.getData().getEffectDuration(), orbData.getData().getEffectMultiplier());
                ItemStack stack = new ItemStack(Items.POTION);
                PotionUtils.setCustomEffects(stack, Collections.singleton(mobEffectInstance));
                stack.getTag().putInt("CustomPotionColor", PotionUtils.getColor(Collections.singleton(mobEffectInstance)));
                stack.getOrCreateTagElement("display").putString("Name", Component.Serializer.toJson(Component.literal("Potion of " + orbData.getData().getName()).withStyle(ChatFormatting.RESET)));
                ItemHandlerHelper.giveItemToPlayer(player, stack);
                this.remove(RemovalReason.DISCARDED);
                player.getItemInHand(hand).setCount(player.getItemInHand(hand).getCount() - 1);
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
                    if(Objects.equals(orbData.getData().getType(), new ResourceLocation("pickableorbs:percentage_healing"))){
                        playerEntity.heal((float) (playerEntity.getMaxHealth() * ((float)orbData.getData().getEffectMultiplier()) / 100.0));
                    }
                    else try {
                        playerEntity.addEffect(new MobEffectInstance(Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getValue(orbData.getData().getType())), effectDuration, effectMultiplier));
                    } catch (Exception e) {
                        PickableOrbs.LOGGER.error("[PickableOrbs] - The orb type of: " + orbData.getData().getName() + " is invalid.");
                    }


                    if(!orbData.getExtraData().getPickupMessage().isEmpty())
                        playerEntity.displayClientMessage(Component.literal(orbData.getExtraData().getPickupMessage()), true);
                    if(orbData.getExtraData().getSound()) this.playSound(Registry.GET_HEART_SOUND.get(), 0.4F, 0.95F);
                    this.remove(RemovalReason.DISCARDED);
                }
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        // You can define synced data here
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this); // Handle entity packet for networking
    }

    @Override
    public boolean isPickable() {
        return true; // Ensure this entity can be picked up
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true; // Ensure this entity is not invulnerable
    }

    // You can add additional methods as needed
}
