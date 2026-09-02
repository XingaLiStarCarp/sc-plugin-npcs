package minecraft.client.render.sky;

import sys.jvm.unsafe;

import graphics.shader.ScreenShader;
import minecraft.client.render.VertexBufferManipulator;
import minecraft.ext.client.render.iris.IrisPostprocess;
import minecraft.resources.ResourceLocations;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WeatherEffect {

	public static VertexBufferManipulator.ColorResolver rainColorResolver;

	public static VertexBufferManipulator.ColorResolver snowColorResolver;

	public static void setRainColorResolver(VertexBufferManipulator.ColorResolver resolver) {
		rainColorResolver = resolver;
	}

	/**
	 * 设置下雨颜色为固定颜色
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 */
	public static void setRainColor(int r, int g, int b, int a) {
		setRainColorResolver(VertexBufferManipulator.ColorResolver.fixed(r, g, b, a));
	}

	public static void setRainColor(int r, int g, int b) {
		setRainColorResolver(VertexBufferManipulator.ColorResolver.fixedRGB(r, g, b));
	}

	public static void setRainAlpha(int a) {
		setRainColorResolver(VertexBufferManipulator.ColorResolver.fixedA(a));
	}

	public static void setSnowColorResolver(VertexBufferManipulator.ColorResolver resolver) {
		snowColorResolver = resolver;
	}

	public static void setSnowColor(int r, int g, int b, int a) {
		setSnowColorResolver(VertexBufferManipulator.ColorResolver.fixed(r, g, b, a));
	}

	public static void setSnowColor(int r, int g, int b) {
		setSnowColorResolver(VertexBufferManipulator.ColorResolver.fixedRGB(r, g, b));
	}

	public static void setSnowAlpha(int a) {
		setSnowColorResolver(VertexBufferManipulator.ColorResolver.fixedA(a));
	}

	public static final String RAIN_LOCATION = "RAIN_LOCATION";

	public static ResourceLocation getRainTexture() {
		return (ResourceLocation) unsafe.read_static_reference(LevelRenderer.class, RAIN_LOCATION);
	}

	public static void setRainTexture(String namespacedLoc) {
		unsafe.write_static(LevelRenderer.class, RAIN_LOCATION, ResourceLocations.build(namespacedLoc));
	}

	public static final String SNOW_LOCATION = "SNOW_LOCATION";

	public static ResourceLocation getSnowTexture() {
		return (ResourceLocation) unsafe.read_static_reference(LevelRenderer.class, SNOW_LOCATION);
	}

	public static void setSnowTexture(String namespacedLoc) {
		unsafe.write_static(LevelRenderer.class, SNOW_LOCATION, ResourceLocations.build(namespacedLoc));
	}

	public static final void setWeatherPostprocessShader(ScreenShader sky_postprocess_shader) {
		IrisPostprocess.setPhasePostprocessShader("RAIN_SNOW", sky_postprocess_shader);
	}
}
