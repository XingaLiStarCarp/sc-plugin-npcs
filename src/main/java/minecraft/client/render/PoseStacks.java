package minecraft.client.render;

import java.lang.reflect.Field;
import java.util.ArrayDeque;

import com.mojang.blaze3d.vertex.PoseStack;

import sys.jvm.reflection;
import sys.jvm.unsafe;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PoseStacks {
	private static final Field ArrayDeque_elements;
	private static final Field ArrayDeque_head;

	static {
		ArrayDeque_elements = reflection.find_field(ArrayDeque.class, "elements");
		ArrayDeque_head = reflection.find_field(ArrayDeque.class, "head");
	}

	/**
	 * 获取PoseStack内部的Pose
	 * 
	 * @param poseStack
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ArrayDeque<PoseStack.Pose> poseStackImpl(PoseStack poseStack) {
		return (ArrayDeque<PoseStack.Pose>) unsafe.read_member_reference(poseStack, "poseStack");
	}

	/**
	 * 在PoseStack顶层推入指定Pose
	 * 
	 * @param poseStack
	 * @param pose
	 */
	public static void pushPose(PoseStack poseStack, PoseStack.Pose pose) {
		poseStackImpl(poseStack).addLast(pose);
	}

	public static PoseStack.Pose clone(PoseStack.Pose pose) {
		PoseStack.Pose cloned = null;
		try {
			cloned = unsafe.allocate(PoseStack.Pose.class);
			unsafe.write_member(cloned, "pose", pose.pose().clone());
			unsafe.write_member(cloned, "normal", pose.normal().clone());
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
		}
		return cloned;
	}

	public static PoseStack clone(PoseStack poseStack) {
		PoseStack cloned = null;
		cloned = unsafe.allocate(PoseStack.class);// 分配内存
		ArrayDeque<PoseStack.Pose> innerPoseStack = poseStackImpl(poseStack).clone();// 拷贝ArrayDeque对象
		Object[] elements = (Object[]) unsafe.read_reference(innerPoseStack, ArrayDeque_elements);// 对ArrayDeque对象的内部数组进行深拷贝
		int head = (int) unsafe.read_reference(innerPoseStack, ArrayDeque_head);// 获取第一个元素的偏移量
		for (int i = 0; i < innerPoseStack.size(); ++i) {
			elements[head + i] = clone((PoseStack.Pose) (elements[head + i]));
		}
		unsafe.write_member(cloned, "poseStack", innerPoseStack);
		return cloned;
	}
}
