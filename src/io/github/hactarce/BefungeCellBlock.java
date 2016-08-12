package io.github.hactarce;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by OWNER on 8/7/2016.
 */
class BefungeCellBlock {

	private final boolean empty;
	final int top;
	final int left;
	final int bottom;
	final int right;

	BefungeCellBlock() {
		empty = true;
		top = 0;
		left = 0;
		bottom = 0;
		right = 0;
	}

	BefungeCellBlock(int top, int left, int bottom, int right) {
		empty = false;
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	BefungeCellBlock(BefungeVector corner1, BefungeVector corner2) {
		this(
				Integer.min(corner1.y, corner2.y),
				Integer.min(corner1.x, corner2.x),
				Integer.max(corner1.y, corner2.y),
				Integer.max(corner1.x, corner2.x)
		);
	}

	static BefungeCellBlock defineCornerAndSize(BefungeVector corner, BefungeVector size) {
		return new BefungeCellBlock(corner, corner.copy().add(size).offset(-1, -1));
	}

	BefungeVector[] getVectors() {
		ArrayList<BefungeVector> vectors = new ArrayList<>();
		if (!isEmpty())
			for (int y = top; y <= bottom; y++)
				for (int x = left; x <= right; x++)
					vectors.add(new BefungeVector(x, y));
		return vectors.toArray(new BefungeVector[]{});
	}

	@Override
	public boolean equals(Object obj) {
		if (getClass() != obj.getClass()) return false;
		BefungeCellBlock other = (BefungeCellBlock) obj;
		return top == other.top &&
				bottom == other.bottom &&
				left == other.left &&
				right == other.right;
	}

	boolean isEmpty() {
		return empty;
	}

	int getVolume() {
		return empty ? 0 : (right - left + 1) * (bottom - top + 1);
	}

	boolean contains(BefungeVector vector) {
		return !empty &&
				left <= vector.x &&
				vector.x <= right &&
				top <= vector.y &&
				vector.y <= bottom;
	}

	void print() {
		System.out.println(String.format("from %s to %s", new BefungeVector(left, top).toString(), new BefungeVector(right, bottom).toString()));
	}

}
