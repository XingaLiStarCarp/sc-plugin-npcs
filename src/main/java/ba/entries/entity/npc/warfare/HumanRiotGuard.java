package ba.entries.entity.npc.warfare;

import ba.entries.entity.npc.warfare.trait.RiotGuardTrait;
import minecraft.entity.EntityRendererType;
import minecraft.entity.Humanoid;
import minecraft.entity.mob.BaseMob;
import minecraft.extended.entity.GeneralHumanoidMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

public class HumanRiotGuard extends GeneralHumanoidMob {
	public static final String ID = "ba:npc_human_riot_guard";

	public static final DeferredHolder<EntityType<?>, EntityType<HumanRiotGuard>> TYPE = newType(HumanRiotGuard.class, Humanoid.HUMANOID_WIDTH, Humanoid.HUMANOID_HEIGHT, ID, RiotGuardTrait.ATTRIBUTES);

	public HumanRiotGuard(EntityType<BaseMob> entityType, EntityRendererType<GeneralHumanoidModelInfo> rendererType, Level level) {
		super(entityType, rendererType, level);
		this.addTrait(new RiotGuardTrait());
	}
}
