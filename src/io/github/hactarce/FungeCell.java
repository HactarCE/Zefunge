package io.github.hactarce;

/**
 * Created by OWNER on 8/6/2016.
 */
public class FungeCell {

	final int value;
	final char character;
	final boolean truish;

	static final char UNKNOWN_CHAR = '•';

	FungeCell(int value) {
		this.value = value;
		character = getChar(value);
		truish = value != 0;
	}

	FungeCell(boolean truish) {
		this(truish ? 1 : 0);
	}

	FungeCell(FungeCell existingCell) {
		this(existingCell.value);
	}

	static char getChar(int value) {
		return 32 <= value && value <= 126 ? (char) value : UNKNOWN_CHAR;
	}

	FungeCell copy() {
		return new FungeCell(this);
	}

}
