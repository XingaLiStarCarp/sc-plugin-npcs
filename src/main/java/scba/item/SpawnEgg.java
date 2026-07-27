package scba.item;

import minecraft.registry.Registers;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.RegistryObject;
import scba.entity.npc.warfare.HumanArmyOfficer;

public class SpawnEgg {
    public static final RegistryObject<Item> HUMAN_ARMY_OFFICER_SPAWN_EGG = Registers.registerItem(
            "human_army_officer_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    HumanArmyOfficer.TYPE,
                    0x3B3B3B,
                    0x8B0000,
                    new Item.Properties()
            )
    );
}
