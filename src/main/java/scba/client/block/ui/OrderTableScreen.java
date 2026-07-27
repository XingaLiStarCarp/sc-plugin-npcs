package scba.client.block.ui;

import minecraft.client.graphics.ui.ExScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import scba.block.Blocks;
import scba.block.OrderTableEntityBlock;
import scba.block.OrderTableEntityBlock.OrderTableMenu;

public class OrderTableScreen extends AbstractContainerScreen<OrderTableMenu> implements ExScreen<OrderTableScreen> {
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
			price.setHint(Component.translatable(Blocks.ORDER_TABLE_HINT_LANG_KEY));
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

	static {
		// 将渲染的Screen与实际数据交互的Menu绑定
		ExScreen.register(OrderTableEntityBlock.MENU_TYPE, OrderTableMenu.class, OrderTableScreen.class, null);
	}
}