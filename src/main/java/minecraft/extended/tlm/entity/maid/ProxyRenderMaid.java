package minecraft.extended.tlm.entity.maid;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.github.tartaricacid.touhoulittlemaid.api.client.render.MaidRenderState;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.CustomModelPack;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.datagen.tag.TagEntity;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleManager;
import com.github.tartaricacid.touhoulittlemaid.entity.data.MaidTaskDataMaps;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidSwimManager;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import javabase.HashFunctions;
import jvmsp.reflection;
import jvmsp.unsafe;
import minecraft.ModPaths;
import minecraft.entity.ProxyRenderEntity;
import minecraft.entity.data.BlankEntity;
import minecraft.entity.data.EntityData;
import minecraft.entity.data.SynchedEntityDataOp;
import minecraft.entity.data.SynchedEntityDataOp.DataEntry;
import minecraft.extended.tlm.entity.maid.ProxyRenderMaid.MaidModelAsset;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import scba.ModEntry;

/**
 * 可以作为Touhou Little Maid模组女仆模型渲染的委托接口。<br>
 * 支持YSM模型渲染。<br>
 */
public interface ProxyRenderMaid extends ProxyRenderEntity<EntityMaid, MaidModelAsset> {
	public static final float MIAD_WIDTH = 0.6f;
	public static final float MAID_HEIGHT = 1.8f;

	public static interface ProxyRenderMaidProvider {
		public abstract ProxyRenderMaid proxyRenderMaid();
	}

	/**
	 * 女仆模型信息。<br>
	 * 包含TLM的模型信息和YSM的模型信息。<br>
	 */
	public static final class MaidModelAsset {
		public static final String DEFAULT_MODEL_ID = "";
		public static final String DEFAULT_TEXTURE = "-";
		public static final String DEFAULT_YSM_ANIMATION = "empty";

		public static final MaidModelAsset DEFAULT = new MaidModelAsset();

		private String tlmModelId;
		private boolean isYsmModel;
		private String ysmModelId;
		private String ysmModelTexture;
		private Component ysmModelName;

		private String ysmAnimation = DEFAULT_YSM_ANIMATION;
		private boolean isYsmAnimationPlaying = false;

		public MaidModelAsset(String tlmModelId, boolean isYsmModel, String ysmModelId, String ysmModelTexture, Component ysmModelName) {
			this.tlmModelId = tlmModelId;
			this.isYsmModel = isYsmModel;
			this.ysmModelId = ysmModelId;
			this.ysmModelTexture = ysmModelTexture;
			this.ysmModelName = ysmModelName;
		}

		public MaidModelAsset(String tlmModelId) {
			this(tlmModelId, false, DEFAULT_MODEL_ID, DEFAULT_TEXTURE, Component.empty());
		}

		public MaidModelAsset(String ysmModelId, String ysmModelTexture, Component ysmModelName) {
			this(DEFAULT_MODEL_ID, true, ysmModelId, ysmModelTexture, ysmModelName);
		}

		public MaidModelAsset(MaidModelAsset info) {
			this(info.tlmModelId, info.isYsmModel, info.ysmModelId, info.ysmModelTexture, info.ysmModelName);
		}

		public MaidModelAsset() {
			this(DEFAULT_MODEL_ID);
		}

		public String getTlmModelId() {
			return tlmModelId;
		}

		public void setTlmModelId(String tlmModelId) {
			this.tlmModelId = tlmModelId;
		}

		public boolean isYsmModel() {
			return isYsmModel;
		}

		public void setIsYsmModel(boolean isYsmModel) {
			this.isYsmModel = isYsmModel;
		}

		public String getYsmModelId() {
			return ysmModelId;
		}

		public void setYsmModelId(String ysmModelId) {
			this.ysmModelId = ysmModelId;
		}

		public String getYsmModelTexture() {
			return ysmModelTexture;
		}

		public void setYsmModelTexture(String ysmModelTexture) {
			this.ysmModelTexture = ysmModelTexture;
		}

		public Component getYsmModelName() {
			return ysmModelName;
		}

		public void setYsmModelName(Component ysmModelName) {
			this.ysmModelName = ysmModelName;
		}

		public String getYsmAnimation() {
			return ysmAnimation;
		}

		public void setYsmAnimation(String ysmAnimation) {
			this.ysmAnimation = ysmAnimation;
		}

