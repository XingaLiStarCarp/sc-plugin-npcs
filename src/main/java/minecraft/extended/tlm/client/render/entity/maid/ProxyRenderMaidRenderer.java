package minecraft.extended.tlm.client.render.entity.maid;

import minecraft.client.graphics.render.entity.EntityRenderers;
import minecraft.extended.tlm.entity.maid.MaidMob;
import minecraft.extended.tlm.entity.maid.ProxyRenderMaid;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * MaidMob渲染器
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
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
