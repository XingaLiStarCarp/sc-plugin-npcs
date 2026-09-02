package minecraft.common;

import java.util.function.Consumer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import minecraft.client.render.SceneGraphNode;
import phys.MassPoint;

public class GameObject extends MassPoint {
	private SceneGraphNode renderable_node;
	private Consumer<Matrix4f> doTransform;

	public static final Vector3f ZP = new Vector3f(0, 0, 1);

	public GameObject(float mass, Vector3f initialPosition, Vector3f initialMomentum, float dt, SceneGraphNode renderable_node) {
		super(mass, initialPosition, initialMomentum, dt);
		this.renderable_node = renderable_node;
		renderable_node.setUpdate((SceneGraphNode node, float cam_x, float cam_y, float cam_z, float time) -> {
			Matrix4f transform = new Matrix4f();
			if (doTransform != null)
				doTransform.accept(transform);
			transform.translate(this.currentDisplacement());
			node.setTransform(transform);
		});
	}

	public final Consumer<Matrix4f> rotateToMomentum(Vector3f up_vec) {
		return (Matrix4f tarsform) -> tarsform.rotateTowards(this.currentMomentum(), up_vec);
	}

	public final Consumer<Matrix4f> zpRotateToMomentum(Vector3f up_vec) {
		return rotateToMomentum(ZP);
	}

	public void setDoTransform(Consumer<Matrix4f> doTransform) {
		this.doTransform = doTransform;
	}

	public void setVisible(boolean visible) {
		renderable_node.setVisible(visible);
	}
}
