package minecraft.item;

import java.util.ArrayList;
import java.util.Iterator;

import minecraft.core.registry.registries.bootstrap.BootstrapRegistries;
import minecraft.resources.ResourceLocations;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class Items {
	private static ArrayList<Item> allItems;

	public static ArrayList<Item> allItems() {
		if (allItems == null)
			allItems = Items.getAllItems();
		return allItems;
	}

	@SuppressWarnings("deprecation")
	public static String getID(Item item) {
		return item.builtInRegistryHolder().key().location().toString();
	}

	public static ArrayList<Item> getAllItems() {
		ArrayList<Item> items = new ArrayList<>();
		Iterator<Item> iter = BootstrapRegistries.ITEM.iterator();
		while (iter.hasNext())
			items.add((Item) iter.next());
		return items;
	}

	public static ItemDisplayContext parseItemDisplayContext(String context_str) {
		ItemDisplayContext context = null;
		switch (context_str) {
		case "thirdperson_lefthand":
			context = ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
			break;
		case "thirdperson_righthand":
			context = ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
			break;
		case "firstperson_lefthand":
			context = ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
			break;
		case "firstperson_righthand":
			context = ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
			break;
		case "head":
			context = ItemDisplayContext.HEAD;
			break;
		case "gui":
			context = ItemDisplayContext.GUI;
			break;
		case "ground":
			context = ItemDisplayContext.GROUND;
			break;
		case "fixed":
			context = ItemDisplayContext.FIXED;
			break;
		default:
			context = ItemDisplayContext.NONE;
			break;
		}
		return context;
	}

	public static final Item item(String type) {
		return item(ResourceLocations.build(type));
	}

	public static final Item item(ResourceLocation type) {
		return BuiltInRegistries.ITEM.get(type);
	}

	public static final ItemStack itemStack(String type) {
		return item(type).getDefaultInstance();
	}
}
