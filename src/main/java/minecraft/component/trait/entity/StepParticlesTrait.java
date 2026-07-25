package minecraft.component.trait.entity;

import minecraft.component.TraitProvider.TraitComponent;
import minecraft.entity.EntityInteractions;
import net.minecraft.world.entity.LivingEntity;

public class StepParticlesTrait implements TraitComponent<LivingEntity> {
	private double speedThreshold;
	private int interval;

	public static final int DEFAULT_PARTICLE_SPAWN_INTERVAL = 1;

	public StepParticlesTrait(double speedThreshold, int interval) {
		this.speedThreshold = speedThreshold;
		this.interval = interval;
	}

	public StepParticlesTrait(double speedThreshold) {
		this(speedThreshold, DEFAULT_PARTICLE_SPAWN_INTERVAL);
	}

	@Override
	public void tick(LivingEntity entity) {
		if (entity.level().isClientSide()) {
			EntityInteractions.spawnRunningGroundParticles(entity, interval, speedThreshold);// 奔跑时的粒子效果
		}
	}
}
