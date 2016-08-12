package io.github.hactarce;

import java.awt.*;

/**
 * Created by OWNER on 8/6/2016.
 */
class BefungeWorld {

	private FungeCell[][] world;
	private final BefungeVector size;
	private CellDisplay[][] displays;
	private final Thread quickRunThread = new Thread(new Runnable() {
		@Override
		public void run() {
			long lastStep = 0;
			while (!Thread.currentThread().isInterrupted()) {
				sleep(Long.max(0, lastStep + (1000 / simSpeed) - System.currentTimeMillis()));
				while (!running) sleep(100);
				lastStep = System.currentTimeMillis();
				step();
			}
			quickRunThread.start();
		}

		private void sleep(long ms) {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				e.getLocalizedMessage();
			}
		}

	}, "Thread-Befunge-Simulator");
	private boolean running = false;
	long simSpeed = 5;
	private BefungeCellBlock selection = new BefungeCellBlock();
	private boolean ended = false;
	BefungeIP instructionPointer = new BefungeIP(this);

	BefungeWorld(BefungeVector size, Container displayBase) {
		this.size = size;
		initWorld();
		initDisplays(displayBase);
		quickRunThread.start();
	}

	private void initWorld() {
		world = new FungeCell[size.x][size.y];
		for (int x = 0; x < size.x; x++)
			for (int y = 0; y < size.y; y++)
				world[x][y] = new FungeCell(' ');
	}

	private void initDisplays(Container displayBase) {
		GridBagConstraints gc = new GridBagConstraints();
		displays = new CellDisplay[size.x][size.y];
		for (int x = 0; x < size.x; x++) {
			gc.gridx = x;
			for (int y = 0; y < size.y; y++) {
				gc.gridy = y;
				CellDisplay newCellDisplay = new CellDisplay(this, new BefungeVector(x, y));
				displayBase.add(newCellDisplay, gc);
				displays[x][y] = newCellDisplay;
			}
		}
	}

	//region Flow Control
	void run() {
		if (ended) return;
		running = true;
		if (onRunStateChanged != null) onRunStateChanged.run();
	}

	void stop() {
		if (ended) return;
		running = false;
		if (onRunStateChanged != null) onRunStateChanged.run();
	}

	void toggleRun() {
		if (ended) return;
		running = !running;
		if (onRunStateChanged != null) onRunStateChanged.run();
	}

	boolean isRunning() {
		return running;
	}

	//region onRunStateChanged, onStep, onEnd, onReset
	private Runnable onRunStateChanged;
	private Runnable onStep;
	private Runnable onEnd;
	private Runnable onReset;

	void setOnRunStateChanged(Runnable onRunStateChanged) {
		this.onRunStateChanged = onRunStateChanged;
	}

	void setOnStep(Runnable onStep) {
		this.onStep = onStep;
	}

	void setOnEnd(Runnable onEnd) {
		this.onEnd = onEnd;
	}

	void setOnReset(Runnable onReset) {
		this.onReset = onReset;
	}
	//endregion

	void step() {
		if (ended) return;
		instructionPointer.stepForward();
		if (onStep != null) onStep.run();
	}

	void end() {
		stop();
		ended = true;
		instructionPointer.setDelta(new BefungeVector());
		notifyIPMoved();
		if (onEnd != null) onEnd.run();
	}

	boolean isEnded() {
		return ended;
	}

	void restart() {
		instructionPointer = new BefungeIP(this);
		ended = false;
		output = "";
		if (onReset != null) onReset.run();
	}
	//endregion
	//region World
	BefungeVector getSize() {
		return size;
	}

	FungeCell getCell(BefungeVector position) {
		return getCell(position.x, position.y);
	}

	FungeCell getCell(int x, int y) {
		try {
			return world[x][y];
		} catch (IndexOutOfBoundsException e) {
			return new FungeCell(20);
		}
	}

	void putCell(BefungeVector position, FungeCell cell) {
		putCell(position, cell.value);
	}

	void putCell(BefungeVector position, int value) {
		putCell(position.x, position.y, value);
	}

	void putCell(int x, int y, FungeCell cell) {
		putCell(x, y, cell.value);
	}

	void putCell(int x, int y, int value) {
		try {
			world[x][y] = new FungeCell(value);
			displays[x][y].update();
		} catch (IndexOutOfBoundsException e) {
		}
	}

	void setSize(BefungeVector newSize) {
		FungeCell[][] newWorld = new FungeCell[newSize.x][newSize.y];
		for (int x = 0; x < newSize.x; x++) {
			for (int y = 0; y < newSize.y; y++) {
				newWorld[x][y] = getCell(x, y);
			}
		}
		Container displayBase = getParentContainer();
		displayBase.removeAll();
		initDisplays(displayBase);
	}
	//endregion
	//region Selection/Clipboard
	private Runnable onRegionSelect;

	void setOnRegionSelect(Runnable onRegionSelect) {
		this.onRegionSelect = onRegionSelect;
	}

	BefungeClipboardSelection getClipboardSelection(BefungeCellBlock block) {
		int[][] cells = new int[block.right - block.left + 1][block.bottom - block.top + 1];
		for (BefungeVector vector : block.getVectors())
			cells[vector.x - block.left][vector.y - block.top] = getCell(vector).value;
		return new BefungeClipboardSelection(cells);
	}

	void clearCells(BefungeCellBlock block) {
		for (BefungeVector vector : block.getVectors())
			putCell(vector, ' ');
	}

	void pasteString(String cellBlockString, BefungeVector location) {
		int x = location.x;
		int y = location.y;
		for (char c : cellBlockString.toCharArray()) {
			if (c == '\r') continue;
			if (c == '\n') {
				x = location.x;
				y++;
			} else putCell(x++, y, c);
		}
	}

	void pasteBlock(int[][] cellBlock, BefungeVector location) {
		int maxHeight = 0;
		for (int x = 0; x < cellBlock.length; x++) {
			int[] column = cellBlock[x];
			if (column.length > maxHeight) maxHeight = column.length;
			for (int y = 0; y < column.length; y++)
				putCell(location.x + x, location.y + y, column[y]);
		}
		select(new BefungeCellBlock(location, location.copy().offset(cellBlock.length - 1, maxHeight - 1)));
	}
	//endregion
	//region Visuals
	void focusCell(BefungeVector position) {
		focusCell(position.x, position.y);
	}

	void focusCell(int x, int y) {
		try {
			displays[x][y].grabFocus();
		} catch (IndexOutOfBoundsException e) {
		}
	}

	void selectCell(BefungeVector position) {
		selectCell(position.x, position.y);
	}

	void selectCell(int x, int y) {
		try {
			displays[x][y].select();
		} catch (IndexOutOfBoundsException e) {
		}
	}

	void deselectCell(BefungeVector position) {
		deselectCell(position.x, position.y);
	}

	void deselectCell(int x, int y) {
		try {
			displays[x][y].deselect();
		} catch (IndexOutOfBoundsException e) {
		}
	}

	BefungeCellBlock getSelection() {
		return selection;
	}

	private Container getParentContainer() {
		return displays[0][0].getParent();
	}

	void select(BefungeCellBlock newSelection) {
		if (selection.equals(newSelection)) return;
		if (!selection.isEmpty())
			for (BefungeVector vector : selection.getVectors())
				if (!newSelection.contains(vector))
					deselectCell(vector);
		if (!newSelection.isEmpty())
			for (BefungeVector vector : newSelection.getVectors())
				if (!selection.contains(vector))
					selectCell(vector);
		selection = newSelection;
		getParentContainer().repaint(0, 0, 1, 1);
		if (onRegionSelect != null) onRegionSelect.run();
	}

	void selectAll() {
		select(new BefungeCellBlock(new BefungeVector(0, 0), getSize().copy().offset(-1, -1)));
	}

	void deselectAll() {
		select(new BefungeCellBlock());
	}
	//endregion
	//region I/O
	private String output = "";
	private Runnable onOutput;

	void setOnOutput(Runnable onOutput) {
		this.onOutput = onOutput;
	}

	String getOutput() {
		return output;
	}

	void appendOutput(String s) {
		output += s;
		if (onOutput != null) onOutput.run();
	}

	void appendOutput(char c) {
		output += c;
		if (onOutput != null) onOutput.run();
	}
	//endregion
	void notifyIPMoved() {
		focusCell(instructionPointer.getPosition());
		if (instructionPointer.getOnChange() != null) instructionPointer.getOnChange().run();
	}

}
