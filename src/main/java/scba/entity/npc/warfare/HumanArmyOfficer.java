package scba.entity.npc.warfare;

import minecraft.entity.EntityRendererType;
import minecraft.entity.Humanoid;
import minecraft.entity.mob.BaseMob;
import minecraft.extended.entity.GeneralHumanoidMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import scba.entity.npc.warfare.trait.ArmyOfficerTrait;

public class HumanArmyOfficer extends GeneralHumanoidMob {
	public static final String TYPE_NAME = "npc_human_army_officer";

	public static final RegistryObject<EntityType<HumanArmyOfficer>> TYPE = newType(HumanArmyOfficer.class, Humanoid.HUMANOID_WIDTH, Humanoid.HUMANOID_HEIGHT, TYPE_NAME, ArmyOfficerTrait.ATTRIBUTES);

	public HumanArmyOfficer(EntityType<BaseMob> entityType, EntityRendererType<GeneralHumanoidModelInfo> rendererType, Level level) {
		super(entityType, rendererType, level);
		this.addTrait(new ArmyOfficerTrait());
	}
}
