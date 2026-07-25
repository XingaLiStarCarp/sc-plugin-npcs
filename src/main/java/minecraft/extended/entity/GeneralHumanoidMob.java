package minecraft.extended.entity;

import java.util.function.Supplier;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

import minecraft.entity.EntityRendererType;
import minecraft.entity.Humanoid.HumanoidEntity;
import minecraft.entity.Humanoid.PlayerModelAsset;
import minecraft.entity.data.SynchedEntityDataOp;
import minecraft.entity.mob.BaseMob;
import minecraft.entity.mob.HumanoidMob;
import minecraft.entity.mob.ProxyRenderPlayer.ProxyRenderPlayerEntity;
import minecraft.entity.mob.ProxyRenderPlayer.ProxyRenderPlayerProvider;
import minecraft.extended.tlm.entity.maid.MaidMob;
import minecraft.extended.tlm.entity.maid.ProxyRenderMaid.MaidModelAsset;
import minecraft.extended.tlm.entity.maid.ProxyRenderMaid.ProxyRenderMaidEntity;
import minecraft.extended.tlm.entity.maid.ProxyRenderMaid.ProxyRenderMaidProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

/**
 * 通用人型实体，支持TLM、YSM和玩家模型
 */
public class GeneralHumanoidMob extends BaseMob implements ProxyRenderPlayerProvider, ProxyRenderMaidProvider {

	public static class GeneralHumanoidModelInfo {
		/**
		 * 原版玩家模型
		 */
		public static final int TYPE_HUMANOID = 0;

		/**
		 * TLM女仆或YSM模型
		 */
		public static final int TYPE_MAID = 1;

		/**
		 * 如果有mod修改过玩家的渲染器，则此选项将使用修改后的渲染器。<br>
		 * 如果装了YSM，请勿使用本选项，改用TYPE_MAID。<br>
		 */
		public static final int TYPE_PLAYER = 2;

		public int renderType;
		public PlayerModelAsset vallinaPlayerModel;
		public MaidModelAsset maidModel;

		private GeneralHumanoidModelInfo(int renderType, PlayerModelAsset vallinaPlayerModel, MaidModelAsset maidModel) {
			this.renderType = renderType;
			this.vallinaPlayerModel = vallinaPlayerModel;
			this.maidModel = maidModel;
		}

		public static final GeneralHumanoidModelInfo pack(int renderType, PlayerModelAsset vallinaPlayerModel, MaidModelAsset maidModel) {
			return new GeneralHumanoidModelInfo(renderType, vallinaPlayerModel, maidModel);
		}

		public static final GeneralHumanoidModelInfo DEFAULT = GeneralHumanoidModelInfo.pack(GeneralHumanoidModelInfo.TYPE_HUMANOID, HumanoidMob.RENDERER_TYPE.defaultAsset(), MaidMob.RENDERER_TYPE.defaultAsset());
	}

	public static final EntityRendererType<GeneralHumanoidModelInfo> RENDERER_TYPE = new EntityRendererType<>();

	public static final <T extends BaseMob> RegistryObject<EntityType<T>> newType(Class<T> entityClazz, float width, float height, String typeName, MobCategory category, Supplier<AttributeSupplier> attributes) {
		return BaseMob.newType(entityClazz, width, height, RENDERER_TYPE, typeName, category, attributes);
	}

	public static final <T extends BaseMob> RegistryObject<EntityType<T>> newType(Class<T> entityClazz, float width, float height, String typeName, Supplier<AttributeSupplier> attributes) {
		return newType(entityClazz, width, height, typeName, MobCategory.MISC, attributes);
	}

	static {
		RENDERER_TYPE.setDefaultRenderAsset(GeneralHumanoidModelInfo.DEFAULT);
	}

	public static final String TAG_RENDER_TYPE = "render_type";

	/**
	 * 渲染类型
	 */
	public static final EntityDataAccessor<Integer> ST_RENDER_TYPE = SynchedEntityDataOp.define(GeneralHumanoidMob.class, EntityDataSerializers.INT, RENDERER_TYPE.defaultAsset().renderType);

	/**
	 * 原版玩家数据
	 */
	public static final EntityDataAccessor<?>[] PLAYER_ACCS = HumanoidEntity.defineAllHumanoidEntityData(GeneralHumanoidMob.class);

	private class ProxyPlayer implements ProxyRenderPlayerEntity {

		protected ResourceLocation skin = HumanoidMob.RENDERER_TYPE.defaultAsset().getSkin();

		@Override
		@SuppressWarnings("rawtypes")
		public EntityDataAccessor[] humanoidEntityDataAccs() {
			return PLAYER_ACCS;
		}

