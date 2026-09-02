package minecraft.entity.data;

import java.lang.reflect.Field;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import sys.jvm.reflection;
import sys.jvm.unsafe;

/**
 * 实体内存数据操作
 */
@SuppressWarnings("unchecked")
public class EntityData {

	private static Field Entity_entityData;
	private static Field Entity_position;
	private static Field Entity_blockPosition;
	private static Field Entity_chunkPosition;
	private static Field Entity_forgeFluidTypeHeight;
	private static Field Entity_level;
	private static Field Entity_passengers;

	static {
		Entity_entityData = reflection.find_declared_field(Entity.class, "entityData");
		Entity_position = reflection.find_declared_field(Entity.class, "position");
		Entity_blockPosition = reflection.find_declared_field(Entity.class, "blockPosition");
		Entity_chunkPosition = reflection.find_declared_field(Entity.class, "chunkPositio");
		Entity_level = reflection.find_declared_field(Entity.class, "level");
		Entity_passengers = reflection.find_declared_field(Entity.class, "passengers");
		Entity_forgeFluidTypeHeight = reflection.find_declared_field(Entity.class, "forgeFluidTypeHeight");
	}

	/**
	 * 两种实体的所有entityData字段及其顺序必须完全相同，否则使用EntityDataAccessor会导致访问到错误的或不存在的值。<br>
	 * 
	 * @param <_T>
	 * @param entityClazz
	 * @param entityData
	 * @return
	 */
	public static final <_T extends Entity> void setEntityData(_T entity, SynchedEntityData entityData) {
		unsafe.write(entity, Entity_entityData, entityData);
	}

	public static final <_T extends Entity> void setPosition(_T entity, Vec3 pos, BlockPos blockPos, ChunkPos chunkPos) {
		unsafe.write(entity, Entity_position, pos);
		unsafe.write(entity, Entity_blockPosition, blockPos);
		unsafe.write(entity, Entity_chunkPosition, chunkPos);
	}

	public static final <_T extends Entity> Vec3 getPosition(_T entity) {
		return (Vec3) unsafe.read_reference(entity, Entity_position);
	}

	public static final <_T extends Entity> BlockPos getBlockPosition(_T entity) {
		return (BlockPos) unsafe.read_reference(entity, Entity_blockPosition);
	}

	public static final <_T extends Entity> ChunkPos getChunkPosition(_T entity) {
		return (ChunkPos) unsafe.read_reference(entity, Entity_chunkPosition);
	}

	public static final <_T extends Entity> void setFluidTypeHeight(_T entity, Object2DoubleMap<FluidType> fluidTypeHeight) {
		unsafe.write(entity, Entity_forgeFluidTypeHeight, fluidTypeHeight);
	}

	public static final <_T extends Entity> Object2DoubleMap<FluidType> getFluidTypeHeight(_T entity) {
		return (Object2DoubleMap<FluidType>) unsafe.read_reference(entity, Entity_forgeFluidTypeHeight);
	}

	public static final <_T extends Entity> void setLevel(_T entity, Level level) {
		unsafe.write(entity, Entity_level, level);
	}

	public static final <_T extends Entity> void setPassengers(_T entity, ImmutableList<Entity> passengers) {
		unsafe.write(entity, Entity_passengers, passengers);
	}

	private static final long TOP_Entity;
	private static final long BOTTOM_Entity;

	private static final long BOTTOM_LivingEntity;

	private static final long TOP_Mob;
	private static final long BOTTOM_Mob;

	static {
		TOP_Entity = unsafe.top_offset(Entity.class);
		BOTTOM_Entity = unsafe.bottom_offset(Entity.class);
		BOTTOM_LivingEntity = unsafe.bottom_offset(LivingEntity.class);
		TOP_Mob = unsafe.top_offset(Mob.class);
		BOTTOM_Mob = unsafe.bottom_offset(Mob.class);
	}

	/**
	 * 拷贝从Entity到Mob继承链的实体数据
	 * 
	 * @param src
	 * @param dest
	 */
	public static final void copyData(Entity src, Entity dest) {
		if (src instanceof Mob srcMob) {
			if (dest instanceof Mob destMob)
				copyMobData(srcMob, destMob);
			else if (dest instanceof LivingEntity destLivingEntity)
				copyLivingEntityData(srcMob, destLivingEntity);
			else if (dest instanceof Entity destEntity)
				copyEntityData(srcMob, destEntity);
		} else if (src instanceof LivingEntity srcLivingEntity) {
			if (dest instanceof LivingEntity destLivingEntity)
				copyLivingEntityData(srcLivingEntity, destLivingEntity);
			else if (dest instanceof Entity destEntity)
				copyEntityData(srcLivingEntity, destEntity);
		} else if (src instanceof Entity srcEntity) {
			if (dest instanceof Entity destEntity)
				copyEntityData(srcEntity, destEntity);
		}
	}

