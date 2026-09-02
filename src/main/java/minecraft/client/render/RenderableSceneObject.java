package minecraft.client.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.math.Axis;

import javabase.Node;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 可渲染物体<br>
 */
@OnlyIn(Dist.CLIENT)
public class RenderableSceneObject extends SceneGraphNode {
	public static class ViewPos {
		@FunctionalInterface
		public static interface NumericalCalculation {
			/**
			 * x、z坐标差，y为物体渲染相对高度<br>
			 * (近地物体坐标-玩家坐标)=offset<br>
			 * 
			 * @param object_x
			 * @param dx
			 * @param object_y
			 * @param dy
			 * @param object_z
			 * @param dz
			 * @return x、y、z方向的渲染坐标
			 */
			public float calc(float object_x, float dx, float object_y, float dy, float object_z, float dz);

			/**
			 * 固定坐标差
			 * 
			 * @param value
			 * @return
			 */
			public static NumericalCalculation fixed(float value) {
				return (float object_x, float dx, float object_y, float dy, float object_z, float dz) -> value;
			}

			public static final NumericalCalculation X = (float object_x, float dx, float object_y, float dy, float object_z, float dz) -> object_x;

			/**
			 * 物体渲染位置总是位于玩家视角固定高度处
			 */
			public static final NumericalCalculation Y = (float object_x, float dx, float object_y, float dy, float object_z, float dz) -> object_y;

			public static final NumericalCalculation Z = (float object_x, float dx, float object_y, float dy, float object_z, float dz) -> object_z;

			public static final NumericalCalculation DX = (float object_x, float dx, float object_y, float dy, float object_z, float dz) -> dx;

			/**
			 * 物体渲染位置为玩家与物体高度之差
			 */
			public static final NumericalCalculation DY = (float object_x, float dx, float object_y, float dy, float object_z, float dz) -> dy;

			public static final NumericalCalculation DZ = (float object_x, float dx, float object_y, float dy, float object_z, float dz) -> dz;

			/**
			 * Y轴线性插值衰减函数，
			 * 
			 * @param fromDy    开始插值时物体与相机的坐标高度差
			 * @param fromValue 相机与物体高度差大于等于fromDy时采用的渲染高度差
			 * @param toDy      物体与相机的坐标高度差
			 * @param toValue   相机与物体高度差小于等于toDy时采用的渲染高度差
			 * @return
			 */
			public static NumericalCalculation yLerpDecayTo(float fromDy, float fromValue, float toDy, float toValue) {
				return new NumericalCalculation() {
					float k = (toValue - fromValue) / (toDy - fromDy);

					@Override
					public float calc(float object_x, float dx, float object_y, float dy, float object_z, float dz) {
						if (dy >= fromDy)
							return fromValue;
						else if (dy <= toDy)
							return toValue;
						else
							return fromValue + k * (dy - fromDy);
					}

					@Override
					public String toString() {
						return "NumericalCalculation::yLerpDecayTo[k=" + k + "]";
					}
				};
			}
		}

		/**
		 * 物体坐标，位于这个坐标时物体在90°天顶
		 */
		public final float object_x;

		/**
		 * 物体高度坐标
		 */
		public final float object_y;
		public final float object_z;
		/**
		 * 方块坐标与渲染坐标的比例
		 */
		public final NumericalCalculation x_view_offset;
		/**
		 * 物体相对相机的高度
		 */
		public final NumericalCalculation y_view_height;
		public final NumericalCalculation z_view_offset;

		public static final NumericalCalculation DEFAULT_X_VIEW_OFFSET = NumericalCalculation.DX;
		public static final NumericalCalculation DEFAULT_Z_VIEW_OFFSET = NumericalCalculation.DZ;
		public static final NumericalCalculation DEFAULT_Y_VIEW_HEIGHT = NumericalCalculation.DY;

		private ViewPos(float object_x, float object_y, float object_z, NumericalCalculation x_view_offset, NumericalCalculation y_view_height, NumericalCalculation z_offset_decay) {
			this.object_x = object_x;
			this.object_y = object_y;
			this.object_z = object_z;
			this.x_view_offset = x_view_offset;
			this.y_view_height = y_view_height;
			this.z_view_offset = z_offset_decay;
		}

		public static ViewPos of(float object_x, float object_y, float object_z, NumericalCalculation x_view_offset, NumericalCalculation y_view_height, NumericalCalculation z_offset_decay) {
			return new ViewPos(object_x, object_y, object_z, x_view_offset, y_view_height, z_offset_decay);
		}