		public ProxyPlayer() {
			this.renderingEntity = this.blankRenderingEntity(GeneralHumanoidMob.this);
		}

		protected final Player renderingEntity;

		@Override
		public final Player renderingEntity() {
			return renderingEntity;
		}

		@Override
		public final Entity bindEntity() {
			return GeneralHumanoidMob.this;
		}

		@Override
		public PlayerModelAsset modelAsset() {
			return new PlayerModelAsset(skin, this.isSlim());
		}

		@Override
		public ResourceLocation getSkin() {
			return skin;
		}

		@Override
		public void updateSkin(String skinId) {
			this.skin = ResourceLocation.parse(skinId);
		}
	};

	/**
	 * 女仆数据
	 */
	public static final EntityDataAccessor<?>[] MAID_ACCS = ProxyRenderMaidEntity.defineAllMaidEntityData(GeneralHumanoidMob.class);

	private class ProxyMaid implements ProxyRenderMaidEntity {

		@Override
		@SuppressWarnings("rawtypes")
		public EntityDataAccessor[] maidEntityDataAccs() {
			return MAID_ACCS;
		}

		protected final EntityMaid renderingEntity;

		public ProxyMaid() {
			this.renderingEntity = this.blankRenderingEntity(GeneralHumanoidMob.this);
		}

		@Override
		public boolean isSwingingArms() {
			return GeneralHumanoidMob.this.swinging;
		}

		@Override
		public EntityMaid renderingEntity() {
			return renderingEntity;
		}

		@Override
		public Entity bindEntity() {
			return GeneralHumanoidMob.this;
		}
	}

	/**
	 * 玩家虚假实体
	 */
	@Deprecated
	private ProxyPlayer proxyPlayer;

	/**
	 * proxyPlayer必须使用此方法获取。<br>
	 * 否则实体在实例化时，父类构造函数会触发onSyncedDataUpdated()导致proxyPlayer还未初始化就被访问。
	 * 
	 * @return
	 */
	@Override
	public final ProxyRenderPlayerEntity proxyRenderPlayer() {
		return proxyPlayer == null ? (this.proxyPlayer = this.new ProxyPlayer()) : proxyPlayer;
	}

	/**
	 * 女仆虚假实体
	 */
	@Deprecated
	private ProxyMaid proxyMaid;

	@Override
	public final ProxyRenderMaidEntity proxyRenderMaid() {
		return proxyMaid == null ? (this.proxyMaid = this.new ProxyMaid()) : proxyMaid;
	}

	public GeneralHumanoidMob(EntityType<BaseMob> entityType, EntityRendererType<GeneralHumanoidModelInfo> renderType, Level level) {
		super(entityType, renderType, level);
	}

	public int getRenderType() {
		return entityData.get(ST_RENDER_TYPE);
	}

	public void setRenderType(int type) {
		entityData.set(ST_RENDER_TYPE, type);
	}

	@Override
	protected void loadData(CompoundTag compound, SynchedEntityData entityData) {
		SynchedEntityDataOp.loadInt(compound, TAG_RENDER_TYPE, entityData, ST_RENDER_TYPE);
		proxyRenderPlayer().loadAllHumanoidEntityData(compound, entityData);
		proxyRenderMaid().loadAllMaidEntityData(compound, entityData);
	}

	@Override
	protected void storeData(CompoundTag compound, SynchedEntityData entityData) {
		SynchedEntityDataOp.storeInt(compound, TAG_RENDER_TYPE, entityData, ST_RENDER_TYPE);
		proxyRenderPlayer().storeAllHumanoidEntityData(compound, entityData);
		proxyRenderMaid().storeAllMaidEntityData(compound, entityData);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> acc) {
		super.onSyncedDataUpdated(acc);
		proxyRenderPlayer().onHumanoidEntitySyncedDataUpdated(acc);
	}

	@Override
	public void tick() {
		super.tick();
		switch (this.getRenderType()) {
		case GeneralHumanoidModelInfo.TYPE_PLAYER: {
			proxyRenderPlayer().syncRenderingEntity();
			break;
		}
		case GeneralHumanoidModelInfo.TYPE_MAID: {
			proxyRenderMaid().syncRenderingEntity();
			break;
		}
		}
	}

	@Override
	public void rideTick() {
		super.rideTick();
		switch (this.getRenderType()) {
		case GeneralHumanoidModelInfo.TYPE_PLAYER: {
			break;
		}
		case GeneralHumanoidModelInfo.TYPE_MAID: {
			proxyRenderMaid().tickMaidRide();
			break;
		}
		}
	}

}