		public boolean isYsmAnimationPlaying() {
			return isYsmAnimationPlaying;
		}

		public void setIsYsmAnimationPlaying(boolean isYsmAnimationPlaying) {
			this.isYsmAnimationPlaying = isYsmAnimationPlaying;
		}

		/**
		 * 自定义YSM模型路径
		 */
		public static final Path LOCAL_CUSTOM_YSM_MODEL_DIR = ModPaths.config("yes_steve_model/custom");

		private static boolean CACHED_TLM_MODEL_IDS = false;
		private static final ArrayList<String> TLM_MODEL_IDS = new ArrayList<>();

		/**
		 * 获取缓存的TLM模型id列表
		 * 
		 * @return
		 */
		public static final List<String> tlmModelIds() {
			if (!CACHED_TLM_MODEL_IDS) {
				CACHED_TLM_MODEL_IDS = true;
				return retrieveTlmModelIds();
			}

			return TLM_MODEL_IDS;
		}

		/**
		 * 获取最新的TLM模型id列表
		 * 
		 * @return
		 */
		public static final ArrayList<String> retrieveTlmModelIds() {
			TLM_MODEL_IDS.clear();
			List<CustomModelPack<MaidModelInfo>> packList = CustomPackLoader.MAID_MODELS.getPackList();
			for (CustomModelPack<MaidModelInfo> pack : packList) {
				List<MaidModelInfo> modelList = pack.getModelList();
				for (MaidModelInfo info : modelList) {
					TLM_MODEL_IDS.add(info.getModelId().toString());
				}
			}
			return TLM_MODEL_IDS;
		}

		public static final String hashTlmModelId(UUID uuid) {
			String id = HashFunctions.modHash(uuid, tlmModelIds());
			return id == null ? DEFAULT_MODEL_ID : id;
		}

		public static final String YSM_EXTENSION_NAME = ".ysm";
		private static boolean CACHED_LOCAL_CUSTOM_YSM_MODEL_IDS = false;
		private static final ArrayList<String> LOCAL_CUSTOM_YSM_MODEL_IDS = new ArrayList<>();

		/**
		 * 获取缓存的YSM模型id列表
		 * 
		 * @return
		 */
		public static final List<String> localCustomYsmModelIds() {
			if (!CACHED_LOCAL_CUSTOM_YSM_MODEL_IDS) {
				CACHED_LOCAL_CUSTOM_YSM_MODEL_IDS = true;
				return retrieveLocalCustomYsmModelIds();
			}
			return LOCAL_CUSTOM_YSM_MODEL_IDS;
		}

		/**
		 * 获取最新的本地custom文件夹下YSM模型id列表。<br>
		 * 这些模型可能会有未被YSM加载的，需要玩家自行reload。<br>
		 * 
		 * @return
		 */
		public static final List<String> retrieveLocalCustomYsmModelIds() {
			LOCAL_CUSTOM_YSM_MODEL_IDS.clear();
			try (Stream<Path> paths = Files.list(LOCAL_CUSTOM_YSM_MODEL_DIR)) {
				paths.forEach(file -> {
					String name = file.getFileName().toString();
					if (Files.isRegularFile(file)) {
						if (name.endsWith(YSM_EXTENSION_NAME))
							LOCAL_CUSTOM_YSM_MODEL_IDS.add(name);// 文件只要.ysm文件
					} else {
						LOCAL_CUSTOM_YSM_MODEL_IDS.add(name);// 文件夹则直接加入
					}
				});
			} catch (IOException ex) {
				ModEntry.LOGGER.warn("retrieve local custom ysm model ids failed");
			}
			return LOCAL_CUSTOM_YSM_MODEL_IDS;
		}

		public static final String hashLocalCustomYsmModelId(UUID uuid) {
			String id = HashFunctions.modHash(uuid, localCustomYsmModelIds());
			return id == null ? DEFAULT_MODEL_ID : id;
		}
	}

