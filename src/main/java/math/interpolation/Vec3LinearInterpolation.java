package math.interpolation;

import net.minecraft.world.phys.Vec3;

public class Vec3LinearInterpolation extends Interpolation<Vec3> {
	public Vec3LinearInterpolation(double minT, double maxT) {
		super(minT, maxT);
	}

	public Vec3LinearInterpolation append(double t, double x, double y, double z) {
		return (Vec3LinearInterpolation) super.append(t, new Vec3(x, y, z));
	}

	public Vec3LinearInterpolation append(double t, double f) {
		return append(t, f, f, f);
	}

	@SuppressWarnings("unchecked")
	public static Vec3LinearInterpolation begin(double minT, double maxT) {
		return new Vec3LinearInterpolation(minT, maxT);
	}

	public Vec3 interploteVec3(double t) {
		Result<Vec3> result = super.interplote(t);
		Vec3 begin = result.begin;
		Vec3 end = result.end;
		double f = result.dt / result.pointDistance;
		return new Vec3(
				(begin.x * (1 - f) + end.x * f),
				(begin.y * (1 - f) + end.y * f),
				(begin.z * (1 - f) + end.z * f));
	}
}
