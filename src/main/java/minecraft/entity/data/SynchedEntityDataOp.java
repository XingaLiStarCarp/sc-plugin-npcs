package minecraft.entity.data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javabase.HashKey;
import minecraft.core.registry.MappedRegistryAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import sys.jvm.reflection;
import sys.jvm.unsafe;

/**
 * SynchedEntityData的Unsafe操作。<br>
 * 错误的操作将导致实体数据损坏！<br>
 * 1.21.1下，Entity类构造函数中调用defineSynchedData()并传入Builder。<br>
 * 所有派生类均在覆写的defineSynchedData()中将需要同步的字段进行定义，待该函数定义完成后，才会将Builder对象构造为实际的SynchedEntityData对象。<br>
 * 1.20.1的SynchedEntityData由于支持动态修改，有锁，在1.21.1已经去除锁，只允许在defineSynchedData()中修改。<br>
 */
@SuppressWarnings("unchecked")
public class SynchedEntityDataOp {
	private static Field SynchedEntityData_entity;
	private static Field SynchedEntityData_itemsById;
	private static Field SynchedEntityData_isDirty;

	private static Field SynchedEntityData_Builder_itemsById;

	static {
		SynchedEntityData_entity = reflection.find_declared_field(SynchedEntityData.class, "entity");
		SynchedEntityData_itemsById = reflection.find_declared_field(SynchedEntityData.class, "itemsById");
		SynchedEntityData_isDirty = reflection.find_declared_field(SynchedEntityData.class, "isDirty");
		SynchedEntityData_Builder_itemsById = reflection.find_declared_field(SynchedEntityData.Builder.class, "itemsById");
	}

	/**
	 * 获取Builder的底层定义集合
	 * 
	 * @param builder
	 * @return
	 */
	public static final SynchedEntityData.DataItem<?>[] getItems(SynchedEntityData.Builder builder) {
		return (SynchedEntityData.DataItem<?>[]) unsafe.read_reference(builder, SynchedEntityData_Builder_itemsById);
	}

	/**
	 * 获取SynchedEntityData内部的所有属性Map
	 * 
	 * @param entityData
	 * @return
	 */
	public static final SynchedEntityData.DataItem<?>[] getItems(SynchedEntityData entityData) {
		return (SynchedEntityData.DataItem<?>[]) unsafe.read_reference(entityData, SynchedEntityData_itemsById);
	}

	/**
	 * 不加锁直接获取DataItem
	 * 
	 * @param entityData
	 * @param acc
	 * @return
	 */
	public static final SynchedEntityData.DataItem<?> getItem(SynchedEntityData entityData, EntityDataAccessor<?> acc) {
		return getItems(entityData)[acc.id()];
	}

	public static final Entity getEntity(SynchedEntityData entityData) {
		return (Entity) unsafe.read_reference(entityData, SynchedEntityData_entity);
	}

	public static final void setEntity(SynchedEntityData entityData, Entity entity) {
		unsafe.write(entityData, SynchedEntityData_entity, entity);
	}

	/**
	 * 设置脏标记
	 * 
	 * @param entityData
	 * @param isDirty
	 */
	public static final void setDirty(SynchedEntityData entityData, boolean isDirty) {
		unsafe.write(entityData, SynchedEntityData_isDirty, isDirty);
	}

	/**
	 * 将数据从srcData拷贝到destData中。<br>
	 * 仅拷贝两者共有的数据（EntityDataAccessor对象相同的数据），否则直接略过。<br>
	 * 
	 * @param srcData
	 * @param destData
	 */
	@SuppressWarnings("rawtypes")
	public static final void copyData(SynchedEntityData srcData, SynchedEntityData destData) {
		Entity destEntity = getEntity(destData);
		SynchedEntityData.DataItem<?>[] srcItems = getItems(srcData);
		SynchedEntityData.DataItem<?>[] destItems = getItems(destData);
		for (SynchedEntityData.DataItem destItem : destItems) {
			EntityDataAccessor<?> acc = destItem.getAccessor();
			int id = acc.id();
			// 判断srcData中是否存在相同ID的数据项
			// 必须要EntityDataAccessor引用完全相同才能断定是同一项数据，否则不同实体的各自独有数据项id有可能会撞
			if (id < srcItems.length) { // 考虑到两个集合不是包含关系，有可能遇到destItems有而srcData没有的数据，则直接略过
				SynchedEntityData.DataItem<?> srcItem = srcItems[id];// 有可能不存在
				if (srcItem != null && srcItem.getAccessor() == acc) {
					destItem.setValue(srcItem.getValue());
					destEntity.onSyncedDataUpdated(acc);// 通知实体数据有更新
					destItem.setDirty(true);
				}
			}
		}
		setDirty(destData, true);// 为目标标记需要更新
	}