	/**
	 * 创建一个虚假女仆实体，仅用于渲染
	 * 
	 * @param initEntity 数据模板，新的EntityMaid将沿用此实体的数据。
	 * @return
	 */
	public static EntityMaid blankEntityMaid(Entity initEntity) {
		class Symbols {
			private static Field EntityMaid_chatBubbleManager;
			private static Field EntityMaid_swimManager;
			private static Field EntityMaid_taskDataMaps;
			private static Field EntityMaid_handItemsForAnimation;

			static {
				EntityMaid_chatBubbleManager = reflection.find_declared_field(EntityMaid.class, "chatBubbleManager");
				EntityMaid_swimManager = reflection.find_declared_field(EntityMaid.class, "swimManager");
				EntityMaid_taskDataMaps = reflection.find_declared_field(EntityMaid.class, "taskDataMaps");
				EntityMaid_handItemsForAnimation = reflection.find_declared_field(EntityMaid.class, "handItemsForAnimation");
			}
		}
		return BlankEntity.allocate(EntityMaid.class, (entityMaid) -> {
			EntityData.copyData(initEntity, entityMaid);
			unsafe.write(entityMaid, Symbols.EntityMaid_chatBubbleManager, new ChatBubbleManager(entityMaid));
			unsafe.write(entityMaid, Symbols.EntityMaid_swimManager, new MaidSwimManager(entityMaid));// 游泳姿态更新器
			unsafe.write(entityMaid, Symbols.EntityMaid_taskDataMaps, new MaidTaskDataMaps());
			unsafe.write(entityMaid, Symbols.EntityMaid_handItemsForAnimation, new ItemStack[] { ItemStack.EMPTY, ItemStack.EMPTY });// 设置空手持物品，渲染时需要
			entityMaid.renderState = MaidRenderState.ENTITY;
			entityMaid.rouletteAnimPlaying = false;
			entityMaid.rouletteAnim = "empty";
			entityMaid.rouletteAnimDirty = false;
			entityMaid.roamingVarsUpdateFlag = 0;
			entityMaid.roamingVars = new Object2FloatOpenHashMap<>();
			entityMaid.animationId = 0;
			entityMaid.animationRecordTime = -1L;
			entityMaid.shouldReset = false;
		});
	}

	/**
	 * 禁止覆写此方法。<br>
	 * 创建一个虚假的女仆渲染实体。<br>
	 */
	@Override
	public default EntityMaid blankRenderingEntity(Entity bindEntity) {
		return blankEntityMaid(bindEntity);
	}

	public String getTlmModelId();

	public void setTlmModelId(String modelId);

	public boolean isYsmModel();

	public void setIsYsmModel(boolean isYsmModel);

	public String getYsmModelId();

	public void setYsmModelId(String ysmModelId);

	public String getYsmModelTexture();

	public void setYsmModelTexture(String ysmModelTexture);

	public Component getYsmModelName();

	public void setYsmModelName(Component ysmModelName);

	/**
	 * 更新roamingVars，YSM渲染使用
	 * 
	 * @param roamingVars
	 */
	public default void setRoamingVars(Object2FloatOpenHashMap<String> roamingVars) {
		renderingEntity().roamingVars = roamingVars;
	}

	/**
	 * 设置YSM模型
	 * 
	 * @param ysmModelId
	 * @param ysmModelTexture
	 * @param ysmModelName
	 */
	public default void setYsmModel(String ysmModelId, String ysmModelTexture, Component ysmModelName) {
		if (!ysmModelId.equals(this.getYsmModelId())) {
			this.setRoamingVars(new Object2FloatOpenHashMap<>());
			this.stopRouletteAnim();
		}
		setYsmModelId(ysmModelId);
		setYsmModelTexture(ysmModelTexture);
		setYsmModelName(ysmModelName);
	}

	/**
	 * 女仆是否正在挥手。<br>
	 * 
	 * @return
	 */
	public boolean isSwingingArms();

	/**
	 * 同步EntityMaid虚假实体的模型.<br>
	 * 同时同步绑定的的实体数据到maid，包括渲染使用到的实体相关数据。<br>
	 * 该方法必须被子类调用以实时同步渲染模型，每tick调用一次即可。<br>
	 */
	@Override
	public default EntityMaid syncRenderingEntityData() {
		EntityMaid renderingEntity = ProxyRenderEntity.super.syncRenderingEntityData();
		// 同步模型
		renderingEntity.setModelId(this.getTlmModelId());
		renderingEntity.setIsYsmModel(this.isYsmModel());
		renderingEntity.setYsmModel(this.getYsmModelId(), this.getYsmModelTexture(), this.getYsmModelName());
		// 同步挥手
		// TODO: TLM SlashBlade兼容层参数设置
		renderingEntity.setSwingingArms(this.isSwingingArms());
		return renderingEntity;
	}