	/**
	 * 拷贝Entity.class的成员字段，但entityData保留原本的。
	 * 
	 * @param <_T>
	 * @param src
	 * @param dest
	 */
	public static final void copyEntityData(Entity src, Entity dest) {
		SynchedEntityData entityData = dest.getEntityData();
		unsafe.copy_member_fields(src, dest, TOP_Entity, BOTTOM_Entity);// Forge新加了字段导致BOTTOM_Entity比TOP_LivingEntity还大，因此可能会拷贝部分LivingEntity的数据
		setEntityData(dest, entityData);
	}

	private static Field LivingEntity_attributes;
	private static Field LivingEntity_walkAnimation;

	static {
		LivingEntity_attributes = reflection.find_declared_field(LivingEntity.class, "attributes");
		LivingEntity_walkAnimation = reflection.find_declared_field(LivingEntity.class, "walkAnimation");
	}

	public static final <_T extends LivingEntity> void setAttributes(_T entity, AttributeMap attributes) {
		unsafe.write(entity, LivingEntity_attributes, attributes);
	}

	public static final <_T extends LivingEntity> AttributeMap getAttributes(_T entity) {
		return (AttributeMap) unsafe.read_reference(entity, LivingEntity_attributes);
	}

	public static final <_T extends LivingEntity> void setWalkAnimation(_T entity, WalkAnimationState walkAnimation) {
		unsafe.write(entity, LivingEntity_walkAnimation, walkAnimation);
	}

	public static final <_T extends LivingEntity> WalkAnimationState getWalkAnimation(_T entity) {
		return (WalkAnimationState) unsafe.read_reference(entity, LivingEntity_walkAnimation);
	}

	public static final void copyLivingEntityData(LivingEntity src, LivingEntity dest) {
		SynchedEntityData entityData = dest.getEntityData();
		// 1.20.1 Forge新加了字段导致Entity类和LivingEntity类字段偏移量区间有重叠，因此需要在整体拷贝完成以后再次设定entityData
		unsafe.copy_member_fields(src, dest, TOP_Entity, BOTTOM_LivingEntity);
		setEntityData(dest, entityData);
	}

	private static Field Mob_handItems;
	private static Field Mob_armorItems;
	private static Field Mob_navigation;

	static {
		Mob_handItems = reflection.find_declared_field(Mob.class, "handItems");
		Mob_armorItems = reflection.find_declared_field(Mob.class, "armorItems");
		Mob_navigation = reflection.find_declared_field(Mob.class, "navigation");
	}

	public static final <_T extends Mob> void setHandItems(_T entity, NonNullList<ItemStack> handItems) {
		unsafe.write(entity, Mob_handItems, handItems);
	}

	public static final <_T extends Mob> void setArmorItems(_T entity, NonNullList<ItemStack> armorItems) {
		unsafe.write(entity, Mob_armorItems, armorItems);
	}

	public static final <_T extends Mob> PathNavigation getNavigation(_T entity) {
		return (PathNavigation) unsafe.read_reference(entity, Mob_navigation);
	}

	public static final <_T extends Mob> void setNavigation(_T entity, PathNavigation navigation) {
		unsafe.write(entity, Mob_navigation, navigation);
	}

	public static final void copyMobData(Mob src, Mob dest) {
		copyLivingEntityData(src, dest);
		unsafe.copy_member_fields(src, dest, TOP_Mob, BOTTOM_Mob);
	}

	private static Field PathNavigation_speedModifier;

	static {
		PathNavigation_speedModifier = reflection.find_declared_field(PathNavigation.class, "speedModifier");
	}

	public static final double getSpeedModifier(PathNavigation navigation) {
		return (double) unsafe.read_double(navigation, PathNavigation_speedModifier);
	}

	public static final void syncEntityData(Entity srcEntity, Entity destEntity) {
		copyData(srcEntity, destEntity);// 同步内存字段值
		SynchedEntityDataOp.copyData(srcEntity.getEntityData(), destEntity.getEntityData());// 同步SynchedEntityData各项值
	}
}
