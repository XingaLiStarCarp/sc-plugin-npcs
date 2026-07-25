package minecraft.extended.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import minecraft.client.graphics.render.entity.EntityRenderers;
import minecraft.client.graphics.render.entity.mob.GeneralVallinaPlayerModelRenderer;
import minecraft.client.graphics.render.entity.player.ProxyRenderPlayerRenderer;
import minecraft.extended.entity.GeneralHumanoidMob;
import minecraft.extended.entity.GeneralHumanoidMob.GeneralHumanoidModelInfo;
import minecraft.extended.tlm.client.render.entity.maid.ProxyRenderMaidRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
public class GeneralHumanoidMobRenderer extends EntityRenderer<GeneralHumanoidMob> {
	GeneralVallinaPlayerModelRenderer<LivingEntity> humanoidRenderer;
	ProxyRenderPlayerRenderer playerRenderer;
	ProxyRenderMaidRenderer maidRenderer;

	public GeneralHumanoidMobRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.5f;
		humanoidRenderer = new GeneralVallinaPlayerModelRenderer<>(context);
		playerRenderer = new ProxyRenderPlayerRenderer(context);
		maidRenderer = new ProxyRenderMaidRenderer(context);
	}

	@Override
	public void render(GeneralHumanoidMob entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		switch (entity.getRenderType()) {
		case GeneralHumanoidModelInfo.TYPE_HUMANOID:
			humanoidRenderer.render(entity, entity.proxyRenderPlayer(), entityYaw, partialTicks, poseStack, bufferSource, packedLight);
			break;
		case GeneralHumanoidModelInfo.TYPE_MAID:
			maidRenderer.render(entity.proxyRenderMaid(), entityYaw, partialTicks, poseStack, bufferSource, packedLight);
			break;
		case GeneralHumanoidModelInfo.TYPE_PLAYER:
			playerRenderer.render(entity.proxyRenderPlayer(), entityYaw, partialTicks, poseStack, bufferSource, packedLight);
			break;
		}
	}

	@Override
	public ResourceLocation getTextureLocation(GeneralHumanoidMob entity) {
		switch (entity.getRenderType()) {
		case GeneralHumanoidModelInfo.TYPE_HUMANOID:
			return humanoidRenderer.getTextureLocation(entity);
		case GeneralHumanoidModelInfo.TYPE_MAID:
			return maidRenderer.getTextureLocation(entity);
		case GeneralHumanoidModelInfo.TYPE_PLAYER:
			return playerRenderer.getTextureLocation(entity);
		default:
			return null;
		}
	}

	static {
		GeneralHumanoidMob.RENDERER_TYPE.registerRenderer(GeneralHumanoidMobRenderer.class, EntityRendererProvider.Context.class);
	}

	@SubscribeEvent
	public static void register(EntityRenderersEvent.AddLayers event) {
		EntityRenderers.register(GeneralHumanoidMob.RENDERER_TYPE, EntityRenderers.context());
	}
}
