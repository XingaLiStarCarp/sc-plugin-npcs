package minecraft.component.trait;

import minecraft.component.OpProvider;
import minecraft.component.OpProvider.OpComponent;
import minecraft.component.OpProvider.OpComponent.Operation;
import minecraft.component.TraitProvider.TraitComponent;

public abstract class OpTrait<_Target extends OpProvider, _Param, _DerivedTrait extends OpTrait<_Target, _Param, _DerivedTrait>> implements TraitComponent<_Target> {
	private OpComponent<_Target, _Param> opComponent;
	private Operation<_Target, _Param> op;
	private Class<_Param> paramClazz;

	public OpTrait(Class<_Param> paramClazz) {
		this.paramClazz = paramClazz;
		this.op = (target, param) -> this.operate(target, param);
	}

	protected abstract boolean operate(_Target target, _Param param);

	@Override
	@SuppressWarnings("unchecked")
	public void init(_Target target) {
		this.opComponent = (OpComponent<_Target, _Param>) target.getOpComponent(paramClazz);
		opComponent.add(op);
	}

	@Override
	public void uninit(_Target target) {
		opComponent.remove(op);
	}
}