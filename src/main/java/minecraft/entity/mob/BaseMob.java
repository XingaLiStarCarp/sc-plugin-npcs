package minecraft.entity.mob;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import minecraft.component.OpProvider;
import minecraft.component.TraitProvider;
import minecraft.core.Core;
import minecraft.core.registry.RegistryMap;
import minecraft.entity.EntityInteractions;
import minecraft.entity.EntityRendererType;
import minecraft.entity.data.EntityDefaultAttributes;
import minecraft.entity.data.SynchedEntityDataOp;
import minecraft.item.Items;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import sys.jvm.symbols;

public abstract class BaseMob extends PathfinderMob implements TraitProvider, OpProvider {
	public static final RegistryMap<EntityType<?>> ENTITIES = (RegistryMap<EntityType<?>>) RegistryMap.of(Registries.ENTITY_TYPE);

	private final EntityRendererType<?> rendererType;

	/**
	 * 实体所属性别
	 * 
	 * @return
	 */
	public final EntityRendererType<?> rendererType() {
		return rendererType;
	}

	public final Object defaultRenderAsset() {
		return rendererType.defaultAsset();
	}

	@SuppressWarnings("unchecked")
	public final <_T> _T defaultRenderAsset(Class<_T> clazz) {
		return (_T) rendererType.defaultAsset();
	}

	/**
	 * 注册一种新的生物实体类型。<br>
	 * 不同的生物实体类型具有不同的行为。<br>
	 * 
	 * @param <T>
	 * @param entityClazz
	 * @param rendererType
	 * @param typeName     带命名空间的类型名称
	 * @param category
	 * @param attributes   生物实体的默认属性，属性必须至少包含MAX_HEALTH和FOLLOW_TDNGE
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final <T extends BaseMob> DeferredHolder<EntityType<?>, EntityType<T>> newType(Class<T> entityClazz, float width, float height, EntityRendererType<?> rendererType, String typeName, MobCategory category, Function<EntityType<?>, AttributeSupplier> attributes) {
		DeferredHolder<EntityType<?>, EntityType<BaseMob>> type = ENTITIES.register(typeName,
				() -> EntityType.Builder.of((EntityType<BaseMob> entityType, Level level) -> {
					try {
						MethodHandle constructor = symbols.find_constructor(entityClazz, EntityType.class, EntityRendererType.class, Level.class);
						return (T) constructor.invoke(entityType, rendererType, level);
					} catch (Throwable ex) {
						Core.logError("create base mob of type '{}' failed: {}", typeName, Core.throwableString(ex));
						return null;
					}
				}, category)
						.sized(width, height)
						.build(typeName));
		rendererType.apply(type);// 将实体类型添加到指定渲染类型
		EntityDefaultAttributes.set(type, attributes);// 设置默认的生物实体属性
		return (DeferredHolder<EntityType<?>, EntityType<T>>) (DeferredHolder) type;
	}

	public static final <T extends BaseMob> DeferredHolder<EntityType<?>, EntityType<T>> newType(Class<T> entityClazz, float width, float height, EntityRendererType<?> rendererType, String typeName, Function<EntityType<?>, AttributeSupplier> attributes) {
		return newType(entityClazz, width, height, rendererType, typeName, MobCategory.MISC, attributes);
	}

	public static final <T extends BaseMob> DeferredHolder<EntityType<?>, EntityType<T>> newType(Class<T> entityClazz, float width, float height, EntityRendererType<?> rendererType, String typeName, MobCategory category, Supplier<AttributeSupplier> attributes) {
		return newType(entityClazz, width, height, rendererType, typeName, category, (t) -> attributes.get());
	}

	public static final <T extends BaseMob> DeferredHolder<EntityType<?>, EntityType<T>> newType(Class<T> entityClazz, float width, float height, EntityRendererType<?> rendererType, String typeName, Supplier<AttributeSupplier> attributes) {
		return newType(entityClazz, width, height, rendererType, typeName, MobCategory.MISC, attributes);
	}

	private boolean updateSwing = true;
	private boolean pushable = true;
	private boolean attackable = true;
	private double rmDistance = -1;

	public BaseMob(EntityType<BaseMob> entityType, EntityRendererType<?> renderType, Level level) {
		super(entityType, level);
		this.rendererType = renderType;
	}

	public final boolean isClientSide() {
		return level().isClientSide();
	}

	@Override
	public void tick() {
		this.tickTraits();// 更新特性的执行动作
		super.tick();
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (updateSwing)
			this.updateSwingTime();// 使用swing()后需要手动更新swing剩余时间
	}

	@Override
	protected void hurtCurrentlyUsedShield(float damage) {
		// 默认使用盾牌格挡时只播放音效，不减少盾牌耐久
		this.handleEntityEvent(EntityEvent.ATTACK_BLOCKED);
	}

	/**
	 * 破盾。<br>
	 * 
	 * @param forceDisable 是否强制破盾
	 */
	public void disableShield(boolean forceDisable) {
		EntityInteractions.disableShield(this, forceDisable);
	}

