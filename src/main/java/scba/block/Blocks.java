package scba.block;

import minecraft.registry.Registers;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import scba.ModEntry;

public class Blocks {
	public static final String ORDER_TABLE_ID = "order_table";
	public static final String ORDER_TABLE_MENU_LANG_KEY = "gui." + ModEntry.MOD_ID + "." + ORDER_TABLE_ID;
	public static final String ORDER_TABLE_HINT_LANG_KEY = ORDER_TABLE_MENU_LANG_KEY + ".hint";
	public static final RegistryObject<Block> ORDER_TABLE = Registers.registerBlock(ORDER_TABLE_ID, () -> new OrderTableEntityBlock());

	public static final RegistryObject<Item> ORDER_TABLE_ITEM = Registers.registerBlockItem(ORDER_TABLE_ID, ORDER_TABLE);
}