	/**
	 * 安全地初始化字段。如果字段已经初始化则忽略该操作。
	 * 
	 * @param <T>
	 * @param builder
	 * @param accessor
	 * @param value
	 */
	public static <T> void define(SynchedEntityData.Builder builder, EntityDataAccessor<T> accessor, T value) {
		if (getItems(builder)[accessor.id()] != null) {
			return;// 如果已经初始化，则不再重复初始化
		} else {
			builder.define(accessor, value);
		}
	}

	private static final HashMap<Class<? extends Entity>, ArrayList<EntityDataAccessor<?>>> DATA_ENTRIES = new HashMap<>();

	/**
	 * 不同EntityDataAccessor分别对应的默认值。<br>
	 * EntityDataAccessor的id是字段索引，且该索引从0开始包含全部父类的字段，最大为254。<br>
	 * EntityDataAccessor的hashCode()为id，且equals()也是判断id是否相等，因此两个id相同的EntityDataAccessor作为key在HashMap中对应同一个value。<br>
	 */
	private static final HashMap<HashKey<EntityDataAccessor<?>>, Object> DEFAULT_VALUES = new HashMap<>();

	/**
	 * 获取指定实体类定义的数据
	 * 
	 * @param entityClazz
	 * @return
	 */
	public static ArrayList<EntityDataAccessor<?>> dataEntries(Class<? extends Entity> entityClazz) {
		return DATA_ENTRIES.computeIfAbsent(entityClazz, (k) -> new ArrayList<>());
	}

	public static void setDefaultValue(EntityDataAccessor<?> acc, Object value) {
		DEFAULT_VALUES.put(HashKey.of(acc), value);
	}

	public static Object getDefaultValue(EntityDataAccessor<?> acc) {
		return DEFAULT_VALUES.get(HashKey.of(acc));
	}

	/**
	 * 为指定类型的实体创建一个新字段
	 * 
	 * @param <_T>
	 * @param entityClazz  实体类
	 * @param dataType     字段数据类型
	 * @param defaultValue 字段默认值
	 * @return
	 */
	public static <_T> EntityDataAccessor<_T> define(Class<? extends Entity> entityClazz, EntityDataSerializer<_T> dataType, _T defaultValue) {
		EntityDataAccessor<_T> acc = SynchedEntityData.defineId(entityClazz, dataType);
		dataEntries(entityClazz).add(acc);
		setDefaultValue(acc, Objects.requireNonNull(defaultValue));
		return acc;
	}

	public static class DataEntry<_T> {
		public final EntityDataSerializer<_T> dataType;
		public final _T defaultValue;

		private DataEntry(EntityDataSerializer<_T> dataType, _T defaultValue) {
			this.dataType = dataType;
			this.defaultValue = defaultValue;
		}

		public static final <_T> DataEntry<_T> of(EntityDataSerializer<_T> dataType, _T defaultValue) {
			return new DataEntry<>(dataType, defaultValue);
		}
	}

	/**
	 * 一次性按顺序为实体定义数据
	 * 
	 * @param entityClazz
	 * @param entries
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static EntityDataAccessor<?>[] define(Class<? extends Entity> entityClazz, DataEntry... entries) {
		EntityDataAccessor<?>[] accs = new EntityDataAccessor[entries.length];
		for (int idx = 0; idx < entries.length; ++idx) {
			DataEntry entry = entries[idx];
			accs[idx] = define(entityClazz, entry.dataType, entry.defaultValue);
		}
		return accs;
	}

	/**
	 * 指定的实体类是否有字段。<br>
	 * 只有通过本类的define()方法加入的字段才能识别。<br>
	 * 
	 * @param entityClazz
	 * @return
	 */
	public static boolean hasEntries(Class<? extends Entity> entityClazz) {
		return DATA_ENTRIES.containsKey(entityClazz);
	}

	@SuppressWarnings({ "rawtypes" })
	public static void reset(Class<? extends Entity> entityClazz, SynchedEntityData entityData) {
		ArrayList<EntityDataAccessor<?>> entries = dataEntries(entityClazz);
		for (EntityDataAccessor<?> acc : entries) {
			entityData.set((EntityDataAccessor) acc, getDefaultValue(acc));
		}
	}

