package ba.entries.block;

import com.mojang.serialization.MapCodec;

import minecraft.block.ExBlockEntity;
import minecraft.ui.ExMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.registries.DeferredHolder;

public class OrderTableEntityBlock extends BaseEntityBlock {

	public OrderTableEntityBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
	}

	public static class OrderTableMenu extends AbstractContainerMenu implements ExMenu<OrderTableMenu> {
		public OrderTableMenu(MenuType<OrderTableMenu> type, int id, Inventory inv, FriendlyByteBuf buf) {
			super(type, id);
			this.initPlayerHotbar(inv);
			this.initPlayerInventory(inv);
		}

		@Override
		public ItemStack quickMoveStack(Player player, int slot) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean stillValid(Player player) {
			return true;
		}
	}

	// 数据交互对象
	static class OrderTableBlockEntity extends BlockEntity implements MenuProvider {

		public OrderTableBlockEntity(BlockPos pos, BlockState state) {
			super(BLOCKENTITY_TYPE.get(), pos, state);
		}

		@Override
		public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
			return new OrderTableMenu(MENU_TYPE.get(), id, inv, null);
		}

		@Override
		public Component getDisplayName() {
			return Component.translatable(Blocks.ORDER_TABLE_MENU_LANG_KEY);
		}
	}

	// 注册数据交互对象Menu，负责显示的Screen必须在客户端才注册
	public static final DeferredHolder<MenuType<?>, MenuType<OrderTableMenu>> MENU_TYPE = ExMenu.newType(OrderTableMenu.class, Blocks.ORDER_TABLE_ID);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OrderTableBlockEntity>> BLOCKENTITY_TYPE = ExBlockEntity.newType(OrderTableBlockEntity.class, Blocks.ORDER_TABLE_ID, Blocks.ORDER_TABLE);

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new OrderTableBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}

	/**
	 * 当玩家空手或使用非物品交互时的处理
	 * 在 NeoForge 1.21.1 中，这是 use 方法的替代
	 */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide) {
			player.openMenu(state.getMenuProvider(level, pos));
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	/**
	 * 当玩家使用物品交互时的处理
	 * 在 NeoForge 1.21.1 中，这是原有的 use 方法替代
	 */
	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!level.isClientSide) {
			player.openMenu(state.getMenuProvider(level, pos));
		}
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		// TODO Auto-generated method stub
		return null;
	}
}