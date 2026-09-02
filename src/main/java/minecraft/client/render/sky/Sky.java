package minecraft.client.render.sky;

import sys.jvm.unsafe;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import minecraft.client.render.VanillaRenderable;
import minecraft.client.render.RenderableSceneObject;
import minecraft.client.render.SceneGraphNode;
import minecraft.client.render.VertexBufferManipulator;
import minecraft.client.render.RenderableSceneObject.ViewPos;
import minecraft.mixins.internal.LevelRendererInternal;
import minecraft.resources.ResourceLocations;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Sky {
	public static final String SUN_LOCATION = "SUN_LOCATION";

	public static ResourceLocation getSunTexture() {
		return (ResourceLocation) unsafe.read_static_reference(LevelRenderer.class, SUN_LOCATION);
	}

	public static void setSunTexture(String namespacedLoc) {
		unsafe.write_static(LevelRenderer.class, SUN_LOCATION, ResourceLocations.build(namespacedLoc));
	}

	public static final String MOON_LOCATION = "MOON_LOCATION";

	public static ResourceLocation getMoonTexture() {
		return (ResourceLocation) unsafe.read_static_reference(LevelRenderer.class, MOON_LOCATION);
	}

	public static void setMoonTexture(String namespacedLoc) {
		unsafe.write_static(LevelRenderer.class, MOON_LOCATION, ResourceLocations.build(namespacedLoc));
	}

	public static final String CLOUDS_LOCATION = "CLOUDS_LOCATION";

	public static ResourceLocation getCloudsTexture() {
		return (ResourceLocation) unsafe.read_static_reference(LevelRenderer.class, CLOUDS_LOCATION);
	}

	public static void setCloudsTexture(String namespacedLoc) {
		unsafe.write_static(LevelRenderer.class, CLOUDS_LOCATION, ResourceLocations.build(namespacedLoc));
	}

	public static final String END_SKY_LOCATION = "END_SKY_LOCATION";

	public static ResourceLocation getEndSkyTexture() {
		return (ResourceLocation) unsafe.read_static_reference(LevelRenderer.class, END_SKY_LOCATION);
	}

	public static void setEndSkyTexture(String namespacedLoc) {
		unsafe.write_static(LevelRenderer.class, END_SKY_LOCATION, ResourceLocations.build(namespacedLoc));
	}

	public static final String FORCEFIELD_LOCATION = "FORCEFIELD_LOCATION";

	public static ResourceLocation getForceFieldTexture() {
		return (ResourceLocation) unsafe.read_static_reference(LevelRenderer.class, FORCEFIELD_LOCATION);
	}

	public static void setForceFieldTexture(String namespacedLoc) {
		unsafe.write_static(LevelRenderer.class, FORCEFIELD_LOCATION, ResourceLocations.build(namespacedLoc));
	}

	public static VertexBufferManipulator.NormalizedColorResolver celestialColor = VertexBufferManipulator.NormalizedColorResolver.NONE;

	/**
	 * 设置太阳、月亮的着色器颜色，即各个通道纹理颜色的贡献值
	 * 
	 * @param resolver
	 */
	public static final void setCelestialColorResolver(VertexBufferManipulator.NormalizedColorResolver resolver) {
		celestialColor = resolver;
	}

	public static final void setFixedCelestialColor(float red, float green, float blue, float alpha) {
		setCelestialColorResolver(VertexBufferManipulator.NormalizedColorResolver.fixed(red, green, blue, alpha));
	}

	public static final void setFixedCelestialColor(float red, float green, float blue) {
		setCelestialColorResolver(VertexBufferManipulator.NormalizedColorResolver.fixedRGB(red, green, blue));
	}

	public static final void setFixedCelestialAlpha(float alpha) {
		setCelestialColorResolver(VertexBufferManipulator.NormalizedColorResolver.fixedA(alpha));
	}

	/**
	 * 天空渲染高度为16，天体渲染高度最好只略低于16。<br>
	 * 即视觉上天体透视大小相等时，要选择位置高体积大，而不能选位置低体积小，否则会受到严重的玩家走动镜头晃动影响
	 */
	public static final SceneGraphNode CELESTIAL_BODYS = SceneGraphNode.createSceneGraph();

	private static final SceneGraphNode SKY_BG = SceneGraphNode.createSceneGraph();

	static {
		LevelRendererInternal.RenderLevel.Callbacks.addAfter_popPush_sky(
				(LevelRenderer this_, DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) -> {
					RenderSystem.disableBlend();
					RenderSystem.depthMask(false);
					SKY_BG.render(frustumMatrix, projectionMatrix);
					RenderSystem.depthMask(true);
					RenderSystem.enableBlend();
					CELESTIAL_BODYS.render(frustumMatrix, projectionMatrix);
				});
	}

	public static SceneGraphNode renderSkyBackground(String path, VanillaRenderable sky) {
		return RenderableSceneObject.createRenderableNode(SKY_BG, path, sky);
	}

	/**
	 * 在天空中渲染物体
	 * 
	 * @param path
	 * @param obj
	 */
	public static RenderableSceneObject render(String path, VanillaRenderable obj) {
		return RenderableSceneObject.createRenderableNode(CELESTIAL_BODYS, path, obj);
	}

	public static RenderableSceneObject renderGameObject(String path, VanillaRenderable obj, RenderableSceneObject.Orbit orbit) {
		return render(path, obj).setOrbit(orbit);
	}

	public static RenderableSceneObject renderGameObject(String path, VanillaRenderable obj, float object_x, float object_y, float object_z) {
		return render(path, obj).setOrbit(object_x, object_y, object_z);
	}

	public static RenderableSceneObject renderGameObject(String path, VanillaRenderable obj, float object_x, float object_y, float object_z, ViewPos.NumericalCalculation final_view_height) {
		return render(path, obj).setOrbit(object_x, object_y, object_z, final_view_height);
	}

	/**
	 * 渲染圆形轨道近地物体
	 * 
	 * @param path
	 * @param obj
	 * @param center_x
	 * @param center_z
	 * @param radius
	 * @param view_height
	 * @param angular_speed
	 * @param initial_phase
	 * @return
	 */
	public static RenderableSceneObject renderCircleOrbitGameObject(String path, VanillaRenderable obj, float center_x, float center_z, float radius, float view_height, float angular_speed, float initial_phase) {
		return render(path, obj).setOrbit(RenderableSceneObject.Orbit.circle(center_x, center_z, radius, view_height, angular_speed, initial_phase));
	}

	public static RenderableSceneObject renderCircleOrbitGameObject(String path, VanillaRenderable obj, float center_x, float center_z, float radius, float view_height, float angular_speed) {
		return render(path, obj).setOrbit(RenderableSceneObject.Orbit.circle(center_x, center_z, radius, view_height, angular_speed));
	}

}
