package minecraft.client.graphics.render.entity.player;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CustomGeneralPlayerRenderer extends GeneralPlayerRenderer {
	private ResourceLocation currentTextureLocation;
	private boolean currentIsSlim;

	public CustomGeneralPlayerRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean isSlim(AbstractClientPlayer localPlayer) {
		return currentIsSlim;
	}

	@Override
	public ResourceLocation getTextureLocation(AbstractClientPlayer localPlayer) {
		return currentTextureLocation;
	}

	/**
	 * 对每个AbstractClientPlayer实体对象需要分别设置，否则都将使用同一皮肤。<br>
	 * 
	 * @param currentIsSlim
	 * @param currentTextureLocation
	 */
	public void setupModelAsset(ResourceLocation currentTextureLocation, boolean currentIsSlim) {
		this.currentTextureLocation = currentTextureLocation;
		this.currentIsSlim = currentIsSlim;
	}
}
