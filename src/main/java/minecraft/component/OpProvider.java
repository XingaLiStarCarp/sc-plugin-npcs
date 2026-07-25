package minecraft.component;

import java.util.ArrayList;
import java.util.HashMap;

public interface OpProvider {
	/**
	 * 行为组件
	 */
	public static final class OpComponent<_Target, _Param> {
		@FunctionalInterface
		public static interface Operation<_Target, _Param> {
			/**
			 * 执行一段动作
			 * 
			 * @param target 要操作的目标
			 * @param param  任意参数
			 * @return 是否继续遍历执行下一个操作
			 */
			public boolean operate(_Target target, _Param param);
		}

		private _Target target;
		private ArrayList<Operation<_Target, _Param>> ops;

		private OpComponent(_Target target) {
			this.target = target;
			this.ops = new ArrayList<>();
		}

		public void add(Operation<_Target, _Param> op) {
			ops.add(op);
		}

		public void remove(Operation<_Target, _Param> op) {
			ops.remove(op);
		}

		/**
		 * 执行全部行为
		 * 
		 * @param param
		 * @return 是否所有行为都执行成功
		 */
		public boolean execute(_Param param) {
			for (Operation<_Target, _Param> op : ops) {
				if (!op.operate(target, param))
					return false;
			}
			return true;
		}
	}

	public abstract HashMap<Class<?>, OpComponent<?, ?>> getOpComponents();

	/**
	 * 获取实体的行为组件，若不存在则新建一个对应的行为组件。
	 * 
	 * @param <_Param>
	 * @param paramClazz 参数类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default <_Param> OpComponent<?, _Param> getOpComponent(Class<_Param> paramClazz) {
		return (OpComponent<?, _Param>) getOpComponents().computeIfAbsent(paramClazz, (k) -> new OpComponent<>(this));
	}

	/**
	 * 执行行为
	 * 
	 * @param <_Param>
	 * @param paramClazz
	 * @return
	 */
	public default <_Param> boolean executeOpComponent(Class<_Param> paramClazz, _Param param) {
		return this.getOpComponent(paramClazz).execute(param);
	}
}
