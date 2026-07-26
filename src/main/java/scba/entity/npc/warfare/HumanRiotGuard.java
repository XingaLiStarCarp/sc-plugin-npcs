package scba.entity.npc.warfare;

import minecraft.entity.EntityRendererType;
import minecraft.entity.Humanoid;
import minecraft.entity.mob.BaseMob;
import minecraft.extended.entity.GeneralHumanoidMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import scba.entity.npc.warfare.trait.RiotGuardTrait;

public class HumanRiotGuard extends GeneralHumanoidMob {
	public static final String TYPE_NAME = "npc_human_riot_guard";

	public static final RegistryObject<EntityType<HumanRiotGuard>> TYPE = newType(HumanRiotGuard.class, Humanoid.HUMANOID_WIDTH, Humanoid.HUMANOID_HEIGHT, TYPE_NAME, RiotGuardTrait.ATTRIBUTES);

	public HumanRiotGuard(EntityType<BaseMob> entityType, EntityRendererType<GeneralHumanoidModelInfo> rendererType, Level level) {
		super(entityType, rendererType, level);
		this.addTrait(new RiotGuardTrait());
	}
}
