package minecraft.entity.data;

import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;

import cpw.mods.modlauncher.api.INameMappingService;
import jvmsp.symbols;
import minecraft.capability.CapabilityData;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import scba.ModEntry;

/**
 * 仅用作数据容器的虚假实体
 */
public class BlankEntity {
	private static MethodHandle Entity_defineSynchedData;

	static {
		Entity_defineSynchedData = symbols.find_virtual_method(Entity.class, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.METHOD, "m_8097_"), void.class);
	}

	/**
	 * 创建一个具有该实体种类默认entityData的空实体。<br>
	 * 虚假实体不实际存在于游戏，因此它也无法自动更新，无法自动接收处理Packet和SynchedEntityData。
	 * 
	 * @param <_T>
	 * @param entityClazz
	 * @return
	 */
	public static final <_T extends Entity> _T allocate(Class<_T> entityClazz) {
		_T entity = CapabilityData.construct(entityClazz, Entity.class);// 调用CapabilityProvider(Entity.class)初始化一个Entity对象
		EntityData.setEntityData(entity, SynchedEntityDataOp.newBasicEntityData(entity));
		try {
			Entity_defineSynchedData.invokeExact(entity);
		} catch (Throwable ex) {
			ModEntry.LOGGER.error("create blank entity '" + entityClazz + "' failed", ex);
		}
		return entity;
	}

	public static final <_T extends Entity> _T allocate(Class<_T> entityClazz, Consumer<_T> init) {
		_T entity = BlankEntity.allocate(entityClazz);
		init.accept(entity);
		CapabilityData.gatherCapabilities(entity);// 必须！否则YSM不会渲染模型
		return entity;
	}
}
