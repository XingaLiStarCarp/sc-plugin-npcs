package minecraft.entity.data;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 实体的默认实体属性
 */
@SuppressWarnings("rawtypes")
@EventBusSubscriber
public final class EntityDefaultAttributes {

	private static final HashMap<DeferredHolder, Function<EntityType<?>, AttributeSupplier>> DEFEREED_ENTITY_ATTRIBS = new HashMap<>();

	/**
	 * 设置指定实体的默认属性，延迟初始化防止属性本身未注册
	 * 
	 * @param type
	 * @param entries
	 */
	public static final void set(DeferredHolder type, Function<EntityType<?>, AttributeSupplier> entries) {
		DEFEREED_ENTITY_ATTRIBS.put(type, entries);
	}

	public static final void set(DeferredHolder type, Supplier<AttributeSupplier> entries) {
		set(type, (t) -> entries.get());
	}

	/**
	 * 将type的默认属性设置成与target相同
	 * 
	 * @param type
	 * @param target
	 */
	public static final void set(DeferredHolder type, DeferredHolder target) {
		set(type, get(target));
	}

	/**
	 * 获取指定实体的默认属性
	 * 
	 * @param type
	 * @return
	 */
	public static final Function<EntityType<?>, AttributeSupplier> get(DeferredHolder type) {
		return DEFEREED_ENTITY_ATTRIBS.get(type);
	}

	@SubscribeEvent
	@SuppressWarnings("unchecked")
	public static void registerDefaultAttributes(EntityAttributeCreationEvent event) {
		for (Entry<DeferredHolder, Function<EntityType<?>, AttributeSupplier>> entityEntry : DEFEREED_ENTITY_ATTRIBS.entrySet()) {
			EntityType type = (EntityType) entityEntry.getKey().get();
			event.put(type, entityEntry.getValue().apply(type));
		}
	}
}