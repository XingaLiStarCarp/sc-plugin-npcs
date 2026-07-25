package javabase;

public class Pair<_First, _Second> {
	public _First first;
	public _Second second;

	public Pair(_First first, _Second second) {
		this.first = first;
		this.second = second;
	}

	public static <_First, _Second> Pair<_First, _Second> of(_First first, _Second second) {
		return new Pair<>(first, second);
	}

	@Override
	public String toString() {
		return "(" + first.toString() + ", " + second.toString() + ")";
	}
}