	@Override
	protected void blockUsingShield(LivingEntity damager) {
		super.blockUsingShield(damager);// 被击退
		// 判断攻击者的主手武器是否能破盾
		if (damager.getMainHandItem().canDisableShield(this.useItem, this, damager)) {
			this.disableShield(true);
		}
	}

	/**
	 * 定义并初始化实体数据字段默认值
	 */
	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		SynchedEntityDataOp.defineAll(this.getClass(), builder);
	}

	/**
	 * 从entityData中读取数据并序列化存入compound
	 * 
	 * @param compound
	 * @param entityData
	 */
	protected void storeData(CompoundTag compound, SynchedEntityData entityData) {

	}

	/**
	 * 序列化实体的数据
	 */
	@Override
	public final void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		this.storeData(compound, super.entityData);
	}

	protected void loadData(CompoundTag compound, SynchedEntityData entityData) {

	}

	/**
	 * 反序列化实体的数据
	 */
	@Override
	public final void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		this.loadData(compound, super.entityData);
	}

	public void setUpdateSwing(boolean updateSwing) {
		this.updateSwing = updateSwing;
	}

	@Override
	public boolean isPushable() {
		return pushable;
	}

	/**
	 * 设置生物实体是否可以被推动
	 * 
	 * @param pushable
	 * @return
	 */
	public void setPushable(boolean pushable) {
		this.pushable = pushable;
	}

	/**
	 * 设置生物实体远离消失的阈值距离。<br>
	 * 若小于等于0则永远不移除.<br>
	 * 
	 * @param distance
	 * @return
	 */
	public void setRemoveDistance(double distance) {
		this.rmDistance = distance;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return rmDistance > 0 ? (distanceToClosestPlayer > rmDistance) : false;
	}

	@Override
	public boolean attackable() {
		return attackable;
	}

	/**
	 * 设置实体是否可以被攻击
	 * 
	 * @param attackable
	 * @return
	 */
	public void setAttackable(boolean attackable) {
		this.attackable = attackable;
	}

	/**
	 * 让生物实体朝向指定的方向
	 * 
	 * @param loc
	 * @return
	 */
	public void face(Vec3 loc) {
		this.lookAt(Anchor.EYES, loc);
	}

	/**
	 * 让生物实体朝向指定的实体
	 * 
	 * @param entity
	 * @return
	 */
	public void face(Entity entity) {
		face(entity.getEyePosition());
	}

	/**
	 * 设置手持物品，该设置会替换生物实体当前手持物品
	 * 
	 * @param item
	 * @return
	 */
	public void setMainHandHold(ItemStack item) {
		this.setItemInHand(InteractionHand.MAIN_HAND, item);
	}

	public void setMainHandHold(Item item) {
		this.setMainHandHold(new ItemStack(item, 1));
	}

	public void setMainHandHold(String item) {
		this.setMainHandHold(Items.item(item));
	}

	public void setOffHandHold(ItemStack item) {
		this.setItemInHand(InteractionHand.OFF_HAND, item);
	}

	public void setOffHandHold(Item item) {
		this.setOffHandHold(new ItemStack(item, 1));
	}

	public void setOffHandHold(String item) {
		this.setOffHandHold(Items.item(item));
	}

	/**
	 * 单独向一个玩家发送消息
	 * 
	 * @param player
	 * @param msg
	 * @return
	 */
	public void chat(Player player, String msg) {
		Component name = this.getCustomName();
		if (name == null) {
			// 生物实体如果没有名字则省去名字前缀，直接发送消息
			player.sendSystemMessage(Component.literal(msg));
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append('<');
			sb.append(name.getString());
			sb.append("> ");
			sb.append(msg);
			player.sendSystemMessage(Component.literal(sb.toString()));
		}
	}

	private final ArrayList<TraitComponent<?>> traitComponents = new ArrayList<>();

	@Override
	public final ArrayList<TraitComponent<?>> getTraits() {
		return traitComponents;
	}

	private final HashMap<Class<?>, OpComponent<?, ?>> opComponents = new HashMap<>();

	@Override
	public final HashMap<Class<?>, OpComponent<?, ?>> getOpComponents() {
		return opComponents;
	}

	/**
	 * 暂停实体的行为
	 */
	public void pause() {
		EntityInteractions.pause(this);
	}

	public void fakeDie() {
		this.setPose(Pose.SLEEPING);// 设置为睡姿
		this.pause();
	}
}
