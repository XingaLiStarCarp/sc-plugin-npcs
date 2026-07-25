package minecraft.block;

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

import jvmsp.symbols;
import minecraft.registry.Registers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import scba.ModEntry;

public class ExBlockEntity {
	@SafeVarargs
	public static final <_T extends BlockEntity> RegistryObject<BlockEntityType<_T>> newType(Class<_T> blockEntityClazz, String name, Supplier<? extends Block>... blocks) {
		return Registers.BLOCK_ENTITIES_REG.register(name, () -> {
			MethodHandle constructor = symbols.find_constructor(blockEntityClazz, BlockPos.class, BlockState.class);// 构造函数
			Block[] block_instances = new Block[blocks.length];
			for (int idx = 0; idx < blocks.length; ++idx) {
				block_instances[idx] = blocks[idx].get();
			}
			return BlockEntityType.Builder.of((BlockPos pos, BlockState state) -> {
				try {
					return (_T) constructor.invoke(pos, state);
				} catch (Throwable ex) {
					ModEntry.LOGGER.error("create BlockEntity of type '" + name + "' failed", ex);
					return null;
				}
			}, block_instances).build(null);
		});
	}
}
