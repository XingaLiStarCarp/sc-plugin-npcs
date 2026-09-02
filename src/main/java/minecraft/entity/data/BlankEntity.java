package minecraft.entity.data;

import java.lang.invoke.MethodHandle;

import minecraft.core.Core;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import sys.jvm.symbols;
import sys.jvm.unsafe;

/**
 * 仅用作数据容器的虚假实体
 */
public class BlankEntity {
	private static MethodHandle Entity_defineSynchedData;

	static {
		Entity_defineSynchedData = symbols.find_virtual_method(Entity.class, "defineSynchedData", void.class, SynchedEntityData.Builder.class);
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
		// 创建一个无初始化的空Entity对象，并设置同步数据初始化一个Entity对象
		_T entity = (_T) unsafe.allocate(entityClazz);
		SynchedEntityData.Builder builder = SynchedEntityDataOp.newBasicEntityData(entity);
		try {
			Entity_defineSynchedData.invokeExact(entity, builder);
		} catch (Throwable ex) {
			Core.logError("create blank entity '{}' failed: {}", entityClazz, Core.throwableString(ex));
		}
		EntityData.setEntityData(entity, builder.build());
		return entity;
	}
}
