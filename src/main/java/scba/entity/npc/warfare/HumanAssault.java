package scba.entity.npc.warfare;

import minecraft.entity.EntityRendererType;
import minecraft.entity.Humanoid;
import minecraft.entity.mob.BaseMob;
import minecraft.extended.entity.GeneralHumanoidMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import scba.entity.npc.warfare.trait.AssaultTrait;

public class HumanAssault extends GeneralHumanoidMob {
	public static final String TYPE_NAME = "npc_human_assault";

	public static final RegistryObject<EntityType<HumanAssault>> TYPE = newType(HumanAssault.class, Humanoid.HUMANOID_WIDTH, Humanoid.HUMANOID_HEIGHT, TYPE_NAME, AssaultTrait.ATTRIBUTES);

	public HumanAssault(EntityType<BaseMob> entityType, EntityRendererType<GeneralHumanoidModelInfo> rendererType, Level level) {
		super(entityType, rendererType, level);
		this.addTrait(new AssaultTrait());
	}
}
