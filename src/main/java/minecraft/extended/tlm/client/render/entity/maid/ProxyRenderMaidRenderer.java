package minecraft.extended.tlm.client.render.entity.maid;

import minecraft.client.graphics.render.entity.EntityRenderers;
import minecraft.extended.tlm.entity.maid.MaidMob;
import minecraft.extended.tlm.entity.maid.ProxyRenderMaid;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * MaidMob渲染器
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class ProxyRenderMaidRenderer extends GeneralProxyRenderMaidRenderer {
	static {
		MaidMob.RENDERER_TYPE.registerRenderer(ProxyRenderMaidRenderer.class, EntityRendererProvider.Context.class);
	}

	public ProxyRenderMaidRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected ProxyRenderMaid dispatchProxyEntity(Entity entity) {
		if (entity instanceof ProxyRenderMaid maid)
			return maid;
		else
			return null;
	}

	@SubscribeEvent
	public static void register(EntityRenderersEvent.AddLayers event) {
		EntityRenderers.register(MaidMob.RENDERER_TYPE, EntityRenderers.context());
	}
}
