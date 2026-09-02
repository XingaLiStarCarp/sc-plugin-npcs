package minecraft.client.graphics.render.entity.mob;

import minecraft.client.graphics.render.entity.EntityRenderers;
import minecraft.entity.Humanoid;
import minecraft.entity.mob.HumanoidMob;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 渲染实现了Humanoid接口的实体。<br>
 * 使用了原版的PlayerRenderer的渲染策略，如果PlayerRenderer被Mixin注入了新功能，此渲染器不会受影响。<br>
 * 
 * @param <_T>
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT)
public class GeneralVallinaHumanoidRenderer<_T extends LivingEntity & Humanoid> extends GeneralVallinaPlayerModelRenderer<_T> {

	public GeneralVallinaHumanoidRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation skinTexture(_T entity) {
		return entity.getSkin();
	}

	@Override
	protected boolean isSlim(_T entity) {
		return entity.isSlim();
	}

	static {
		HumanoidMob.RENDERER_TYPE.registerRenderer(GeneralVallinaHumanoidRenderer.class, EntityRendererProvider.Context.class);
	}

	@SubscribeEvent
	public static void register(EntityRenderersEvent.AddLayers event) {
		EntityRenderers.register(HumanoidMob.RENDERER_TYPE, EntityRenderers.context());
	}
}