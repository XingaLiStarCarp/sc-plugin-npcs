package scba.block;

import minecraft.registry.Registers;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTabs {
	public static final RegistryObject<Item> BLOCKS_TAB_ICON = Blocks.ORDER_TABLE_ITEM;

	public static final RegistryObject<CreativeModeTab> BLOCKS_TAB = Registers.CREATIVE_TABS_REG.register("sc_blocks",
			() -> CreativeModeTab.builder().withTabsBefore(CreativeModeTabs.COMBAT)
					.icon(() -> BLOCKS_TAB_ICON.get().getDefaultInstance()).displayItems((parameters, output) -> {
						output.accept(BLOCKS_TAB_ICON.get());
					}).build());
}
