package ba.entries.entity.npc.warfare;

import ba.entries.entity.npc.warfare.trait.SniperTrait;
import minecraft.entity.EntityRendererType;
import minecraft.entity.Humanoid;
import minecraft.entity.mob.BaseMob;
import minecraft.extended.entity.GeneralHumanoidMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

public class HumanSniper extends GeneralHumanoidMob {
	public static final String ID = "ba:npc_human_sniper";

	public static final DeferredHolder<EntityType<?>, EntityType<HumanSniper>> TYPE = newType(HumanSniper.class, Humanoid.HUMANOID_WIDTH, Humanoid.HUMANOID_HEIGHT, ID, SniperTrait.ATTRIBUTES);

	public HumanSniper(EntityType<BaseMob> entityType, EntityRendererType<GeneralHumanoidModelInfo> rendererType, Level level) {
		super(entityType, rendererType, level);
		this.addTrait(new SniperTrait());
	}
}
