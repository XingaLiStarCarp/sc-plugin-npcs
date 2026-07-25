package minecraft.client.graphics.render.entity;

import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import minecraft.entity.ProxyRenderEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * 用于将一种实体类型的全部实体渲染为另一种实体的模型。<br>
 * 
 * @param <_RenderingEntity>
 * @param <_ProxyEntity>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class ProxyRenderEntityRenderer<_ProxyEntity extends ProxyRenderEntity<?, ?>, _ProxyRenderer extends EntityRenderer<?>> extends EntityRenderer<Entity> {
	protected final _ProxyRenderer proxyEntityRenderer;
	private Entity dispatchKeyEntity;
	private _ProxyEntity dispatchedProxyEntity;

	protected ProxyRenderEntityRenderer(EntityRendererProvider.Context context, _ProxyRenderer proxyEntityRenderer) {
		super(context);
		this.proxyEntityRenderer = Objects.requireNonNull(proxyEntityRenderer);
		this.shadowRadius = EntityRenderers.getEntityRendererShadowRadius(proxyEntityRenderer);// 设置实体的脚下阴影半径
		this.shadowStrength = EntityRenderers.getEntityRendererShadowStrength(proxyEntityRenderer);// 设置实体脚下阴影的强度
	}

	/**
	 * 在渲染前进行可能存在的模型设置
	 * 
	 * @param proxyEntity
	 */
	protected void setupModel(_ProxyRenderer proxyEntityRenderer, _ProxyEntity proxyEntity) {

	}

	public void render(_ProxyEntity proxyEntity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		if (proxyEntity != null) {
			this.setupModel(proxyEntityRenderer, proxyEntity);
			((EntityRenderer) proxyEntityRenderer).render(proxyEntity.renderingEntity(), entityYaw, partialTick, poseStack, bufferSource, packedLight);
		}
	}

	/**
	 * 为指定的实体指派一个同步渲染模型
	 * 
	 * @param entity 原本的实体
	 * @return 要替换的模型虚假实体。返回null则不会渲染任何模型
	 */
	protected abstract _ProxyEntity dispatchProxyEntity(Entity entity);

	/**
	 * 缓存dispatchProxyEntity()得到的对象，直到以不同的entity调用此函数时重置。<br>
	 * 
	 * @param entity
	 * @return
	 */
	public final _ProxyEntity dispatch(Entity entity) {
		if (dispatchKeyEntity != entity) {
			this.dispatchKeyEntity = entity;
			dispatchedProxyEntity = this.dispatchProxyEntity(entity);
		}
		return dispatchedProxyEntity;
	}

	@Override
	public void render(Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		this.render(this.dispatch(entity), entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(Entity entity) {
		_ProxyEntity proxyEntity = this.dispatch(entity);
		if (proxyEntity == null)
			return null;
		return ((EntityRenderer) proxyEntityRenderer).getTextureLocation(proxyEntity.renderingEntity());
	}
}
