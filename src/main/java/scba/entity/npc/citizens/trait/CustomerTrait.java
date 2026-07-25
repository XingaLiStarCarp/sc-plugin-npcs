package scba.entity.npc.citizens.trait;

import java.util.function.Supplier;

import minecraft.component.trait.MultiTrait;
import minecraft.component.trait.entity.ItemConsumeTrait;
import minecraft.entity.mob.BaseMob;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class CustomerTrait extends MultiTrait {
	public static final Supplier<AttributeSupplier> ATTRIBUTES = () -> Mob.createMobAttributes().build();

	public CustomerTrait() {
		super();

		this.add(new ItemConsumeTrait<BaseMob>() {

			@Override
			public void onSuccess(PlayerInteractEvent.EntityInteract event, Player player, BaseMob mob, InteractionHand hand) {
				mob.chat(player, "success");
			}

		});
	}

}
