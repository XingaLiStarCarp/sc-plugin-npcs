package scba.block;

import minecraft.block.ExBlockEntity;
import minecraft.ui.ExMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraftforge.registries.RegistryObject;

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
	public static final RegistryObject<MenuType<OrderTableMenu>> MENU_TYPE = ExMenu.newType(OrderTableMenu.class, Blocks.ORDER_TABLE_ID);

	public static final RegistryObject<BlockEntityType<OrderTableBlockEntity>> BLOCKENTITY_TYPE = ExBlockEntity.newType(OrderTableBlockEntity.class, Blocks.ORDER_TABLE_ID, Blocks.ORDER_TABLE);

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new OrderTableBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}

	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!level.isClientSide) {
			player.openMenu(this.getMenuProvider(state, level, pos));
		}
		return super.use(state, level, pos, player, hand, hit);
	}
}