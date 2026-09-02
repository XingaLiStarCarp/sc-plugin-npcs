package minecraft.block;

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

import minecraft.core.Core;
import minecraft.core.registry.RegistryMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import sys.jvm.symbols;

public class ExBlockEntity {
	public static final RegistryMap<BlockEntityType<?>> BLOCK_ENTITIES = (RegistryMap<BlockEntityType<?>>) RegistryMap.of(Registries.BLOCK_ENTITY_TYPE);

	@SafeVarargs
	public static final <_T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<_T>> newType(Class<_T> blockEntityClazz, String name, Supplier<? extends Block>... blocks) {
		return BLOCK_ENTITIES.register(name, () -> {
			MethodHandle constructor = symbols.find_constructor(blockEntityClazz, BlockPos.class, BlockState.class);// 构造函数
			Block[] block_instances = new Block[blocks.length];
			for (int idx = 0; idx < blocks.length; ++idx) {
				block_instances[idx] = blocks[idx].get();
			}
			return BlockEntityType.Builder.of((BlockPos pos, BlockState state) -> {
				try {
					return (_T) constructor.invoke(pos, state);
				} catch (Throwable ex) {
					Core.logError("create BlockEntity of type '" + name + "' failed", ex);
					return null;
				}
			}, block_instances).build(null);
		});
	}
}