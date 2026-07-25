package minecraft.client.graphics.render.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;

import minecraft.entity.Humanoid;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * 综合男女两种体型的使用玩家模型的实体渲染器。<br>
 * 原版渲染器，如果有mod对PlayerRenderer使用了Mixin注入，将不会影响到此方法。
 * 
 * @param <_T>
 */
public class GeneralVallinaPlayerModelRenderer<_T extends LivingEntity> extends EntityRenderer<_T> {
	private class ModelRenderer extends VallinaPlayerModelRenderer<_T> {
		public ModelRenderer(Context context, boolean slim) {
			super(context, slim);
		}

		@Override
		public ResourceLocation getTextureLocation(_T entity) {
			return GeneralVallinaPlayerModelRenderer.this.getTextureLocation(entity);
		}
	}

	private VallinaPlayerModelRenderer<_T> wideRenderer;
	private VallinaPlayerModelRenderer<_T> slimRenderer;

	public GeneralVallinaPlayerModelRenderer(EntityRendererProvider.Context context) {
		super(context);
		wideRenderer = this.new ModelRenderer(context, false);
		slimRenderer = this.new ModelRenderer(context, true);
	}

	private ResourceLocation directTexture;

	public ResourceLocation skinTexture(_T entity) {
		throw new java.lang.IllegalStateException("this method should be overrided");
	}

	@Override
	public final ResourceLocation getTextureLocation(_T entity) {
		return directTexture == null ? this.skinTexture(entity) : directTexture;
	}

	/**
	 * 本渲染器可以直接渲染LivingEntity实体
	 * 
	 * @param entity
	 * @param texture
	 * @param isSlim
	 * @param entityYaw
	 * @param partialTick
	 * @param poseStack
	 * @param bufferSource
	 * @param packedLight
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void render(LivingEntity entity, ResourceLocation texture, boolean isSlim, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		this.directTexture = texture;
		if (isSlim)
			((VallinaPlayerModelRenderer) slimRenderer).render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		else
			((VallinaPlayerModelRenderer) wideRenderer).render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		this.directTexture = null;
	}

	public void render(LivingEntity entity, Humanoid humanoid, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		this.render(entity, humanoid.getSkin(), humanoid.isSlim(), entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}

	/**
	 * 实体是否是slim类型的皮肤
	 * 
	 * @param entity
	 * @return
	 */
	protected boolean isSlim(_T entity) {
		throw new java.lang.IllegalStateException("this method should be overrided");
	}

	@Override
	public void render(_T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		if (this.isSlim(entity))
			slimRenderer.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		else
			wideRenderer.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}