		public static ViewPos of(float object_x, float object_y, float object_z, NumericalCalculation y_view_height) {
			return of(object_x, object_y, object_z, DEFAULT_X_VIEW_OFFSET, y_view_height, DEFAULT_Z_VIEW_OFFSET);
		}

		public static ViewPos of(float object_x, float object_y, float object_z) {
			return of(object_x, object_y, object_z, DEFAULT_Y_VIEW_HEIGHT);
		}
	}

	@FunctionalInterface
	public static interface Spin {
		/**
		 * 实时计算自旋角速度
		 * 
		 * @param time tick为单位
		 * @return
		 */
		public abstract float angularSpeed(float time);

		public static final Spin ZERO = (float time) -> 0;

		/**
		 * 固定自旋角速度
		 * 
		 * @param omega
		 * @return
		 */
		public static Spin fixed(float omega) {
			return (float time) -> omega;
		}
	}

	/**
	 * 轨道
	 */
	@FunctionalInterface
	public static interface Orbit {
		/**
		 * 实时计算轨道
		 * 
		 * @param time tick为单位
		 * @return
		 */
		public abstract ViewPos position(float time);

		/**
		 * 固定点
		 * 
		 * @param object_x
		 * @param object_y
		 * @param object_z
		 * @return
		 */
		public static Orbit fixed(float object_x, float object_y, float object_z) {
			return (float time) -> ViewPos.of(object_x, object_y, object_z);
		}

		public static Orbit fixed(float object_x, float object_y, float object_z, ViewPos.NumericalCalculation y_view_height) {
			return (float time) -> ViewPos.of(object_x, object_y, object_z, y_view_height);
		}

		/**
		 * 固定高度正圆形轨道
		 * 
		 * @param center_x
		 * @param center_y
		 * @param center_z
		 * @param radius
		 * @param angular_speed 角速度
		 * @param initial_phase 初始相位
		 * @return
		 */
		public static Orbit circle(float center_x, float center_y, float center_z, float radius, float angular_speed, float initial_phase) {
			return (float time) -> {
				float phase = initial_phase + angular_speed * time;
				return ViewPos.of(center_x + (float) Math.sin(phase) * radius, center_y, center_z + (float) Math.cos(phase) * radius);
			};
		}

		public static Orbit circle(float center_x, float center_y, float center_z, float radius, float angular_speed, float initial_phase, ViewPos.NumericalCalculation y_view_height) {
			return (float time) -> {
				float phase = initial_phase + angular_speed * time;
				return ViewPos.of(center_x + (float) Math.sin(phase) * radius, center_y, center_z + (float) Math.cos(phase) * radius, y_view_height);
			};
		}

		/**
		 * 圆心在其他轨道上的圆形轨道
		 * 
		 * @param center_orbit  圆心所处轨道，该轨道只取x、z值
		 * @param center_height
		 * @param radius
		 * @param angular_speed
		 * @param initial_phase
		 * @return
		 */
		public static Orbit circle(Orbit center_orbit, float radius, float angular_speed, float initial_phase) {
			return (float time) -> {
				float phase = initial_phase + angular_speed * time;
				ViewPos center = center_orbit.position(time);
				return ViewPos.of(center.object_x + (float) Math.sin(phase) * radius, center.object_y, center.object_z + (float) Math.cos(phase) * radius);
			};
		}

		public static Orbit circle(Orbit center_orbit, float radius, float angular_speed) {
			return circle(center_orbit, radius, angular_speed, 0);
		}

		public static Orbit circle(Orbit center_orbit, float radius, float angular_speed, float initial_phase, ViewPos.NumericalCalculation y_view_height) {
			return (float time) -> {
				float phase = initial_phase + angular_speed * time;
				ViewPos center = center_orbit.position(time);
				return ViewPos.of(center.object_x + (float) Math.sin(phase) * radius, center.object_y, center.object_z + (float) Math.cos(phase) * radius, y_view_height);
			};
		}

		public static Orbit circle(Orbit center_orbit, float radius, float angular_speed, ViewPos.NumericalCalculation y_view_height) {
			return circle(center_orbit, radius, angular_speed, 0, y_view_height);
		}

		/**
		 * @param center_x
		 * @param center_z
		 * @param radius
		 * @param view_height
		 * @param angular_speed 单位rad/tick
		 * @return
		 */
		public static Orbit circle(float center_x, float center_height, float center_z, float radius, float angular_speed) {
			return circle(center_x, center_height, center_z, radius, angular_speed, 0);
		}

