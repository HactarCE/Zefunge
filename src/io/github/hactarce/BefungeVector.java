package io.github.hactarce;

/**
 * Created by OWNER on 8/6/2016.
 */
class BefungeVector {

	int x = 0;
	int y = 0;

	BefungeVector() {
	}

	BefungeVector(int x, int y) {
		this.x = x;
		this.y = y;
	}

	BefungeVector copy() {
		return new BefungeVector(x, y);
	}

	BefungeVector offset(int x, int y) {
		this.x += x;
		this.y += y;
		return this;
	}

	BefungeVector add(BefungeVector otherVector) {
		return offset(otherVector.x, otherVector.y);
	}

	BefungeVector sub(BefungeVector otherVector) {
		this.x -= otherVector.x;
		this.y -= otherVector.y;
		return this;
	}

	BefungeVector scale(int scalar) {
		this.x *= scalar;
		this.y *= scalar;
		return this;
	}

	BefungeVector rotateLeft() {
		int oldX = x;
		x = y;
		y = -oldX;
		return this;
	}

	BefungeVector rotateRight() {
		int oldX = x;
		x = -y;
		y = oldX;
		return this;
	}

	BefungeVector reflect() {
		x *= -1;
		y *= -1;
		return this;
	}

	void print() {
		System.out.println(toString());
	}

	boolean facingRight() {
		return Math.abs(x) > Math.abs(y) && x > 0;
	}

	boolean facingLeft() {
		return Math.abs(x) > Math.abs(y) && x < 0;
	}

	boolean facingUp() {
		return Math.abs(x) < Math.abs(y) && y < 0;
	}

	boolean facingDown() {
		return Math.abs(x) < Math.abs(y) && y > 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BefungeVector) {
			BefungeVector other = (BefungeVector) obj;
			return x == other.x && y == other.y;
		} else return false;
	}

	@Override
	public String toString() {
		return String.format("(%d, %d)", x, y);
	}
}