	public static void reset(Entity entity) {
		reset(entity.getClass(), entity.getEntityData());
	}

	/**
	 * 运行时初始化实体数据。<br>
	 * 仅初始化实体自身类的数据，父类的数据不会初始化。<br>
	 * 
	 * @param entityClazz
	 * @param entityData
	 */
	@SuppressWarnings({ "rawtypes" })
	public static void define(Class<? extends Entity> entityClazz, SynchedEntityData.Builder builder) {
		if (hasEntries(entityClazz)) {
			ArrayList<EntityDataAccessor<?>> entries = dataEntries(entityClazz);
			for (EntityDataAccessor<?> acc : entries) {
				define(builder, (EntityDataAccessor) acc, getDefaultValue(acc));
			}
		}
	}

	/**
	 * 递归地初始化实体类自身及其父类的字段。
	 * 
	 * @param entityClazz
	 * @param builder
	 */
	public static void defineAll(Class<? extends Entity> entityClazz, SynchedEntityData.Builder builder) {
		while (entityClazz != Entity.class) {
			define(entityClazz, builder);
			entityClazz = (Class<? extends Entity>) entityClazz.getSuperclass();
		}
	}

	/**
	 * 判断一个字符串属性是否是有效值
	 * 
	 * @param str
	 * @return
	 */
	public static boolean validate(String str) {
		return str != null && !"".equals(str);
	}

	public static String string(Component component) {
		return Component.Serializer.toJson(component, MappedRegistryAccess.serverRegistryAccess);
	}

	public static MutableComponent component(String component) {
		return component == null ? Component.empty() : Component.Serializer.fromJson(component, MappedRegistryAccess.serverRegistryAccess);
	}

	public static String string(ResourceLocation loc) {
		return loc.toString();
	}

	public static ResourceLocation resourceLocation(String loc) {
		return ResourceLocation.parse(loc);
	}

	public static void loadString(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<String> accessor) {
		String data = null;
		if (!compound.contains(tag, Tag.TAG_STRING)) {
			data = (String) getDefaultValue(accessor);
		} else {
			data = compound.getString(tag);
		}
		entityData.set(accessor, data);
	}

	/**
	 * 加载序列化后的对象到entityData，并返回反序列化后的对象。<br>
	 * 
	 * @param <_T>
	 * @param compound
	 * @param tag
	 * @param entityData
	 * @param accessor
	 * @param serializer
	 * @param deserializer
	 * @return
	 */
	public static <_T> _T loadObject(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<String> accessor, Function<_T, String> serializer, Function<String, _T> deserializer) {
		_T obj = null;
		String data = null;// obj序列化后的字符串

		if (!compound.contains(tag, Tag.TAG_STRING)) {
			obj = (_T) getDefaultValue(accessor);
			data = serializer.apply(obj);
		} else {
			data = compound.getString(tag);
			obj = deserializer.apply(data);
		}
		entityData.set(accessor, data);
		return obj;
	}

	public static void loadComponent(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Component> accessor) {
		Component data = null;
		if (!compound.contains(tag, Tag.TAG_STRING)) {
			data = (Component) getDefaultValue(accessor);
		} else {
			data = component(compound.getString(tag));
		}
		entityData.set(accessor, data);
	}

	public static void loadBool(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Boolean> accessor) {
		boolean data = false;
		if (!compound.contains(tag, Tag.TAG_BYTE)) {
			data = (boolean) getDefaultValue(accessor);
		} else {
			data = compound.getBoolean(tag);
		}
		entityData.set(accessor, data);
	}

	public static void loadByte(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Byte> accessor) {
		byte data = 0;
		if (!compound.contains(tag, Tag.TAG_BYTE)) {
			data = (byte) getDefaultValue(accessor);
		} else {
			data = compound.getByte(tag);
		}
		entityData.set(accessor, data);
	}

	public static void loadShort(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Short> accessor) {
		short data = 0;
		if (!compound.contains(tag, Tag.TAG_SHORT)) {
			data = (short) getDefaultValue(accessor);
		} else {
			data = compound.getShort(tag);
		}
		entityData.set(accessor, data);
	}

	public static void loadInt(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Integer> accessor) {
		int data = 0;
		if (!compound.contains(tag, Tag.TAG_INT)) {
			data = (int) getDefaultValue(accessor);
		} else {
			data = compound.getInt(tag);
		}
		entityData.set(accessor, data);
	}