	/**
	 * 更新女仆骑乘位姿
	 */
	public default void tickMaidRide() {
		Entity bindEntity = bindEntity();
		Entity vehicle = bindEntity.getVehicle();
		if (vehicle != null && !vehicle.getType().is(TagEntity.MAID_VEHICLE_ROTATE_BLOCKLIST)) {
			bindEntity.setYHeadRot(vehicle.getYRot());
			bindEntity.setYBodyRot(vehicle.getYRot());
		}
	}

	/**
	 * 获取当前设置的YSM轮盘动画
	 * 
	 * @return
	 */
	public default String getYsmAnimation() {
		return renderingEntity().rouletteAnim;
	}

	/**
	 * 当前是否播放YSM轮盘动画
	 * 
	 * @return
	 */
	public default boolean isYsmAnimationPlaying() {
		return renderingEntity().rouletteAnimPlaying;
	}

	@OnlyIn(Dist.CLIENT)
	public default void playRouletteAnim(String rouletteAnim) {
		renderingEntity().playRouletteAnim(rouletteAnim);
	}

	@OnlyIn(Dist.CLIENT)
	public default void stopRouletteAnim() {
		renderingEntity().stopRouletteAnim();
	}

	public static ProxyRenderMaid bind(Entity bindEntity, MaidModelAsset model) {
		return Binder.bind(bindEntity, model);
	}

	/**
	 * 绑定实体到女仆模型并实时同步实体数据
	 * 
	 * @param bindEntity
	 * @return
	 */
	public static ProxyRenderMaid bind(Entity bindEntity, BiConsumer<Entity, MaidModelAsset> modelResolver) {
		return Binder.bind(bindEntity, modelResolver);
	}

