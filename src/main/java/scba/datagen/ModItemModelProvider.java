package scba.datagen;

import minecraft.registry.Registers;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import scba.ModEntry;

public class ModItemModelProvider extends ItemModelProvider {
	public ModItemModelProvider(PackOutput output, ExistingFileHelper helper) {
		super(output, ModEntry.MOD_ID, helper);
	}

	/**
	 * 为方块物品生成模型
	 * 
	 * @param item
	 */
	public void blockItem(RegistryObject<Item> item) {
		ResourceLocation id = item.getId();
		getBuilder(id.toString())
				.parent(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath())));
	}

	@Override
	protected void registerModels() {

		for (RegistryObject<Item> item : Registers.registeredItems().values()) {
			// 生成默认的物品贴图模型
			if (BlockItem.class.isInstance(item.get())) {
				// 方块物品
				this.blockItem(item);
			} else {
				// 普通物品
				this.basicItem(item.getId());
			}
		}
	}
}
