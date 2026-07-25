package minecraft.extended.tlm.entity.maid;

import java.util.function.Supplier;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

import minecraft.entity.EntityRendererType;
import minecraft.entity.mob.BaseMob;
import minecraft.extended.tlm.entity.maid.ProxyRenderMaid.ProxyRenderMaidEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

/**
 * Touhou Little Maid模组的女仆类型实体，需要配合渲染器EntityMaidRenderer使用
 */
public class MaidMob extends BaseMob implements ProxyRenderMaidEntity {

	public static final EntityRendererType<MaidModelAsset> RENDERER_TYPE = new EntityRendererType<>();

	public static final <T extends BaseMob> RegistryObject<EntityType<T>> newType(Class<T> entityClazz, float width, float height, String typeName, MobCategory category, Supplier<AttributeSupplier> attributes) {
		return BaseMob.newType(entityClazz, width, height, RENDERER_TYPE, typeName, category, attributes);
	}

	public static final <T extends BaseMob> RegistryObject<EntityType<T>> newType(Class<T> entityClazz, float width, float height, String typeName, Supplier<AttributeSupplier> attributes) {
		return newType(entityClazz, width, height, typeName, MobCategory.MISC, attributes);
	}

	static {
		RENDERER_TYPE.setDefaultRenderAsset(MaidModelAsset.DEFAULT);
	}

	private static final EntityDataAccessor<?>[] ACCS = ProxyRenderMaidEntity.defineAllMaidEntityData(MaidMob.class);

	@Override
	public EntityDataAccessor<?>[] maidEntityDataAccs() {
		return ACCS;
	}

	protected final EntityMaid renderingEntity;

	/**
	 * 虚假女仆实体，不实际存在于游戏中，仅仅用于模型渲染
	 * 
	 * @return
	 */
	@Override
	public final EntityMaid renderingEntity() {
		return renderingEntity;
	}

	@Override
	public final Entity bindEntity() {
		return this;
	}

	public MaidMob(EntityType<BaseMob> entityType, EntityRendererType<MaidModelAsset> renderType, Level level) {
		super(entityType, renderType, level);
		renderingEntity = this.blankRenderingEntity(this);
	}

	@Override
	protected void loadData(CompoundTag compound, SynchedEntityData entityData) {
		this.loadAllMaidEntityData(compound, entityData);
	}

	@Override
	protected void storeData(CompoundTag compound, SynchedEntityData entityData) {
		this.storeAllMaidEntityData(compound, entityData);
	}

	@Override
	public void tick() {
		super.tick();
		this.syncRenderingEntity();
	}

	@Override
	public void rideTick() {
		super.rideTick();
		this.tickMaidRide();
	}

	@Override
	public boolean isSwingingArms() {
		return this.swinging;
	}
}
