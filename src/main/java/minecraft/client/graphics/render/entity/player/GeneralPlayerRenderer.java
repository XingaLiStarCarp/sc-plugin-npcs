package minecraft.client.graphics.render.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

public abstract class GeneralPlayerRenderer extends EntityRenderer<AbstractClientPlayer> {
	/**
	 * PlayerRenderer可能被Mixin。<br>
	 * YSM在渲染玩家的事件中使用它自己的渲染器进行的渲染，因此该类自定义的shouldShowName()会被YSM覆盖，实际不生效。<br>
	 * 且YSM的渲染器中会通过player.getTeam().getNameTagVisibility()来决定是否渲染GameProfile的名称。<br>
	 */
	private class CustomPlayerRenderer extends PlayerRenderer {
		public CustomPlayerRenderer(Context context, boolean slim) {
			super(context, slim);
		}

		@Override
		public final ResourceLocation getTextureLocation(AbstractClientPlayer entity) {
			return GeneralPlayerRenderer.this.getTextureLocation(entity);
		}

		@Override
		protected final boolean shouldShowName(AbstractClientPlayer entity) {
			return GeneralPlayerRenderer.this.shouldShowName(entity);
		}

		@Override
		public final void render(AbstractClientPlayer entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
			if (this.shouldShowName(entity)) {
				super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);// 此处渲染玩家时，shouldShowName()必定会为true，除非Team设置名称不可见
			} else {
				Minecraft mc = Minecraft.getInstance();
				boolean original = mc.options.hideGui;
				mc.options.hideGui = true;// 此为兼容Geckolib、YSM的shouldShowName()判断，当hideGui为true时，不会渲染名字，但实体模型本身还会渲染
				super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);// 注意：YSM渲染会出现头发乱摆动问题
				mc.options.hideGui = original;
			}
		}
	}

	private PlayerRenderer wideRenderer;
	private PlayerRenderer slimRenderer;

	public GeneralPlayerRenderer(EntityRendererProvider.Context context) {
		super(context);
		wideRenderer = this.new CustomPlayerRenderer(context, false);
		slimRenderer = this.new CustomPlayerRenderer(context, true);
	}

	/**
	 * 实体是否是slim类型的皮肤
	 * 
	 * @param entity
	 * @return
	 */
	protected abstract boolean isSlim(AbstractClientPlayer entity);

	@Override
	public void render(AbstractClientPlayer entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		if (this.isSlim(entity))
			slimRenderer.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		else
			wideRenderer.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}
