package minecraft.client.graphics.ui;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import javabase.Pair;
import minecraft.core.Core;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import sys.jvm.reflection;
import sys.jvm.symbols;
import sys.jvm.unsafe;

/**
 * 覆盖游戏的顶层UI
 */
public interface ExScreen<_ImplScreen extends Screen & ExScreen<_ImplScreen>> extends Renderable {
	/**
	 * 界面左上角X坐标
	 * 
	 * @return
	 */
	public default int x() {
		return 0;
	}

	/**
	 * 界面左上角Y坐标
	 * 
	 * @return
	 */
	public default int y() {
		return 0;
	}

	/**
	 * 界面总宽度。<br>
	 * 默认为屏幕宽度
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default int width() {
		return ((_ImplScreen) this).width;
	}

	/**
	 * 界面总高度。<br>
	 * 默认为屏幕高度
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default int height() {
		return ((_ImplScreen) this).height;
	}

	@SuppressWarnings("unchecked")
	public default List<NarratableEntry> narratables() {
		class __fields {
			private static Field Screen_narratables;
			static {
				Screen_narratables = reflection.find_declared_field(Screen.class, "narratables");
			}
		}
		return (List<NarratableEntry>) unsafe.read_reference(this, __fields.Screen_narratables);
	}

	/**
	 * 绘制整个纹理
	 * 
	 * @param g
	 * @param x           绘制起始X坐标
	 * @param y           绘制起始Y坐标
	 * @param width       绘制宽度
	 * @param height      绘制高度
	 * @param texture
	 * @param partialTick
	 */
	public default void renderTexture(GuiGraphics g, ResourceLocation texture, int x, int y, int width, int height, float partialTick) {
		g.blit(texture, x, y, 0, 0, width, height, width, height);
	}

	public default void renderItem(GuiGraphics g, ItemStack item, int x, int y) {
		g.renderItem(item, x, y);
	}

	/**
	 * 绘制目标纹理作为背景
	 * 
	 * @param g
	 * @param texture
	 * @param partialTick
	 */
	public default void renderBgTexture(GuiGraphics g, ResourceLocation texture, float partialTick) {
		this.renderTexture(g, texture, x(), y(), width(), height(), partialTick);
	}

	/**
	 * 在屏幕中心绘制纹理
	 * 
	 * @param g
	 * @param texture
	 * @param width
	 * @param height
	 * @param partialTick
	 */
	public default void renderCencteredTexture(GuiGraphics g, ResourceLocation texture, int width, int height, float partialTick) {
		this.renderTexture(g, texture, x() + (width() - width) / 2, y() + (height() - height) / 2, width, height, partialTick);
	}

	public abstract void renderScreen(GuiGraphics g, int mouseX, int mouseY, float partialTick);

	/**
	 * 渲染屏幕方法，需要在Screen中覆写同名方法并调用此方法。
	 * 
	 * @param g
	 * @param mouseX
	 * @param mouseY
	 * @param partialTick
	 */
	@SuppressWarnings("unchecked")
	public default void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		g.pose().pushPose();
		g.pose().translate((float) x(), (float) y(), 0.0f);
		RenderSystem.disableDepthTest();// 关闭深度测试直接在屏幕上绘制
		((_ImplScreen) this).renderScreen(g, mouseX, mouseY, partialTick);// 包含原版的背景渲染
		g.pose().popPose();
	}

	/**
	 * 设置全部控件是否处于激活状态
	 * 
	 * @param widgets
	 * @param active
	 */
	public static void setActive(List<? extends GuiEventListener> widgets, boolean active) {
		for (GuiEventListener listener : widgets) {
			if (listener instanceof AbstractWidget widget) {
				widget.active = active;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public default void setActive(boolean active) {
		setActive(((_ImplScreen) this).children(), active);
	}

	/**
	 * 设置全部控件是否处于可见状态
	 * 
	 * @param widgets
	 * @param active
	 */
	public static void setVisible(List<? extends Renderable> widgets, boolean visible) {
		for (Renderable renderable : widgets) {
			if (renderable instanceof AbstractWidget widget) {
				widget.visible = visible;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public default void setVisible(boolean visible) {
		setVisible(((_ImplScreen) this).renderables, visible);
	}

	/**
	 * 设置UI的默认尺寸和位置
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public abstract void setSize(int x, int y, int width, int height);

	@EventBusSubscriber(value = Dist.CLIENT)
	public static class ScreenRegister {
		@SuppressWarnings("rawtypes")
		private static final ArrayList<Pair<DeferredHolder<MenuType<?>, MenuType<?>>, MenuScreens.ScreenConstructor>> screen_ctors = new ArrayList<>();

		/**
		 * 监听RegisterMenuScreensEvent事件并注册。
		 * 在1.20.1上无事件，需要手动注册.
		 * 
		 * @param event
		 */
		@SubscribeEvent
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static void onFMLClientSetupEvent(RegisterMenuScreensEvent event) {
			for (Pair<DeferredHolder<MenuType<?>, MenuType<?>>, MenuScreens.ScreenConstructor> pair : screen_ctors) {
				event.register(pair.first.get(), pair.second);
			}
		}
	}

	/**
	 * 注册Screen。<br>
	 * 
	 * @param <_Menu>
	 * @param <_Screen>
	 * @param menuClazz
	 * @param screenClazz
	 * @param name
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <_Menu extends AbstractContainerMenu, _Screen extends Screen> void register(DeferredHolder<MenuType<?>, MenuType<_Menu>> menuRegObj, Class<_Menu> menuClazz, Class<_Screen> screenClazz, String name) {
		// 注册Menu对应的Screen
		MenuScreens.ScreenConstructor screenCtor = (AbstractContainerMenu menu, Inventory inv, Component title) -> {
			MethodHandle screenConstructor = symbols.find_constructor(screenClazz, menuClazz, Inventory.class, Component.class);
			try {
				return (Screen) screenConstructor.invoke(menu, inv, title);
			} catch (Throwable ex) {
				Core.logError("create Screen of type '" + name + "' failed", ex);
				return null;
			}
		};
		ScreenRegister.screen_ctors.add((Pair) Pair.of(menuRegObj, screenCtor));
	}
}
