package minecraft.entity;

import minecraft.entity.data.SynchedEntityDataOp;
import minecraft.entity.data.SynchedEntityDataOp.DataEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * 原版玩家模型实体
 */
public interface Humanoid {
	/**
	 * 皮肤
	 * 
	 * @return
	 */
	public ResourceLocation getSkin();

	/**
	 * 皮肤是否是女性
	 * 
	 * @return
	 */
	public boolean isSlim();

	public static class PlayerModelAsset {
		public static final PlayerModelAsset DEFAULT = new PlayerModelAsset(ResourceLocation.parse("minecraft:textures/entity/player/wide/steve.png"), false);

		private ResourceLocation skin;
		private boolean slim;

		public PlayerModelAsset(ResourceLocation skin, boolean slim) {
			this.skin = skin;
			this.slim = slim;
		}

		public ResourceLocation getSkin() {
			return skin;
		}

		public void setSkin(ResourceLocation skin) {
			this.skin = skin;
		}

		public boolean isSlim() {
			return slim;
		}

		public void setIsSlim(boolean slim) {
			this.slim = slim;
		}
	}

	public static final float HUMANOID_WIDTH = 0.6f;
	public static final float HUMANOID_HEIGHT = 1.8f;

	@SuppressWarnings("unchecked")
	public static interface HumanoidEntity extends Humanoid {

		public static final String TAG_SKIN = "skin";
		public static final String TAG_IS_SLIM = "skin_is_slim";

		public static final int IDX_SKIN = 0;
		public static final int IDX_IS_SLIM = 1;

		public static EntityDataAccessor<?>[] defineAllHumanoidEntityData(Class<? extends Entity> entityClazz) {
			return SynchedEntityDataOp.define(entityClazz,
					DataEntry.of(EntityDataSerializers.STRING, PlayerModelAsset.DEFAULT.getSkin().toString()),
					DataEntry.of(EntityDataSerializers.BOOLEAN, PlayerModelAsset.DEFAULT.isSlim()));
		}

		public default void storeAllHumanoidEntityData(CompoundTag compound, SynchedEntityData entityData) {
			SynchedEntityDataOp.storeString(compound, TAG_SKIN, entityData, humanoidEntityDataAccs()[IDX_SKIN]);
			SynchedEntityDataOp.storeBool(compound, TAG_IS_SLIM, entityData, humanoidEntityDataAccs()[IDX_IS_SLIM]);
		}

		public default void loadAllHumanoidEntityData(CompoundTag compound, SynchedEntityData entityData) {
			SynchedEntityDataOp.loadString(compound, TAG_SKIN, entityData, humanoidEntityDataAccs()[IDX_SKIN]);
			SynchedEntityDataOp.loadBool(compound, TAG_IS_SLIM, entityData, humanoidEntityDataAccs()[IDX_IS_SLIM]);
		}

		/**
		 * 必须调用
		 * 
		 * @param acc
		 */
		public default void onHumanoidEntitySyncedDataUpdated(EntityDataAccessor<?> acc) {
			if (humanoidEntityDataAccs()[IDX_SKIN].equals(acc)) {
				updateSkin(getSkinId());
			}
		}

		public abstract void updateSkin(String skinId);

		/**
		 * 继承Entity类自动实现
		 */
		public abstract SynchedEntityData entityData();

		/**
		 * 高频调用，必须返回defineAllEntityData()得到的static数组
		 * 
		 * @return
		 */
		@SuppressWarnings("rawtypes")
		public abstract EntityDataAccessor[] humanoidEntityDataAccs();

		public default String getSkinId() {
			return (String) entityData().get(humanoidEntityDataAccs()[IDX_SKIN]);
		}

		/**
		 * 设置皮肤
		 * 
		 * @param skin
		 */
		public default void setSkinId(String skin) {
			entityData().set(humanoidEntityDataAccs()[IDX_SKIN], skin);
		}

		@Override
		public default boolean isSlim() {
			return (boolean) entityData().get(humanoidEntityDataAccs()[IDX_IS_SLIM]);
		}

		public default void seIsSlim(boolean slim) {
			entityData().set(humanoidEntityDataAccs()[IDX_IS_SLIM], slim);
		}
	}
}
