package minecraft.component.trait;

import minecraft.LogicalEnd;
import minecraft.component.TraitProvider.TraitComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * 双端行为不同的特性。
 */
public interface DualEndedTrait<_Param> extends TraitComponent<_Param> {

	public abstract boolean isClient();

	/**
	 * 判断当前是否处于正确的端上。<br>
	 * 双端事件的执行必须先验证端是否正确，否则会造成重复执行或服务端链接客户端的类。<br>
	 * 
	 * @param level 触发的Level
	 * @return
	 */
	public default boolean validate(Level level) {
		return isClient() == level.isClientSide;
	}

	public default boolean validate(Entity entity) {
		return validate(entity.level());
	}

	/**
	 * 此方法仅判断运行环境，无法区分双端事件。<br>
	 * 
	 * @return
	 */
	public default boolean validate() {
		return isClient() == LogicalEnd.host().isClientAvailable();
	}
}
