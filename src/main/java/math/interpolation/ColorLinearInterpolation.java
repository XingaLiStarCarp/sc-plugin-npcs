package math.interpolation;

import graphics.ColorRGBA;

public class ColorLinearInterpolation extends Interpolation<ColorRGBA> {
	public ColorLinearInterpolation(double minT, double maxT) {
		super(minT, maxT);
	}

	/**
	 * 设置插值点
	 * 
	 * @param t
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 * @return
	 */
	public ColorLinearInterpolation append(float t, int r, int g, int b, int a) {
		return (ColorLinearInterpolation) super.append(t, ColorRGBA.of(r, g, b, a));
	}

	public ColorLinearInterpolation append(float t, float r, float g, float b, float a) {
		return (ColorLinearInterpolation) super.append(t, ColorRGBA.of(r, g, b, a));
	}

	public ColorLinearInterpolation append(float t, int r, int g, int b) {
		return append(t, r, g, b, 255);
	}

	public ColorLinearInterpolation append(float t, float r, float g, float b) {
		return append(t, r, g, b, 1.0f);
	}

	public ColorLinearInterpolation append(float t, int gray) {
		return append(t, gray, gray, gray);
	}

	public ColorLinearInterpolation append(float t, float gray) {
		return append(t, gray, gray, gray);
	}

	@SuppressWarnings("unchecked")
	public static ColorLinearInterpolation begin(double minT, double maxT) {
		return new ColorLinearInterpolation(minT, maxT);
	}

	public ColorRGBA interploteColor(float t) {
		Result<ColorRGBA> result = super.interplote(t);
		ColorRGBA begin = result.begin;
		ColorRGBA end = result.end;
		double f = result.dt / result.pointDistance;
		return ColorRGBA.of(
				(float) (begin.r * (1 - f) + end.r * f),
				(float) (begin.g * (1 - f) + end.g * f),
				(float) (begin.b * (1 - f) + end.b * f),
				(float) (begin.a * (1 - f) + end.a * f));
	}

	/**
	 * 根据时间进行插值
	 * 
	 * @param t
	 * @return
	 */
	public int interplotePacked(float t) {
		return interploteColor(t).packRGB();
	}
}