package ba.entries.block;

import ba.ModEntry;
import minecraft.block.ExtBlock;
import minecraft.datagen.annotation.LangDatagen;
import minecraft.datagen.annotation.Translation;
import net.neoforged.neoforge.registries.DeferredBlock;

public class Blocks {
	static {
		LangDatagen.LangProvider.forDatagen(Blocks.class);
	}

	public static final String ORDER_TABLE_ID = "order_table";
	public static final String ORDER_TABLE_MENU_LANG_KEY = "gui." + ModEntry.ModId + "." + ORDER_TABLE_ID;
	public static final String ORDER_TABLE_HINT_LANG_KEY = ORDER_TABLE_MENU_LANG_KEY + ".hint";
	public static final DeferredBlock<OrderTableEntityBlock> ORDER_TABLE = ExtBlock.register(ORDER_TABLE_ID, () -> new OrderTableEntityBlock());
}