	/**
	 * 绑定实体的女仆模型
	 */
	@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.FORGE)
	public static class Binder extends ModelBinder<EntityMaid, MaidModelAsset> implements ProxyRenderMaid {

		public static final ProxyRenderMaid bind(Entity bindEntity, MaidModelAsset model) {
			return bind(ProxyRenderMaid.Binder::new, bindEntity, model);
		}

		public static final ProxyRenderMaid bind(Entity bindEntity, BiConsumer<Entity, MaidModelAsset> modelResolver) {
			return bind(bindEntity, MaidModelAsset::new, modelResolver, ProxyRenderMaid.Binder::new);
		}

		protected Binder(Entity bindEntity, MaidModelAsset model) {
			super(bindEntity, model);
		}

		@Override
		public String getTlmModelId() {
			return model.getTlmModelId();
		}

		@Override
		public void setTlmModelId(String tlmModelId) {
			model.setTlmModelId(tlmModelId);
		}

		@Override
		public boolean isYsmModel() {
			return model.isYsmModel();
		}

		@Override
		public void setIsYsmModel(boolean isYsmModel) {
			model.setIsYsmModel(isYsmModel);
		}

		@Override
		public String getYsmModelId() {
			return model.getYsmModelId();
		}

		@Override
		public void setYsmModelId(String ysmModelId) {
			model.setYsmModelId(ysmModelId);
		}

		@Override
		public String getYsmModelTexture() {
			return model.getYsmModelTexture();
		}

		@Override
		public void setYsmModelTexture(String ysmModelTexture) {
			model.setYsmModelTexture(ysmModelTexture);
		}

		@Override
		public Component getYsmModelName() {
			return model.getYsmModelName();
		}

		@Override
		public void setYsmModelName(Component ysmModelName) {
			model.setYsmModelName(ysmModelName);
		}

		@Override
		public MaidModelAsset modelAsset() {
			return model;
		}

		@Override
		public boolean isSwingingArms() {
			if (bindEntity instanceof LivingEntity livingEntity) {
				return livingEntity.swinging;
			} else {
				return false;
			}
		}

		@Override
		public String getYsmAnimation() {
			return model.getYsmAnimation();
		}

		@Override
		public boolean isYsmAnimationPlaying() {
			return model.isYsmAnimationPlaying();
		}

		@Override
		public void playRouletteAnim(String rouletteAnim) {
			model.setYsmAnimation(rouletteAnim);
			model.setIsYsmAnimationPlaying(true);
		}

		@Override
		public void stopRouletteAnim() {
			model.setIsYsmAnimationPlaying(false);
		}
	}

	/**
	 * 继承自Entity的类需要实现的接口
	 */
	@SuppressWarnings("unchecked")
	public static interface ProxyRenderMaidEntity extends ProxyRenderMaid, ProxyRenderMaidProvider {
		@Override
		public default ProxyRenderMaid proxyRenderMaid() {
			return this;
		}

		public static final String TAG_TLM_MODEL_ID = "tlm_model_id";
		public static final String TAG_IS_YSM_MODEL = "is_ysm_model";
		public static final String TAG_YSM_MODEL_ID = "ysm_model_id";
		public static final String TAG_YSM_MODEL_TEXTURE = "ysm_model_texture";
		public static final String TAG_YSM_MODEL_NAME = "ysm_model_name";
		public static final String TAG_YSM_ANIMATION = "ysm_animation";
		public static final String TAG_YSM_ANIMATION_PLAYING = "ysm_animation_playing";

		public static final int IDX_TLM_MODEL_ID = 0;
		public static final int IDX_IS_YSM_MODEL = 1;
		public static final int IDX_YSM_MODEL_ID = 2;
		public static final int IDX_YSM_MODEL_TEXTURE = 3;
		public static final int IDX_YSM_MODEL_NAME = 4;
		public static final int IDX_YSM_ANIMATION = 5;
		public static final int IDX_YSM_ANIMATION_PLAYING = 6;

		/**
		 * 定义此种类实体的数据。<br>
		 * 得到的结果必须储存到static字段。<br>
		 * 
		 * @param entityClazz
		 * @return
		 */
		public static EntityDataAccessor<?>[] defineAllMaidEntityData(Class<? extends Entity> entityClazz) {
			return SynchedEntityDataOp.define(entityClazz,
					DataEntry.of(EntityDataSerializers.STRING, MaidModelAsset.DEFAULT.getTlmModelId()),
					DataEntry.of(EntityDataSerializers.BOOLEAN, MaidModelAsset.DEFAULT.isYsmModel()),
					DataEntry.of(EntityDataSerializers.STRING, MaidModelAsset.DEFAULT.getYsmModelId()),
					DataEntry.of(EntityDataSerializers.STRING, MaidModelAsset.DEFAULT.getYsmModelTexture()),
					DataEntry.of(EntityDataSerializers.COMPONENT, MaidModelAsset.DEFAULT.getYsmModelName()),
					DataEntry.of(EntityDataSerializers.STRING, MaidModelAsset.DEFAULT_YSM_ANIMATION),
					DataEntry.of(EntityDataSerializers.BOOLEAN, false));
		}

		public default void loadAllMaidEntityData(CompoundTag compound, SynchedEntityData entityData) {
			SynchedEntityDataOp.loadString(compound, TAG_TLM_MODEL_ID, entityData, maidEntityDataAccs()[IDX_TLM_MODEL_ID]);
			SynchedEntityDataOp.loadBool(compound, TAG_IS_YSM_MODEL, entityData, maidEntityDataAccs()[IDX_IS_YSM_MODEL]);
			SynchedEntityDataOp.loadString(compound, TAG_YSM_MODEL_ID, entityData, maidEntityDataAccs()[IDX_YSM_MODEL_ID]);
			SynchedEntityDataOp.loadString(compound, TAG_YSM_MODEL_TEXTURE, entityData, maidEntityDataAccs()[IDX_YSM_MODEL_TEXTURE]);
			SynchedEntityDataOp.loadComponent(compound, TAG_YSM_MODEL_NAME, entityData, maidEntityDataAccs()[IDX_YSM_MODEL_NAME]);
			SynchedEntityDataOp.loadString(compound, TAG_YSM_ANIMATION, entityData, maidEntityDataAccs()[IDX_YSM_ANIMATION]);
			SynchedEntityDataOp.loadBool(compound, TAG_YSM_ANIMATION_PLAYING, entityData, maidEntityDataAccs()[IDX_YSM_ANIMATION_PLAYING]);
		}

		public default void storeAllMaidEntityData(CompoundTag compound, SynchedEntityData entityData) {
			SynchedEntityDataOp.storeString(compound, TAG_TLM_MODEL_ID, entityData, maidEntityDataAccs()[IDX_TLM_MODEL_ID]);
			SynchedEntityDataOp.storeBool(compound, TAG_IS_YSM_MODEL, entityData, maidEntityDataAccs()[IDX_IS_YSM_MODEL]);
			SynchedEntityDataOp.storeString(compound, TAG_YSM_MODEL_ID, entityData, maidEntityDataAccs()[IDX_YSM_MODEL_ID]);
			SynchedEntityDataOp.storeString(compound, TAG_YSM_MODEL_TEXTURE, entityData, maidEntityDataAccs()[IDX_YSM_MODEL_TEXTURE]);
			SynchedEntityDataOp.storeComponent(compound, TAG_YSM_MODEL_NAME, entityData, maidEntityDataAccs()[IDX_YSM_MODEL_NAME]);
			SynchedEntityDataOp.storeString(compound, TAG_YSM_ANIMATION, entityData, maidEntityDataAccs()[IDX_YSM_ANIMATION]);
			SynchedEntityDataOp.storeBool(compound, TAG_YSM_ANIMATION_PLAYING, entityData, maidEntityDataAccs()[IDX_YSM_ANIMATION_PLAYING]);
		}

		/**
		 * 高频调用，必须返回defineAllEntityData()得到的static数组
		 * 
		 * @return
		 */
		@SuppressWarnings("rawtypes")
		public abstract EntityDataAccessor[] maidEntityDataAccs();

		@Override
		public default String getTlmModelId() {
			return (String) entityData().get(maidEntityDataAccs()[IDX_TLM_MODEL_ID]);
		}

		/**
		 * 设置TLM模型ID
		 * 
		 * @param modelId 模型ID
		 */
		@Override
		public default void setTlmModelId(String modelId) {
			entityData().set(maidEntityDataAccs()[IDX_TLM_MODEL_ID], modelId);
		}

		@Override
		public default boolean isYsmModel() {
			return (boolean) entityData().get(maidEntityDataAccs()[IDX_IS_YSM_MODEL]);
		}

		@Override
		public default void setIsYsmModel(boolean isYsmModel) {
			entityData().set(maidEntityDataAccs()[IDX_IS_YSM_MODEL], isYsmModel);
		}

		@Override
		public default String getYsmModelId() {
			return (String) entityData().get(maidEntityDataAccs()[IDX_YSM_MODEL_ID]);
		}

		@Override
		public default void setYsmModelId(String ysmModelId) {
			entityData().set(maidEntityDataAccs()[IDX_YSM_MODEL_ID], ysmModelId);
		}

		@Override
		public default String getYsmModelTexture() {
			return (String) entityData().get(maidEntityDataAccs()[IDX_YSM_MODEL_TEXTURE]);
		}

		@Override
		public default void setYsmModelTexture(String ysmModelTexture) {
			entityData().set(maidEntityDataAccs()[IDX_YSM_MODEL_TEXTURE], ysmModelTexture);
		}

		@Override
		public default Component getYsmModelName() {
			return (Component) entityData().get(maidEntityDataAccs()[IDX_YSM_MODEL_NAME]);
		}

		@Override
		public default void setYsmModelName(Component ysmModelName) {
			entityData().set(maidEntityDataAccs()[IDX_YSM_MODEL_NAME], ysmModelName);
		}

		@Override
		public default MaidModelAsset modelAsset() {
			return new MaidModelAsset(this.getTlmModelId(), this.isYsmModel(), this.getYsmModelId(), this.getYsmModelTexture(), this.getYsmModelName());
		}

		@Override
		public default String getYsmAnimation() {
			return (String) entityData().get(maidEntityDataAccs()[IDX_YSM_ANIMATION]);
		}

		@Override
		public default boolean isYsmAnimationPlaying() {
			return (boolean) entityData().get(maidEntityDataAccs()[IDX_YSM_ANIMATION_PLAYING]);
		}

		@Override
		public default void playRouletteAnim(String rouletteAnim) {
			entityData().set(maidEntityDataAccs()[IDX_YSM_ANIMATION], rouletteAnim);
			entityData().set(maidEntityDataAccs()[IDX_YSM_ANIMATION_PLAYING], true);
		}

		@Override
		public default void stopRouletteAnim() {
			entityData().set(maidEntityDataAccs()[IDX_YSM_ANIMATION_PLAYING], false);
		}
	}

	/**
	 * 播放动画。<br>
	 * 目前可播放动画见MaidAnimationMessage
	 * 
	 * @param animationId
	 */
	public default void playAnimation(int animationId) {
		EntityMaid renderingEntity = renderingEntity();
		renderingEntity.animationId = animationId;
		renderingEntity.animationRecordTime = System.currentTimeMillis();
		renderingEntity.shouldReset = true;
	}
}
