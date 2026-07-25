package scba.datagen;

import minecraft.registry.Registers;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import scba.ModEntry;

public class ModBlockStateProvider extends BlockStateProvider {
	public ModBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
		super(output, ModEntry.MOD_ID, helper);
	}

	@Override
	protected void registerStatesAndModels() {
		for (RegistryObject<Block> block : Registers.registeredBlocks().values()) {
			this.registerSimpleCubeBlockModel(block);
		}
	}

	/**
	 * 注册生成单一方块的六面纹理相同的正方体模型
	 * 
	 * @param block
	 */
	private void registerSimpleCubeBlockModel(RegistryObject<Block> block) {
		simpleBlock(block.get(), models().cubeAll(block.getId().getPath(), modLoc("block/" + block.getId().getPath())));
	}
}
