package minecraft.component.trait;

import java.util.ArrayList;
import java.util.function.Function;

import minecraft.component.TraitProvider.TraitComponent;

public abstract class ElementTrait<_TraitTarget, _Element> implements TraitComponent<_TraitTarget> {
	private final ArrayList<Function<_TraitTarget, _Element>> elementEntriess;
	private final ArrayList<_Element> elements;

	public ElementTrait() {
		elementEntriess = new ArrayList<>();
		elements = new ArrayList<>();
	}

	public ElementTrait<_TraitTarget, _Element> add(Function<_TraitTarget, _Element> elementCtor) {
		elementEntriess.add(elementCtor);
		return this;
	}

	public ElementTrait<_TraitTarget, _Element> add(_Element element) {
		return add((target) -> element);
	}

	public final _Element get(int idx) {
		return elements.get(idx);
	}

	@Override
	public final void init(_TraitTarget target) {
		elements.clear();
		for (Function<_TraitTarget, _Element> entry : elementEntriess) {
			_Element element = entry.apply(target);
			elements.add(element);
			this.init(target, element);
		}
	}

	protected abstract void init(_TraitTarget target, _Element element);

	@Override
	public final void uninit(_TraitTarget target) {
		for (_Element element : elements) {
			this.uninit(target, element);
		}
		elements.clear();
	}

	protected abstract void uninit(_TraitTarget target, _Element element);
}
