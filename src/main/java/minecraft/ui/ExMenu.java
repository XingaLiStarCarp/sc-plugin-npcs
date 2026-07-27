package minecraft.ui;

import java.lang.invoke.MethodHandle;

import cpw.mods.modlauncher.api.INameMappingService;
import jvmsp.symbols;
import jvmsp.type.java_type;
import minecraft.registry.Registers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.RegistryObject;
import scba.ModEntry;

public interface ExMenu<_Menu extends AbstractContainerMenu & ExMenu<_Menu>> {
	/**
	 * 注册Menu。<br>
	 * _Menu必须有构造函数_Menu(MenuType type, int id, Inventory inv, FriendlyByteBuf buf)。<br>
	 * 
	 * @param <_Menu>
	 * @param <_Screen>
	 * @param menuClazz
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <_Menu extends AbstractContainerMenu> RegistryObject<MenuType<_Menu>> newType(Class<_Menu> menuClazz, String name) {
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