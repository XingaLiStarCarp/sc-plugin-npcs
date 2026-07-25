package minecraft.extended.tlm.client.render.entity.maid;

import java.util.function.BiConsumer;

import minecraft.client.graphics.render.entity.EntityRenderers;
import minecraft.extended.tlm.entity.maid.ProxyRenderMaid;
import minecraft.extended.tlm.entity.maid.ProxyRenderMaid.MaidModelAsset;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * 将指定EntityType渲染为指定TLM或YSM模型的渲染器
 */
public class MaidModelDispatcher extends GeneralProxyRenderMaidRenderer {
	private BiConsumer<Entity, MaidModelAsset> modelResolver;

	public MaidModelDispatcher(EntityRendererProvider.Context context, BiConsumer<Entity, MaidModelAsset> modelResolver) {
		super(context);
		this.modelResolver = modelResolver;
	}

	public MaidModelDispatcher(BiConsumer<Entity, MaidModelAsset> modelResolver) {
		this(EntityRenderers.context(), modelResolver);
	}

	@Override
	protected ProxyRenderMaid dispatchProxyEntity(Entity entity) {
		ProxyRenderMaid maid = ProxyRenderMaid.bind(entity, modelResolver);
		modelResolver.accept(entity, maid.modelAsset());// 执行更新，每tick同步一次数据
		return maid;
	}

	/**
	 * 指派指定实体类型的渲染器。<br>
	 * 必须在Minecraft客户端初始化完成后调用。<br>
	 * 
	 * @param entityTypes
	 */
	public static void dispatch(EntityType<?> type, BiConsumer<Entity, MaidModelAsset> modelResolver) {
		EntityRenderers.setEntityRenderer(type, new MaidModelDispatcher(modelResolver));
	}
}