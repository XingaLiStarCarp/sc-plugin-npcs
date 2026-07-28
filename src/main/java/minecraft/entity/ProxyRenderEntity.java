package minecraft.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import minecraft.entity.data.EntityData;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * 渲染位姿同步的虚假实体
 */
public interface ProxyRenderEntity<_RenderingEntity extends Entity, _Model> {
	/**
	 * 获取渲染的虚假实体。<br>
	 * 该虚假实体需要通过blankRenderingEntity()方法创建。<br>
	 * 
	 * @return
	 */
	public abstract _RenderingEntity renderingEntity();

	/**
	 * 渲染时要绑定的实体数据，包括其位置信息、角度、血量等。<br>
	 * 
	 * @return
	 */
	public abstract Entity bindEntity();

	/**
	 * 获取实体数据<br>
	 * 
	 * @return
	 */
	public default SynchedEntityData entityData() {
		return bindEntity().getEntityData();
	}

	/**
	 * 该渲染模型绑定的实体是否被移除，被移除的实体不能再同步数据。<br>
	 * 在实体外部执行数据同步前必须判断，如果在实体内部tick()方法内同步数据则无需判断。<br>
	 * 
	 * @return
	 */
	public default boolean isEntityRemoved() {
		return bindEntity().isRemoved();
	}

	/**
	 * 同步绑定的的实体数据到renderingEntity，包括渲染使用到的实体相关数据。<br>
	 * 该方法必须被子类调用以实时同步渲染模型，每tick调用一次即可。<br>
	 */
	public default void syncRenderingEntity() {
		// 仅客户端同步
		if (bindEntity().level().isClientSide()) {
			this.syncRenderingEntityData();
		}
	}

	public default _RenderingEntity syncRenderingEntityData() {
		Entity bindEntity = bindEntity();
		// 服务端无虚假实体，直接返回，不需要同步
		if (bindEntity.level().isClientSide()) {
			_RenderingEntity renderingEntity = renderingEntity();
			EntityData.syncEntityData(bindEntity, renderingEntity);// 同步内存字段值
			return renderingEntity;
		}
		return null;
	}

	/**
	 * 当前使用的渲染模型资产。<br>
	 * 如果本方法实现中返回的是资产的拷贝对象，则避免频繁调用此方法，仅作为查询使用。<br>
	 * 
	 * @return
	 */
	public abstract _Model modelAsset();

	/**
	 * 传入一个待绑定的实体，构造一个新的仅用于渲染的虚假实体
	 * 
	 * @param bindEntity
	 * @return
	 */
	public abstract _RenderingEntity blankRenderingEntity(Entity bindEntity);

	/**
	 * 解除绑定，接触后实体数据不再同步
	 * 
	 * @param bindEntity
	 */
	public static void unbind(Entity bindEntity) {
		ModelBinder.unbind(bindEntity);
	}

	/**
	 * 绑定实体的渲染模型
	 */
	@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.FORGE)
	public static abstract class ModelBinder<_RenderingEntity extends Entity, _Model> implements ProxyRenderEntity<_RenderingEntity, _Model> {
		protected final Entity bindEntity;
		protected final _Model model;

		protected final _RenderingEntity renderingEntity;

		@Override
		public final _RenderingEntity renderingEntity() {
			return renderingEntity;
		}

		@Override
		public final Entity bindEntity() {
			return bindEntity;
		}

		@Override
		public final boolean isEntityRemoved() {
			return bindEntity.isRemoved();
		}

		private static final HashMap<Entity, ModelBinder<? extends Entity, ?>> BINDERS = new HashMap<>();

		@SuppressWarnings("unchecked")
		protected static final <_RenderingEntity extends Entity, _Model, _Binder extends ModelBinder<_RenderingEntity, _Model>> _Binder bind(Entity bindEntity, Function<Entity, _Binder> binderProvider) {
			return (_Binder) BINDERS.computeIfAbsent(bindEntity, binderProvider);
		}

		protected static final <_RenderingEntity extends Entity, _Model, _Binder extends ModelBinder<_RenderingEntity, _Model>> _Binder bind(BiFunction<Entity, _Model, _Binder> binderCtor, Entity bindEntity, _Model model) {
			return bind(bindEntity, (newEntity) -> binderCtor.apply(newEntity, model));
		}

		protected static final <_RenderingEntity extends Entity, _Model, _Binder extends ModelBinder<_RenderingEntity, _Model>> _Binder bind(Entity bindEntity, Function<Entity, _Model> modelProvider, BiFunction<Entity, _Model, _Binder> binderCtor) {
			return bind(binderCtor, bindEntity, modelProvider.apply(bindEntity));
		}

		protected static final <_RenderingEntity extends Entity, _Model, _Binder extends ModelBinder<_RenderingEntity, _Model>> _Binder bind(Entity bindEntity, Supplier<_Model> modelProvider, BiConsumer<Entity, _Model> modelResolver, BiFunction<Entity, _Model, _Binder> binderCtor) {
			return bind(bindEntity, (newEntity) -> {
				_Model model = modelProvider.get();
				modelResolver.accept(bindEntity, model);
				return model;
			}, binderCtor);
		}

		protected static final void unbind(Entity bindEntity) {
			BINDERS.remove(bindEntity);
		}

		/**
		 * 将实体绑定一个模型
		 * 
		 * @param bindEntity 绑定的实体
		 * @param model      绑定的模型，模型必须预先初始化好，构造完成后会立即同步模型数据。<br>
		 *                   如果不初始化模型，那么在下一次tick()同步实体数据之前，绑定的实体会渲染为默认模型。<br>
		 */
		protected ModelBinder(Entity bindEntity, _Model model) {
			this.bindEntity = bindEntity;
			this.renderingEntity = this.blankRenderingEntity(bindEntity);
			this.model = model;// 如果渲染时用到了此参数，要注意此参数必须非null
			this.syncRenderingEntity();// 初始化时先同步一次，不能等到下一次tick，否则可能在此期间出现渲染错误
		}

		/**
		 * 每tick自动同步实体数据
		 * 
		 * @param event
		 */
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event) {
			if (event.phase == TickEvent.Phase.END) {
				Iterator<Entry<Entity, ModelBinder<? extends Entity, ?>>> iter = BINDERS.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Entity, ModelBinder<? extends Entity, ?>> entry = iter.next();
					ModelBinder<? extends Entity, ?> dummy = entry.getValue();
					if (dummy.isEntityRemoved()) {
						iter.remove(); // 绑定的实体被移除时，从tick数据同步列表中移除该项
					} else {
						dummy.syncRenderingEntity();
					}
				}
			}
		}

		public final _Model getModel() {
			return model;
		}
	}
}
