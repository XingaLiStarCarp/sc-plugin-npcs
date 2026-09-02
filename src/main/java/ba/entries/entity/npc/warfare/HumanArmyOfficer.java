package ba.entries.entity.npc.warfare;

import ba.entries.entity.npc.warfare.trait.ArmyOfficerTrait;
import minecraft.entity.EntityRendererType;
import minecraft.entity.Humanoid;
import minecraft.entity.mob.BaseMob;
import minecraft.extended.entity.GeneralHumanoidMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

public class HumanArmyOfficer extends GeneralHumanoidMob {
	public static final String ID = "ba:npc_human_army_officer";

	public static final DeferredHolder<EntityType<?>, EntityType<HumanArmyOfficer>> TYPE = newType(HumanArmyOfficer.class, Humanoid.HUMANOID_WIDTH, Humanoid.HUMANOID_HEIGHT, ID, ArmyOfficerTrait.ATTRIBUTES);

	public HumanArmyOfficer(EntityType<BaseMob> entityType, EntityRendererType<GeneralHumanoidModelInfo> rendererType, Level level) {
		super(entityType, rendererType, level);
		this.addTrait(new ArmyOfficerTrait());
	}
}
