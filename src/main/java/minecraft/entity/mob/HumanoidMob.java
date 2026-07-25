package minecraft.entity.mob;

import java.util.function.Supplier;

import minecraft.entity.EntityRendererType;
import minecraft.entity.Humanoid.HumanoidEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

/**
 * 原版玩家模型实体
 */
public class HumanoidMob extends BaseMob implements HumanoidEntity {

	public static final EntityRendererType<PlayerModelAsset> RENDERER_TYPE = new EntityRendererType<>();

	public static final <T extends BaseMob> RegistryObject<EntityType<T>> newType(Class<T> entityClazz, float width, float height, String typeName, MobCategory category, Supplier<AttributeSupplier> attributes) {
		return BaseMob.newType(entityClazz, width, height, RENDERER_TYPE, typeName, category, attributes);
	}

	public static final <T extends BaseMob> RegistryObject<EntityType<T>> newType(Class<T> entityClazz, float width, float height, String typeName, Supplier<AttributeSupplier> attributes) {
		return newType(entityClazz, width, height, typeName, MobCategory.MISC, attributes);
	}

	static {
		RENDERER_TYPE.setDefaultRenderAsset(PlayerModelAsset.DEFAULT);
	}

	private static final EntityDataAccessor<?>[] ACCS = HumanoidEntity.defineAllHumanoidEntityData(HumanoidMob.class);

	@Override
	@SuppressWarnings("rawtypes")
	public EntityDataAccessor[] humanoidEntityDataAccs() {
		return ACCS;
	}

	public HumanoidMob(EntityType<BaseMob> entityType, EntityRendererType<ResourceLocation> renderType, Level level) {
		super(entityType, renderType, level);
	}

	private ResourceLocation skin = RENDERER_TYPE.defaultAsset().getSkin();

	/**
	 * 获取皮肤
	 * 
	 * @return
	 */
	@Override
	public final ResourceLocation getSkin() {
		return skin;
	}

	@Override
	public void updateSkin(String skinId) {
		this.skin = ResourceLocation.parse(skinId);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> acc) {
		super.onSyncedDataUpdated(acc);
		this.onHumanoidEntitySyncedDataUpdated(acc);
	}

	@Override
	protected void storeData(CompoundTag compound, SynchedEntityData entityData) {
		this.storeAllHumanoidEntityData(compound, entityData);
	}

	@Override
	protected void loadData(CompoundTag compound, SynchedEntityData entityData) {
		this.loadAllHumanoidEntityData(compound, entityData);
	}

	@Override
	public SynchedEntityData entityData() {
		return entityData;
	}
}
