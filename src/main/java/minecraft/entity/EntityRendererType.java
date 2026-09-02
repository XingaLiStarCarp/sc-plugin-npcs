package minecraft.entity;

import java.util.ArrayList;

import sys.jvm.symbols;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 实体渲染器相关信息
 */
public class EntityRendererType<_RenderAsset> {

	private Class<?> rendererClazz = null;
	private Class<?>[] rendererClazzCtorTypes = null;

	// 每种实体渲染类型都有哪些EntityType
	private final ArrayList<DeferredHolder<EntityType<?>, ? extends EntityType<?>>> entityTypes;

	// 默认的渲染使用的全部模型、纹理数据
	private _RenderAsset defaultAsset;

	public EntityRendererType(_RenderAsset defaultRenderAsset) {
		this.defaultAsset = defaultRenderAsset;
		this.entityTypes = new ArrayList<>();
	}

	public EntityRendererType() {
		this(null);
	}

	public final Class<?> rendererType() {
		return rendererClazz;
	}

	public final Class<?>[] rendererCtorTypes() {
		return rendererClazzCtorTypes;
	}

	public String toString() {
		return "EntityRendererType[" + rendererClazz + "]";
	}

	/**
	 * 获取该性别下所有的EntityType
	 * 
	 * @return
	 */
	public final ArrayList<DeferredHolder<EntityType<?>, ? extends EntityType<?>>> entityTypes() {
		return entityTypes;
	}

	/**
	 * 获取默认渲染信息
	 * 
	 * @return
	 */
	public final _RenderAsset defaultAsset() {
		if (defaultAsset == null)
			throw new java.lang.IllegalStateException("no default asset defined for '" + this.toString() + "'");
		else
			return defaultAsset;
	}

	public final void setDefaultRenderAsset(_RenderAsset ra) {
		this.defaultAsset = ra;
	}

	/**
	 * 为EntityRendererType注册对应的渲染器类型。<br>
	 * 可以在static代码块中调用。<br>
	 * 仅能在客户端调用，服务端不存在类rendererClazz，如果调用会导致错误。<br>
	 * 
	 * @param rendererType
	 * @param rendererClazz
	 */
	@OnlyIn(Dist.CLIENT)
	public final void registerRenderer(Class<?> rendererClazz, Class<?>... rendererClazzCtorTypes) {
		this.rendererClazz = rendererClazz;
		this.rendererClazzCtorTypes = rendererClazzCtorTypes;
	}

	/**
	 * 创建一个新的渲染器
	 * 
	 * @param args
	 * @return
	 */
	@OnlyIn(Dist.CLIENT)
	public final Object newRenderer(Object... args) {
		return symbols.construct(rendererClazz, rendererClazzCtorTypes, args);
	}

	/**
	 * 将一个EntityType的渲染器注册为本渲染器。<br>
	 * 
	 * @param type
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final void apply(DeferredHolder<EntityType<?>, ? extends EntityType<?>> type) {
		entityTypes.add(type);
	}
}