package minecraft.component.trait.entity;

import java.util.function.Function;

import minecraft.component.trait.ElementTrait;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 添加常驻的Goal的特性
 */
public class GoalTrait extends ElementTrait<Mob, GoalTrait.GoalEntry> {
	static class GoalEntry {
		private int priority;
		private Goal goal;
		private Function<Mob, Goal> goalCtor;

		public GoalEntry(int priority, Function<Mob, Goal> goalCtor) {
			this.priority = priority;
			this.goalCtor = goalCtor;
		}

		public int priority() {
			return priority;
		}

		public Goal goal(Mob mob) {
			return goal == null ? (goal = goalCtor.apply(mob)) : goal;
		}

		public Goal goal() {
			return goal;
		}
	}

	public GoalTrait add(int priority, Function<Mob, Goal> goalCtor) {
		return (GoalTrait) super.add(new GoalEntry(priority, goalCtor));
	}

	public GoalTrait add(int priority, Goal goal) {
		return this.add(priority, goal);
	}

	@Override
	public void init(Mob mob, GoalEntry entry) {
		Goal goal = entry.goal(mob);
		if (goal != null) {
			mob.goalSelector.addGoal(entry.priority(), goal);
		}
	}

	@Override
	public void uninit(Mob mob, GoalEntry entry) {
		Goal goal = entry.goal(mob);
		if (goal != null) {
			mob.goalSelector.removeGoal(entry.goal(mob));
		}
	}

	public final int getPriority(int idx) {
		return super.get(idx).priority();
	}

	public final Goal getGoal(int idx) {
		return super.get(idx).goal();
	}
}
