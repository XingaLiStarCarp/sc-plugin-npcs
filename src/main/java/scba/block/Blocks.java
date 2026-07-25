package scba.block;

import minecraft.block.ExBlockEntity;
import minecraft.client.graphics.ui.ExScreen;
import minecraft.client.graphics.ui.ExScreen.ExMenu;
import minecraft.registry.Registers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;
import scba.ModEntry;

public class Blocks {
	public static final String ORDER_TABLE_ID = "order_table";
	public static final String ORDER_TABLE_MENU_LANG_KEY = "gui." + ModEntry.MOD_ID + "." + ORDER_TABLE_ID;
	public static final String ORDER_TABLE_HINT_LANG_KEY = ORDER_TABLE_MENU_LANG_KEY + ".hint";
	public static final RegistryObject<Block> ORDER_TABLE = Registers.registerBlock(ORDER_TABLE_ID,
			() -> new BaseEntityBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)) {
				static class OrderTableMenu extends AbstractContainerMenu implements ExMenu<OrderTableMenu> {

					protected OrderTableMenu(MenuType<OrderTableMenu> type, int id, Inventory inv, FriendlyByteBuf buf) {
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

				static class OrderTableScreen extends AbstractContainerScreen<OrderTableMenu> implements ExScreen<OrderTableScreen> {
					private EditBox[] prices;

					public OrderTableScreen(OrderTableMenu menu, Inventory inv, Component title) {
						super(menu, inv, title);
						this.prices = new EditBox[3];
					}

					@Override
					protected void init() {
						super.init();
						for (int i = 0; i < prices.length; ++i) {
							EditBox price = new EditBox(
									this.font,
									this.leftPos + 87, // X坐标
									this.topPos + 15 + i * 18, // Y坐标
									57, // 宽度
									16, // 高度
									null);
							this.prices[i] = price;
							price.setMaxLength(10);
							price.setHint(Component.translatable(ORDER_TABLE_HINT_LANG_KEY));
							this.addRenderableWidget(price);
						}
					}

					@Override
					public void setSize(int x, int y, int width, int height) {
					}

					@Override
					protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
						this.renderCencteredTexture(g, ResourceLocation.parse("scba:textures/gui/order_table.png"), 176, 166, partialTick);
					}

					@Override
					public void renderScreen(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
						this.renderBackground(g);
						super.render(g, mouseX, mouseY, partialTick);
						this.renderTooltip(g, mouseX, mouseY);
					}

					@Override
					public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
						ExScreen.super.render(g, mouseX, mouseY, partialTick);
					}
				}

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
						return Component.translatable(ORDER_TABLE_MENU_LANG_KEY);
					}
				}

				public static final RegistryObject<MenuType<OrderTableMenu>> MENU_TYPE = ExMenu.newType(OrderTableMenu.class, OrderTableScreen.class, ORDER_TABLE_ID);

				public static final RegistryObject<BlockEntityType<OrderTableBlockEntity>> BLOCKENTITY_TYPE = ExBlockEntity.newType(OrderTableBlockEntity.class, ORDER_TABLE_ID, ORDER_TABLE);

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
			});

	public static final RegistryObject<Item> ORDER_TABLE_ITEM = Registers.registerBlockItem(ORDER_TABLE_ID, ORDER_TABLE);
}
