package ba.entries.entity.npc.citizens;

import ba.entries.entity.npc.citizens.trait.CustomerTrait;
import minecraft.entity.EntityRendererType;
import minecraft.entity.Humanoid;
import minecraft.entity.EntityInteractions.CombinedTask;
import minecraft.entity.mob.BaseMob;
import minecraft.extended.entity.GeneralHumanoidMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 模拟经营的顾客，原版模型
 */
public class HumanCustomer extends GeneralHumanoidMob {
	public static final String ID = "ba:npc_human_customer";

	public static final DeferredHolder<EntityType<?>, EntityType<HumanCustomer>> TYPE = newType(HumanCustomer.class, Humanoid.HUMANOID_WIDTH, Humanoid.HUMANOID_HEIGHT, ID, CustomerTrait.ATTRIBUTES);

	protected CombinedTask consumeItems;

	/**
	 * 所有Npc的子类都必须含有的构造函数。<br>
	 */
	public HumanCustomer(EntityType<BaseMob> entityType, EntityRendererType<GeneralHumanoidModelInfo> rendererType, Level level) {
		super(entityType, rendererType, level);
		this.addTrait(new CustomerTrait());
	}
}
