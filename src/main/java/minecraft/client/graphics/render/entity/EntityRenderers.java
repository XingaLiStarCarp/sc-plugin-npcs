package minecraft.client.graphics.render.entity;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;

import cpw.mods.modlauncher.api.INameMappingService;
import jvmsp.reflection;
import jvmsp.symbols;
import jvmsp.unsafe;
import minecraft.entity.EntityRendererType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.RegistryObject;

/**
 * 实体渲染器操作
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
public class EntityRenderers {
	private static EntityRenderDispatcher entityRenderDispatcher;
	private static ResourceManager resourceManager;

	private static Font font;
	private static ItemRenderer itemRenderer;
	private static BlockRenderDispatcher blockRenderDispatcher;
	private static ItemInHandRenderer itemInHandRenderer;
	private static EntityModelSet entityModels;

	private static Map<EntityType<?>, EntityRenderer<?>> entityRenderers;
	private static Map<String, EntityRenderer<? extends Player>> playerRenderers;

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onFMLClientSetup(FMLClientSetupEvent event) {
		Minecraft mc = Minecraft.getInstance();
		entityRenderDispatcher = mc.getEntityRenderDispatcher();
		resourceManager = mc.getResourceManager();
		font = (Font) unsafe.read_member_reference(entityRenderDispatcher, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, "f_114365_"));
		itemRenderer = (ItemRenderer) unsafe.read_member_reference(entityRenderDispatcher, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, "f_173995_"));
		blockRenderDispatcher = (BlockRenderDispatcher) unsafe.read_member_reference(entityRenderDispatcher, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, "f_234576_"));
		itemInHandRenderer = (ItemInHandRenderer) unsafe.read_member_reference(entityRenderDispatcher, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, "f_234577_"));
		entityModels = (EntityModelSet) unsafe.read_member_reference(entityRenderDispatcher, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, "f_173996_"));
		context = newContext();
	}

	public static final EntityRenderDispatcher renderDispatcher() {
		return entityRenderDispatcher;
	}

	/**
	 * 根据当前状态创建一个新的Context
	 * 
	 * @return
	 */
	public static final EntityRendererProvider.Context newContext() {
		return new EntityRendererProvider.Context(entityRenderDispatcher, itemRenderer, blockRenderDispatcher, itemInHandRenderer, resourceManager, entityModels, font);
	}

	private static EntityRendererProvider.Context context;

	/**
	 * 由于客户端启动后Context成员不变，因此可以使用延迟初始化的单例变量
	 * 
	 * @return
	 */
	public static final EntityRendererProvider.Context context() {
		return context;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void init(EntityRenderersEvent.AddLayers event) {
		// 将EntityRenderDispatcher的渲染器Map解锁
		entityRenderers = new HashMap<>(getEntityRenderers());
		playerRenderers = new HashMap<>(getPlayerRenderers());
		// 由于entityRenderers和playerRenderers从原则上讲不允许修改，因此其他处理该事件的代码对这两个Map是只读的，不需要将可变的HashMap对象更新到event对象
		setEntityRenderers(entityRenderers);
		setPlayerRenderers(playerRenderers);
	}

	public static final <_T extends EntityRenderer<?>> _T create(Class<_T> rendererClazz) {
		try {
			MethodHandle constructor = symbols.find_constructor(rendererClazz, EntityRendererProvider.Context.class);
			return (_T) constructor.invoke(context());
		} catch (Throwable ex) {
			return null;
		}
	}

	public static final Map<EntityType<?>, EntityRenderer<?>> getEntityRenderers() {
		return entityRenderDispatcher.renderers;
	}

	@SuppressWarnings("unchecked")
	public static final Map<String, EntityRenderer<? extends Player>> getPlayerRenderers() {
		return (Map<String, EntityRenderer<? extends Player>>) unsafe.read_member_reference(entityRenderDispatcher, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, "f_114363_"));
	}

	public static final void setEntityRenderers(Map<EntityType<?>, EntityRenderer<?>> entityRenderers) {
		entityRenderDispatcher.renderers = entityRenderers;
	}

	public static final void setPlayerRenderers(Map<String, EntityRenderer<? extends Player>> playerRenderers) {
		unsafe.write_member(entityRenderDispatcher, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, "f_114363_"), playerRenderers);
	}

	/**
	 * 设置实体使用的渲染器
	 * 
	 * @param type
	 * @param renderer
	 */
	public static final void setEntityRenderer(EntityType<?> type, EntityRenderer<?> renderer) {
		entityRenderers.put(type, renderer);
	}

	public static final EntityRenderer<?> getEntityRenderer(EntityType<?> type) {
		return entityRenderers.get(type);
	}

	public static final void setPlayerRenderer(String name, EntityRenderer<? extends Player> renderer) {
		playerRenderers.put(name, renderer);
	}

	public static final EntityRenderer<? extends Player> getPlayerRenderer(String name) {
		return playerRenderers.get(name);
	}

	public static final void register(EntityRendererType<?> rendererType, Object... args) {
		for (RegistryObject<EntityType<?>> type : rendererType.entityTypes()) {
			register(type.get(), rendererType, args);
		}
	}

	public static final void register(EntityType<?> type, EntityRendererType<?> rendererType, Object... args) {
		EntityRenderers.setEntityRenderer(type, (EntityRenderer<?>) rendererType.newRenderer(args));
	}

	private static Field EntityRenderer_shadowRadius;
	private static Field EntityRenderer_shadowStrength;

	static {
		EntityRenderer_shadowRadius = reflection.find_declared_field(EntityRenderer.class, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, "f_114477_"));
		EntityRenderer_shadowStrength = reflection.find_declared_field(EntityRenderer.class, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, "f_114478_"));
	}

	public static final float getEntityRendererShadowRadius(EntityRenderer<?> renderer) {
		return unsafe.read_float(renderer, EntityRenderer_shadowRadius);
	}

	public static final void setEntityRendererShadowRadius(EntityRenderer<?> renderer, float shadowRadius) {
		unsafe.write(renderer, EntityRenderer_shadowRadius, shadowRadius);
	}

	public static final float getEntityRendererShadowStrength(EntityRenderer<?> renderer) {
		return unsafe.read_float(renderer, EntityRenderer_shadowStrength);
	}

	public static final void setEntityRendererShadowStrength(EntityRenderer<?> renderer, float shadowStrength) {
		unsafe.write(renderer, EntityRenderer_shadowStrength, shadowStrength);
	}

	private static MethodHandle LivingEntityRenderer_render;

	static {
		LivingEntityRenderer_render = symbols.find_special_method(LivingEntityRenderer.class, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.METHOD, "m_7392_"),
				void.class,
				LivingEntity.class, float.class, float.class, PoseStack.class, MultiBufferSource.class, int.class);
	}

	public static final void renderLivingEntity(LivingEntityRenderer<?, ?> renderer, LivingEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		try {
			LivingEntityRenderer_render.invoke(renderer, entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
}
