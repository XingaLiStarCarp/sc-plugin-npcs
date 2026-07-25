package minecraft.entity.goal.action;

import minecraft.entity.goal.DistanceBoundGoal;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;

public class UseItemGoal extends DistanceBoundGoal {

	protected InteractionHand hand;

	protected int useTime;
	private int useTimeCounter = 0;
	protected int useInterval;
	private int useIntervalCounter = 0;

	/**
	 * 当物品使用被打断时，是否重新开始本次使用
	 */
	protected boolean interruptReuse;
	private boolean usingItem = false;

	public static final int INFINITE_USE_TIME = -1;
	public static final int NO_USE_INTERVAL = 0;

	public UseItemGoal(Mob mob, InteractionHand hand, int useTime, int useInterval, boolean interruptReuse) {
		super(mob);
		this.hand = hand;
		this.useTime = useTime;
		this.useInterval = useInterval;
		this.interruptReuse = interruptReuse;
	}

	public UseItemGoal(Mob mob, InteractionHand hand, boolean interruptReuse) {
		this(mob, hand, INFINITE_USE_TIME, NO_USE_INTERVAL, interruptReuse);
	}

	public UseItemGoal(Mob mob, int useTime, int useInterval, boolean interruptReuse) {
		this(mob, InteractionHand.OFF_HAND, useTime, useInterval, interruptReuse);
	}

	public UseItemGoal(Mob mob, boolean interruptReuse) {
		this(mob, INFINITE_USE_TIME, NO_USE_INTERVAL, interruptReuse);
	}

	public void startUsingItem(boolean force) {
		if (!usingItem || force) {
			mob.startUsingItem(hand);
			useTimeCounter = 0;// 重置使用时间计数
			useIntervalCounter = 0;
			usingItem = true;
		}
	}

	public void stopUsingItem(boolean force) {
		if (usingItem || force) {
			mob.stopUsingItem();
			useTimeCounter = 0;
			usingItem = false;
		}
	}

	@Override
	public void enter() {
		if (useTime <= 0) {
			this.startUsingItem(true);// 使用时间小于等于0时，视为永久使用直到该Goal执行结束
			return;
		}
	}

	/**
	 * 检查使用是否被打断，如果打断则重新使用
	 */
	protected void checkAndReuse() {
		if (interruptReuse && usingItem != mob.isUsingItem()) {
			this.startUsingItem(true);
		}
	}

	@Override
	public void update() {
		this.checkAndReuse();
		if (useTime > 0) {
			if (useTimeCounter <= 0) {
				this.startUsingItem(false);
			}
			if (useTimeCounter < useTime) {
				++useTimeCounter;
			} else {
				// 当物品使用时间达到阈值useTime
				if (useIntervalCounter <= 0) {
					this.stopUsingItem(false);
				} else {
					++useIntervalCounter;
				}
				// 判断是否冷却结束
				if (useIntervalCounter >= useInterval) {
					this.startUsingItem(false);
				}
			}
		}
	}

	@Override
	public void exit() {
		this.stopUsingItem(true);
		useTimeCounter = 0;
		useIntervalCounter = 0;
	}
}