	public static void loadLong(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Long> accessor) {
		long data = 0;
		if (!compound.contains(tag, Tag.TAG_LONG)) {
			data = (long) getDefaultValue(accessor);
		} else {
			data = compound.getLong(tag);
		}
		entityData.set(accessor, data);
	}

	public static void loadFloat(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Float> accessor) {
		float data = 0;
		if (!compound.contains(tag, Tag.TAG_FLOAT)) {
			data = (int) getDefaultValue(accessor);
		} else {
			data = compound.getFloat(tag);
		}
		entityData.set(accessor, data);
	}

	public static void loadDouble(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Double> accessor) {
		double data = 0;
		if (!compound.contains(tag, Tag.TAG_DOUBLE)) {
			data = (double) getDefaultValue(accessor);
		} else {
			data = compound.getDouble(tag);
		}
		entityData.set(accessor, data);
	}

	public static void storeString(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<String> accessor) {
		String data = entityData.get(accessor);
		if (data == null) {
			data = (String) getDefaultValue(accessor);
		}
		compound.putString(tag, data);
	}

	public static void storeComponent(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Component> accessor) {
		Component data = entityData.get(accessor);
		if (data == null) {
			data = (Component) getDefaultValue(accessor);
		}
		compound.putString(tag, string(data));
	}

	public static void storeBool(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Boolean> accessor) {
		Boolean data = entityData.get(accessor);
		if (data == null) {
			data = (Boolean) getDefaultValue(accessor);
		}
		compound.putByte(tag, data ? (byte) 1 : (byte) 0);
	}

	public static void storeByte(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Byte> accessor) {
		Byte data = entityData.get(accessor);
		if (data == null) {
			data = (Byte) getDefaultValue(accessor);
		}
		compound.putByte(tag, data);
	}

	public static void storeShort(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Short> accessor) {
		Short data = entityData.get(accessor);
		if (data == null) {
			data = (Short) getDefaultValue(accessor);
		}
		compound.putShort(tag, data);
	}

	public static void storeInt(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Integer> accessor) {
		Integer data = entityData.get(accessor);
		if (data == null) {
			data = (Integer) getDefaultValue(accessor);
		}
		compound.putInt(tag, data);
	}

	public static void storeLong(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Long> accessor) {
		Long data = entityData.get(accessor);
		if (data == null) {
			data = (Long) getDefaultValue(accessor);
		}
		compound.putLong(tag, data);
	}

	public static void storeFloat(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Float> accessor) {
		Float data = entityData.get(accessor);
		if (data == null) {
			data = (Float) getDefaultValue(accessor);
		}
		compound.putFloat(tag, data);
	}

	public static void storeDouble(CompoundTag compound, String tag, SynchedEntityData entityData, EntityDataAccessor<Double> accessor) {
		Double data = entityData.get(accessor);
		if (data == null) {
			data = (Double) getDefaultValue(accessor);
		}
		compound.putDouble(tag, data);
	}

	// Entity所属
	public static final EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID;
	public static final EntityDataAccessor<Integer> DATA_AIR_SUPPLY_ID;
	public static final EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME;
	public static final EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE;
	public static final EntityDataAccessor<Boolean> DATA_SILENT;
	public static final EntityDataAccessor<Boolean> DATA_NO_GRAVITY;
	public static final EntityDataAccessor<Pose> DATA_POSE;
	public static final EntityDataAccessor<Integer> DATA_TICKS_FROZEN;

	static {
		unsafe.ensure_class_initialized(Entity.class);
		DATA_SHARED_FLAGS_ID = (EntityDataAccessor<Byte>) unsafe.read_static_reference(Entity.class, "DATA_SHARED_FLAGS_ID");
		DATA_AIR_SUPPLY_ID = (EntityDataAccessor<Integer>) unsafe.read_static_reference(Entity.class, "DATA_AIR_SUPPLY_ID");
		DATA_CUSTOM_NAME = (EntityDataAccessor<Optional<Component>>) unsafe.read_static_reference(Entity.class, "DATA_CUSTOM_NAME");
		DATA_CUSTOM_NAME_VISIBLE = (EntityDataAccessor<Boolean>) unsafe.read_static_reference(Entity.class, "DATA_CUSTOM_NAME_VISIBLE");
		DATA_SILENT = (EntityDataAccessor<Boolean>) unsafe.read_static_reference(Entity.class, "DATA_SILENT");
		DATA_NO_GRAVITY = (EntityDataAccessor<Boolean>) unsafe.read_static_reference(Entity.class, "DATA_NO_GRAVITY");
		DATA_POSE = (EntityDataAccessor<Pose>) unsafe.read_static_reference(Entity.class, "DATA_POSE");
		DATA_TICKS_FROZEN = (EntityDataAccessor<Integer>) unsafe.read_static_reference(Entity.class, "DATA_TICKS_FROZEN");
	}