		public static Orbit circle(float center_x, float center_height, float center_z, float radius, float angular_speed, ViewPos.NumericalCalculation y_view_height) {
			return circle(center_x, center_height, center_z, radius, angular_speed, 0, y_view_height);
		}
	}

	/**
	 * 轨道决定任一时刻的ViewPos
	 */
	private Orbit orbit;

	private Spin spin = Spin.ZERO;

	private UpdateOperation preRenderOperation;

	@Override
	public RenderableSceneObject setUpdate(UpdateOperation preRenderOperation) {
		this.preRenderOperation = preRenderOperation;
		return this;
	}

	private Matrix4f sub_transform = new Matrix4f();// 默认恒等矩阵

	/**
	 * 设置视角变换前的子变换
	 */
	@Override
	public RenderableSceneObject setTransform(Matrix4f sub_transform) {
		this.sub_transform = sub_transform;
		return this;
	}

	protected RenderableSceneObject(String name, Node<String, VanillaRenderable.Instance> parent) {
		super(name, parent);
	}

	private static final UpdateOperation VIEW_POS_TRANSFORM_FUNC = (SceneGraphNode dest_node, float cam_x, float cam_y, float cam_z, float t) -> {
		RenderableSceneObject object = (RenderableSceneObject) dest_node;
		Matrix4f view_offset = new Matrix4f(object.sub_transform);// 每帧构造新的变换矩阵
		view_offset.rotate(Axis.YN.rotation(object.spin.angularSpeed(t)));
		if (object.orbit != null) {
			ViewPos pos = object.orbit.position(t);// 计算轨道坐标
			float offset_x = pos.object_x - cam_x;
			float offset_z = pos.object_z - cam_y;
			float offset_y = pos.object_y - cam_z;
			view_offset.translate(new Vector3f(
					pos.x_view_offset.calc(pos.object_x, offset_x, pos.object_y, offset_y, pos.object_z, offset_z),
					pos.y_view_height.calc(pos.object_x, offset_x, pos.object_y, offset_y, pos.object_z, offset_z),
					pos.z_view_offset.calc(pos.object_x, offset_x, pos.object_y, offset_y, pos.object_z, offset_z)));
		}
		dest_node.setTransform(view_offset);
		if (object.preRenderOperation != null) {
			object.preRenderOperation.update(dest_node, cam_x, cam_y, cam_z, t);
		}
	};

	protected RenderableSceneObject(String path, Orbit orbit) {
		super(path);
		this.orbit = orbit;
		super.setUpdate(VIEW_POS_TRANSFORM_FUNC);
	}

	protected RenderableSceneObject(String name, VanillaRenderable.Instance renderable, Orbit orbit) {
		super(name, renderable);
		this.orbit = orbit;
		super.setUpdate(VIEW_POS_TRANSFORM_FUNC);
	}

	protected RenderableSceneObject(String name, Node<String, VanillaRenderable.Instance> parent, Orbit orbit) {
		super(name, parent);
		this.orbit = orbit;
		super.setUpdate(VIEW_POS_TRANSFORM_FUNC);
	}

	public final RenderableSceneObject setOrbit(Orbit orbit) {
		this.orbit = orbit;
		return this;
	}

	public final RenderableSceneObject setSpin(Spin spin) {
		this.spin = spin;
		return this;
	}

	public final RenderableSceneObject setOrbit(float object_x, float object_y, float object_z) {
		return setOrbit(Orbit.fixed(object_x, object_y, object_z));
	}

	public final RenderableSceneObject setOrbit(float object_x, float object_y, float object_z, ViewPos.NumericalCalculation y_view_height) {
		return setOrbit(Orbit.fixed(object_x, object_y, object_z, y_view_height));
	}

	public static final RenderableSceneObject createRenderableNode(SceneGraphNode root_node, String path, VanillaRenderable.Instance renderable, RenderableSceneObject.Orbit orbit) {
		return (RenderableSceneObject) root_node.findChildNode(parsePath(path), true, (String name, Node<String, VanillaRenderable.Instance> parent) -> {
			return new RenderableSceneObject(name, parent, orbit).setValue(VanillaRenderable.Instance.empty(true));// 默认节点可渲染对象为null
		});
	}

	public static final RenderableSceneObject createRenderableNode(SceneGraphNode root_node, String path, VanillaRenderable renderable, RenderableSceneObject.Orbit orbit) {
		return createRenderableNode(root_node, path, renderable.newInstance(), orbit);
	}

	public static final RenderableSceneObject createRenderableNode(SceneGraphNode root_node, String path, VanillaRenderable renderable) {
		return createRenderableNode(root_node, path, renderable, null);
	}

}
