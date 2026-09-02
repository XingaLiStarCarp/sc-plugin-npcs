package ba.entries.item;

import ba.entries.creativetab.BaCreativeTab;
import minecraft.core.Core;
import minecraft.datagen.annotation.ItemDatagen;
import minecraft.datagen.annotation.LangDatagen;
import minecraft.datagen.annotation.Translation;
import minecraft.item.ExtItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

public class BaKeyItems {

	static {
		ItemDatagen.ModelProvider.forDatagen(BaKeyItems.class);
		LangDatagen.LangProvider.forDatagen(BaKeyItems.class);
	}
	public static final String namespacePrefix = Core.ModIdPrefix;

	public static final String resourcePath = BaCreativeTab.TexPath.BA_KEY_ITEMS;

	@LangDatagen(translations = { @Translation(locale = "en_us", text = "Shittim Chest"), @Translation(locale = "zh_cn", text = "什亭之匣") })
	@ItemDatagen(tex_path = resourcePath)
	public static final DeferredItem<Item> shittim_chest = ExtItem.register(namespacePrefix + "shittim_chest", BaCreativeTab.BA_KEY_ITEMS);

}