	/**
	 * 创建并初始化entity关联的空SynchedEntityData.Builder。<br>
	 * 这些数据字段是Entity类所属的，无法通过defineSynchedData()定义。<br>
	 * 
	 * @param entity
	 * @return
	 */
	public static final SynchedEntityData.Builder newBasicEntityData(Entity entity) {
		SynchedEntityData.Builder builder = new SynchedEntityData.Builder(entity);
		builder.define(DATA_SHARED_FLAGS_ID, (byte) 0);
		builder.define(DATA_AIR_SUPPLY_ID, 300);
		builder.define(DATA_CUSTOM_NAME_VISIBLE, false);
		builder.define(DATA_CUSTOM_NAME, Optional.empty());
		builder.define(DATA_SILENT, false);
		builder.define(DATA_NO_GRAVITY, false);
		builder.define(DATA_POSE, Pose.STANDING);
		builder.define(DATA_TICKS_FROZEN, 0);
		return builder;
	}

	public static final int ENTITY_SHARED_FLAG_INVISIBLE = 5;

	protected boolean getEntitySharedFlag(Entity entity, int flag) {
		return (entity.getEntityData().get(DATA_SHARED_FLAGS_ID) & 1 << flag) != 0;
	}

	protected void setEntitySharedFlag(Entity entity, int flags, boolean value) {
		SynchedEntityData entityData = entity.getEntityData();
		byte original = entityData.get(DATA_SHARED_FLAGS_ID);
		entityData.set(DATA_LIVING_ENTITY_FLAGS, (byte) (value ? (original |= flags) : (original &= ~flags)));
	}

	/**
	 * 设置实体是否可见
	 * 
	 * @param entity
	 * @param invisible
	 */
	public void setEntityInvisible(Entity entity, boolean invisible) {
		setEntitySharedFlag(entity, ENTITY_SHARED_FLAG_INVISIBLE, invisible);
	}

	public static final int LIVING_ENTITY_FLAG_USING_ITEM = 0b00000001;
	public static final int LIVING_ENTITY_FLAG_USING_OFFHAND = 0b00000010;
	public static final int LIVING_ENTITY_FLAG_AUTO_SPIN_ATTACK = 0b00000100;

	// LivingEntity所属
	public static final EntityDataAccessor<Byte> DATA_LIVING_ENTITY_FLAGS;
	public static final EntityDataAccessor<Float> DATA_HEALTH_ID;
	public static final EntityDataAccessor<List<ParticleOptions>> DATA_EFFECT_PARTICLES;
	public static final EntityDataAccessor<Boolean> DATA_EFFECT_AMBIENCE_ID;
	public static final EntityDataAccessor<Integer> DATA_ARROW_COUNT_ID;
	public static final EntityDataAccessor<Integer> DATA_STINGER_COUNT_ID;
	public static final EntityDataAccessor<Optional<BlockPos>> SLEEPING_POS_ID;

	static {
		unsafe.ensure_class_initialized(LivingEntity.class);
		DATA_LIVING_ENTITY_FLAGS = (EntityDataAccessor<Byte>) unsafe.read_static_reference(LivingEntity.class, "DATA_LIVING_ENTITY_FLAGS");
		DATA_HEALTH_ID = (EntityDataAccessor<Float>) unsafe.read_static_reference(LivingEntity.class, "DATA_HEALTH_ID");
		DATA_EFFECT_PARTICLES = (EntityDataAccessor<List<ParticleOptions>>) unsafe.read_static_reference(LivingEntity.class, "DATA_EFFECT_PARTICLES");
		DATA_EFFECT_AMBIENCE_ID = (EntityDataAccessor<Boolean>) unsafe.read_static_reference(LivingEntity.class, "DATA_EFFECT_AMBIENCE_ID");
		DATA_ARROW_COUNT_ID = (EntityDataAccessor<Integer>) unsafe.read_static_reference(LivingEntity.class, "DATA_ARROW_COUNT_ID");
		DATA_STINGER_COUNT_ID = (EntityDataAccessor<Integer>) unsafe.read_static_reference(LivingEntity.class, "DATA_STINGER_COUNT_ID");
		SLEEPING_POS_ID = (EntityDataAccessor<Optional<BlockPos>>) unsafe.read_static_reference(LivingEntity.class, "SLEEPING_POS_ID");
	}

