package minecraft.component;

import java.util.ArrayList;

public interface TraitProvider {
	/**
	 * 特征组件，所有种类的行为组件都必须实现此接口
	 */
	public static interface TraitComponent<_TraitTarget> {
		public default void init(_TraitTarget target) {

		}

		public default void uninit(_TraitTarget target) {

		}

		public default void tick(_TraitTarget target) {

		}
	}

	public abstract ArrayList<TraitComponent<?>> getTraits();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public default void addTrait(TraitComponent<?> trait) {
		getTraits().add(trait);
		((TraitComponent) trait).init(this);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public default void removeTrait(TraitComponent<?> trait) {
		((TraitComponent) trait).uninit(this);
		getTraits().remove(trait);
	}

	/**
	 * 子类必须每tick调用此方法更新
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public default void tickTraits() {
		ArrayList<TraitComponent<?>> traits = this.getTraits();
		for (TraitComponent<?> trait : traits) {
			((TraitComponent) trait).tick(this);
		}
	}
}