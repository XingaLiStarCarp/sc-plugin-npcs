package minecraft.client.graphics.ui;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import cpw.mods.modlauncher.api.INameMappingService;
import javabase.Pair;
import jvmsp.reflection;
import jvmsp.symbols;
import jvmsp.type.java_type;
import jvmsp.unsafe;
import minecraft.registry.Registers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.RegistryObject;
import scba.ModEntry;

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
				Screen_narratables = reflection.find_declared_field(Screen.class, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, "f_169368_"));
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

	@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
	public static class ScreenRegister {
		@SuppressWarnings("rawtypes")
		private static final ArrayList<Pair<RegistryObject<MenuType>, MenuScreens.ScreenConstructor>> screen_ctors = new ArrayList<>();

		@SubscribeEvent
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static void onFMLClientSetupEvent(FMLClientSetupEvent event) {
			event.enqueueWork(() -> {
				for (Pair<RegistryObject<MenuType>, MenuScreens.ScreenConstructor> pair : screen_ctors) {
					MenuScreens.register(pair.first.get(), pair.second);
				}
			});
		}
	}

	public static interface ExMenu<_Menu extends AbstractContainerMenu & ExMenu<_Menu>> {
		/**
		 * 注册Menu及其对应的Screen。<br>
		 * _Menu必须有构造函数_Menu(MenuType type, int id, Inventory inv, FriendlyByteBuf buf)。<br>
		 * 
		 * @param <_Menu>
		 * @param <_Screen>
		 * @param menuClazz
		 * @param screenClazz
		 * @param name
		 * @return
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static <_Menu extends AbstractContainerMenu, _Screen extends Screen> RegistryObject<MenuType<_Menu>> newType(Class<_Menu> menuClazz, Class<_Screen> screenClazz, String name) {
			java_type.wrapper<RegistryObject<MenuType<_Menu>>> menuType = java_type.wrapper.wrap();
			menuType.value = Registers.MENU_REG.register(name, () -> IForgeMenuType.create((int id, Inventory inv, FriendlyByteBuf buf) -> {
				// Inventory, FriendlyByteBuf参数为mod loader拓展，原AbstractContainerMenu构造函数只有MenuType, int参数
				MethodHandle menuConstructor = symbols.find_constructor(menuClazz, MenuType.class, int.class, Inventory.class, FriendlyByteBuf.class);
				try {
					return (_Menu) menuConstructor.invoke(menuType.value.get(), id, inv, buf);
				} catch (Throwable ex) {
					ModEntry.LOGGER.error("create Menu of type '" + name + "' failed", ex);
					return null;
				}
			}));
			// 注册Menu对应的Screen
			MenuScreens.ScreenConstructor screenCtor = (AbstractContainerMenu menu, Inventory inv, Component title) -> {
				MethodHandle screenConstructor = symbols.find_constructor(screenClazz, menuClazz, Inventory.class, Component.class);
				try {
					return (Screen) screenConstructor.invoke(menu, inv, title);
				} catch (Throwable ex) {
					ModEntry.LOGGER.error("create Screen of type '" + name + "' failed", ex);
					return null;
				}
			};
			ScreenRegister.screen_ctors.add((Pair) Pair.of(menuType.value, screenCtor));
			return menuType.value;
		}

		public default int playerInventoryRoll() {
			return 3;
		}

		public default int playerInventoryColumn() {
			return 9;
		}

		public default int playerHotbarRoll() {
			return 1;
		}

		public default int playerHotbarColumn() {
			return playerInventoryColumn();
		}

		/**
		 * 添加Slot
		 * 
		 * @param slot
		 * @return
		 */
		public default Slot __addSlot(Slot slot) {
			class __methods {
				private static MethodHandle AbstractContainerMenu_addSlot;
				static {
					AbstractContainerMenu_addSlot = symbols.find_virtual_method(AbstractContainerMenu.class, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.METHOD, "m_38897_"), Slot.class, Slot.class);
				}
			}
			try {
				return (Slot) __methods.AbstractContainerMenu_addSlot.invoke(this, slot);
			} catch (Throwable ex) {
				throw new java.lang.InternalError(ex);
			}
		}

		// 玩家背包及快捷栏相关参数
		// 快捷栏、装备栏等实际上也属于背包
		public static int DEFAULT_INV_SLOT_SIZE = 18;
		public static int DEFAULT_INV_SLOT_IDX_BEGIN = 9;// 快捷栏索引原版中从9开始，为9-35
		public static int DEFAULT_INV_SLOT_START_X = 8;
		public static int DEFAULT_INV_SLOT_START_Y = 84;

		public static int DEFAULT_HOTBAR_SLOT_SIZE = 18;
		public static int DEFAULT_HOTBAR_SLOT_IDX_BEGIN = 0;// 快捷栏索引原版中从0开始，为0-9
		public static int DEFAULT_HOTBAR_SLOT_START_X = 8;
		public static int DEFAULT_HOTBAR_SLOT_START_Y = 142;

		/**
		 * 初始化容器格子
		 * 
		 * @param container    容器
		 * @param slotIdxBegin 第一个格子在container中的索引
		 * @param roll         行数
		 * @param column       列数
		 * @param startX       起始X坐标
		 * @param startY       起始Y坐标
		 * @param width        格子宽度
		 * @param height       格子高度
		 */
		public default void initContainer(Container container, int slotIdxBegin, int roll, int column, int startX, int startY, int width, int height) {
			for (int i = 0; i < roll; ++i) {
				for (int j = 0; j < column; ++j) {
					ExMenu.this.__addSlot(new Slot(container, slotIdxBegin++, startX + j * width, startY + i * height));
				}
			}
		}

		public default void initContainer(Container container, int slotIdxBegin, int roll, int column, int startX, int startY) {
			initContainer(container, slotIdxBegin, roll, column, startX, startY, DEFAULT_INV_SLOT_SIZE, DEFAULT_INV_SLOT_SIZE);
		}

		public default int playerInventorySlotIdxBegin() {
			return DEFAULT_INV_SLOT_IDX_BEGIN;
		}

		public default int playerInventorySlotStartX() {
			return DEFAULT_INV_SLOT_START_X;
		}

		public default int playerInventorySlotStartY() {
			return DEFAULT_INV_SLOT_START_Y;
		}

		public default int playerInventorySlotWidth() {
			return DEFAULT_INV_SLOT_SIZE;
		}

		public default int playerInventorySlotHeight() {
			return DEFAULT_INV_SLOT_SIZE;
		}

		/**
		 * 初始化玩家背包<br>
		 * 
		 * @param inventory
		 */
		public default void initPlayerInventory(Inventory inventory) {
			this.initContainer(inventory, this.playerInventorySlotIdxBegin(), this.playerInventoryRoll(), this.playerInventoryColumn(), this.playerInventorySlotStartX(), this.playerInventorySlotStartY(), this.playerInventorySlotWidth(), this.playerInventorySlotHeight());
		}

		public default int playerHotbarSlotIdxBegin() {
			return DEFAULT_HOTBAR_SLOT_IDX_BEGIN;
		}

		public default int playerHotbarSlotStartX() {
			return DEFAULT_HOTBAR_SLOT_START_X;
		}

		public default int playerHotbarSlotStartY() {
			return DEFAULT_HOTBAR_SLOT_START_Y;
		}

		public default int playerHotbarSlotWidth() {
			return DEFAULT_HOTBAR_SLOT_SIZE;
		}

		public default int playerHotbarSlotHeight() {
			return DEFAULT_HOTBAR_SLOT_SIZE;
		}

		/**
		 * 初始化玩家快捷栏。<br>
		 * 
		 * @param inventory
		 */
		public default void initPlayerHotbar(Inventory inventory) {
			this.initContainer(inventory, this.playerHotbarSlotIdxBegin(), this.playerHotbarRoll(), this.playerHotbarColumn(), this.playerHotbarSlotStartX(), this.playerHotbarSlotStartY(), this.playerHotbarSlotWidth(), this.playerHotbarSlotHeight());
		}
	}
}