	public static final void setLivingEntityFlag(LivingEntity entity, int flags, boolean value) {
		SynchedEntityData entityData = entity.getEntityData();
		byte original = entityData.get(DATA_LIVING_ENTITY_FLAGS);
		entityData.set(DATA_LIVING_ENTITY_FLAGS, (byte) (value ? (original |= flags) : (original &= ~flags)));
	}

	public static final boolean getLivingEntityFlag(LivingEntity entity, int flag) {
		return (entity.getEntityData().get(DATA_LIVING_ENTITY_FLAGS) & flag) != 0;
	}

	/**
	 * Player所属数据
	 */
	public static final EntityDataAccessor<Float> DATA_PLAYER_ABSORPTION_ID;
	public static final EntityDataAccessor<Integer> DATA_SCORE_ID;
	public static final EntityDataAccessor<Byte> DATA_PLAYER_MODE_CUSTOMISATION;
	public static final EntityDataAccessor<Byte> DATA_PLAYER_MAIN_HAND;
	public static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_LEFT;
	public static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_RIGHT;

	static {
		// 需要先初始化类才能获得其静态字段
		unsafe.ensure_class_initialized(Player.class);
		DATA_PLAYER_ABSORPTION_ID = (EntityDataAccessor<Float>) unsafe.read_static_reference(Player.class, "DATA_PLAYER_ABSORPTION_ID");
		DATA_SCORE_ID = (EntityDataAccessor<Integer>) unsafe.read_static_reference(Player.class, "DATA_SCORE_ID");
		DATA_PLAYER_MODE_CUSTOMISATION = (EntityDataAccessor<Byte>) unsafe.read_static_reference(Player.class, "DATA_PLAYER_MODE_CUSTOMISATION");
		DATA_PLAYER_MAIN_HAND = (EntityDataAccessor<Byte>) unsafe.read_static_reference(Player.class, "DATA_PLAYER_MAIN_HAND");
		DATA_SHOULDER_LEFT = (EntityDataAccessor<CompoundTag>) unsafe.read_static_reference(Player.class, "DATA_SHOULDER_LEFT");
		DATA_SHOULDER_RIGHT = (EntityDataAccessor<CompoundTag>) unsafe.read_static_reference(Player.class, "DATA_SHOULDER_RIGHT");
	}

	public static final int PLAYER_MODEL_PART_CAPE = PlayerModelPart.CAPE.getMask();
	public static final int PLAYER_MODEL_PART_HAT = PlayerModelPart.HAT.getMask();
	public static final int PLAYER_MODEL_PART_JACKET = PlayerModelPart.JACKET.getMask();
	public static final int PLAYER_MODEL_PART_LEFT_PANTS_LEG = PlayerModelPart.LEFT_PANTS_LEG.getMask();
	public static final int PLAYER_MODEL_PART_LEFT_SLEEVE = PlayerModelPart.LEFT_SLEEVE.getMask();
	public static final int PLAYER_MODEL_PART_RIGHT_PANTS_LEG = PlayerModelPart.RIGHT_PANTS_LEG.getMask();
	public static final int PLAYER_MODEL_PART_RIGHT_SLEEVE = PlayerModelPart.RIGHT_SLEEVE.getMask();

	public static final int PLAYER_MODEL_PART_ALL = PLAYER_MODEL_PART_CAPE |
			PLAYER_MODEL_PART_HAT |
			PLAYER_MODEL_PART_JACKET |
			PLAYER_MODEL_PART_LEFT_PANTS_LEG |
			PLAYER_MODEL_PART_LEFT_SLEEVE |
			PLAYER_MODEL_PART_RIGHT_PANTS_LEG |
			PLAYER_MODEL_PART_RIGHT_SLEEVE;

	/**
	 * 设置该玩家在客户端渲染时渲染的组件
	 * 
	 * @param player
	 */
	public static final void showPlayerModelParts(Player player, int parts) {
		player.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) parts);
	}
}
