package minecraft.client.graphics.render.entity.player;

import minecraft.client.graphics.render.entity.ProxyRenderEntityRenderer;
import minecraft.entity.mob.ProxyRenderPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class ProxyRenderPlayerRenderer extends ProxyRenderEntityRenderer<ProxyRenderPlayer, CustomGeneralPlayerRenderer> {

	public ProxyRenderPlayerRenderer(EntityRendererProvider.Context context) {
		super(context, new CustomGeneralPlayerRenderer(context));
	}

	@Override
	protected void setupModel(CustomGeneralPlayerRenderer proxyEntityRenderer, ProxyRenderPlayer proxyPlayer) {
		proxyEntityRenderer.setupModelAsset(proxyPlayer.getSkin(), proxyPlayer.isSlim());
	}

	@Override
	protected ProxyRenderPlayer dispatchProxyEntity(Entity entity) {
		if (entity instanceof ProxyRenderPlayer player) {
			return player;
		} else {
			return null;
		}
	}

}
