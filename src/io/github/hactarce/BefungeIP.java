package io.github.hactarce;

import java.util.*;

/**
 * Created by OWNER on 8/7/2016.
 */
class BefungeIP {

	public static transient final boolean SUPPORTS_CONCURRENT = false;
	public static transient final boolean SUPPORTS_IO = false;
	public static transient final boolean SUPPORTS_EXEC = false;
	public static transient final int VERSION = 0;

	private final transient BefungeWorld world;
	private BefungeVector position = new BefungeVector();
	private BefungeVector delta = new BefungeVector(1, 0);
	private BefungeVector storageOffset = new BefungeVector();
	private boolean stringMode = false;
	final int id = 0;

	private final ArrayList<ArrayList<FungeCell>> stackOfStacks = new ArrayList<>();
	ArrayList<FungeCell> TOSS;
	ArrayList<FungeCell> SOSS;

	//region "Standard Modes"
	boolean inversePop = false;
	boolean inversePush = false;
	boolean relativeDelta = false;
	boolean switchMode = false;
	//endregion

	BefungeIP(BefungeWorld world) {
		this.world = world;
	}

	void stepForward() {
		FungeCell cell = world.getCell(position);
		int n;
		for (Semantic semantic : loadedSemantics)
			if (semantic.instructions.containsKey(cell.character) && semantic.instructions.get(cell.character).execute(world, this, semantic))
				return;
		if (cell.character == ' ') {
			if (stringMode) push(' ');
			BefungeVector startPos = position.copy();
			do skipForward(false); while (world.getCell(position).value == ' ' && !startPos.equals(position));
			world.notifyIPMoved();
			return;
		} else if (stringMode) {
			if (cell.character == '"') stringMode = false;
			else push(cell);
		} else switch (cell.character) {
//			case ' ': Space (already handled)
			case '!': //region Logical NOT
				push(!pop().truish);
				break; //endregion
			case '"': //region Toggle Stringmode
				stringMode = true;
				break; //endregion
			case '#': //region Trampoline
				skipForward(false);
				break; //endregion
			case '$': //region Pop
				pop();
				break; //endregion
			case '%': //region Remainder
				push(pop(1).value % pop().value);
				break; //endregion
			case '&': //region Input Integer (TODO)
				reflect();
				break; //endregion
			case '\'': // region Fetch Character
				push(world.getCell(position.add(delta)));
				break; //endregion
			case '(': //region Load Semantics
				n = popFingerprint();
				if (Semantic.ALL_SEMANTICS.containsKey(n)) {
					loadSemantic(Semantic.ALL_SEMANTICS.get(n));
					pushFingerprint(n);
				} else reflect();
				break; //endregion
			case ')': //region Unload Semantics
				n = popFingerprint();
				if (Semantic.ALL_SEMANTICS.containsKey(n) && loadedSemantics.contains(Semantic.ALL_SEMANTICS.get(n)))
					unloadSemantic(Semantic.ALL_SEMANTICS.get(n));
				else reflect();
				break; //endregion
			case '*': //region Multiply
				push(pop(1).value * pop().value);
				break; //endregion
			case '+': //region Add
				push(pop(1).value + pop().value);
				break; //endregion
			case ',': //region Output Character
				world.appendOutput(pop().character);
				break; //endregion
			case '-': //region Subtract
				push(pop(1).value - pop().value);
				break; //endregion
			case '.': //region Output Integer
				world.appendOutput(Integer.toString(pop().value));
				break; //endregion
			case '/': //region Divide
				push(pop(1).value / pop().value);
				break; //endregion
			case '0': //region Push 0-9
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				push(cell.character - '0');
				break; //endregion
			case ':': //region Duplicate
				push(peek());
				break; //endregion
			case ';': //region Jump Over
				do skipForward(false);
				while (world.getCell(position).character != ';');
				break; //endregion
			case '<': //region Go West
				if (relativeDelta) delta = WEST();
				else delta.add(WEST());
				break; //endregion
			case '=': //region Execute (TODO)
				reflect();
				break; //endregion
			case '>': //region Go East
				if (relativeDelta) delta = EAST();
				else delta.add(EAST());
				break; //endregion
			case '?': //region Go Away (Random)
				n = new Random().nextInt(4);
				delta = n == 0 ? EAST()
						: n == 1 ? NORTH()
						: n == 2 ? SOUTH()
						: WEST();
				break; //endregion
			case '@': //region Stop
				world.end();
				break; //endregion
			case 'A': //region Fingerprint-Defined
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'J':
			case 'K':
			case 'L':
			case 'M':
			case 'N':
			case 'O':
			case 'P':
			case 'Q':
			case 'R':
			case 'S':
			case 'T':
			case 'U':
			case 'V':
			case 'W':
			case 'X':
			case 'Y':
			case 'Z':
				for (Semantic semantic : loadedSemantics) {
					if (semantic.instructions.containsKey(cell.character)) {
						semantic.instructions.get(cell.character).execute(world, this, semantic);
						break;
					}
				}
				break; //endregion
			case '[': //region Turn Left
				delta.rotateLeft();
				break; //endregion
			case '\\': //region Swap
				push(pop(1));
				break; //endregion
			case ']': //region Turn Right
				delta.rotateRight();
				break; //endregion
			case '^': //region Go North
				if (relativeDelta) delta = NORTH();
				else delta.add(NORTH());
				break; //endregion
			case '_': //region East-West If
				if (relativeDelta) delta = new BefungeVector(pop().truish ? -1 : 1, 0);
				else delta.add(new BefungeVector(pop().truish ? -1 : 1, 0));
				break; //endregion
			case '`': //region Greater Than
				push(pop(1).value > pop().value);
				break; //endregion
			case 'a': //region Push 10-15
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
				push(cell.character - 'a' + 10);
				break; //endregion
			case 'g': //region Get
				push(world.getCell(popVector().add(storageOffset)));
				break; //endregion
			case 'h': //region Go High (3D only)
				reflect();
				break; //endregion
			case 'i': //region Input File (TODO)
				reflect();
				break; //endregion
			case 'j': //region Jump Forward
				skipForward(pop().value);
				break; //endregion
			case 'k': //region Iterate
				do position.add(delta);
				while ((" \";k" + FungeCell.UNKNOWN_CHAR).contains(Character.toString(world.getCell(position).character)));
				BefungeVector priorPos = position;
				for (int i = 0; i < pop().value; i++) {
					stepForward();
					position = priorPos;
				}
				break; //endregion
			case 'l': //region Go Low (3D only)
				reflect();
				break; //endregion
			case 'm': //region High-Low If
				reflect();
				break; //endregion
			case 'n': //region Clear Stack
				TOSS.clear();
				break; //endregion
			case 'o': //region Output File (TODO)
				reflect();
				break; //endregion
			case 'p': //region Put
				world.putCell(popVector().add(storageOffset), pop());
				break; //endregion
			case 'q': //region Quit
				world.end();
				break; //endregion
			case 'r': //region Reflect
				reflect();
				break; //endregion
			case 's': //region Store Character
				world.putCell(position.copy().add(delta), pop());
				skipForward(false);
				break; //endregion
			case 't': //region Split
//				if (SUPPORTS_CONCURRENT) {
//					try {
//						ByteArrayOutputStream bos = new ByteArrayOutputStream();
//						ObjectOutputStream oos = new ObjectOutputStream(bos);
//						oos.writeObject(this);
//						oos.flush();
//						oos.close();
//						BefungeIP newIP = (BefungeIP) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
//						newIP.reflect();
//						world.assignIPID(newIP);
//						world.instructionPointers.add(newIP);
//					} catch (IOException e) {
//						e.getLocalizedMessage();
//						returnCode = ExecError.ERR_SERIALIZATION_FAILED;
//						reflect();
//					} catch (ClassNotFoundException e) {
//						returnCode = ExecError.ERR_DESERIALIZATION_FAILED;
//						reflect();
//					}
//				} else reflect();
				reflect();
				break; //endregion
			case 'u': //region Stack Under Stack
				if (SOSS != null)
					for (int i = pop().value; i > 0; i--)
						push(pop(SOSS));
				break; //endregion
			case 'v': //region Go South
				if (relativeDelta) delta = SOUTH();
				else delta.add(SOUTH());
				break; //endregion
			case 'w': //region Compare
				int secondNum = pop().value;
				int firstNum = pop().value;
				if (firstNum > secondNum) delta.rotateRight();
				if (firstNum < secondNum) delta.rotateLeft();
				break; //endregion
			case 'x': //region Absolute Delta
				delta = popVector();
				break; //endregion
			case 'y': //region Get SysInfo
				n = Math.abs(pop().value);
				sizeOfTOSS = TOSS.size();
				if (n == 0)
					for (int i = 20; i > 1; i--)
						pushSysInfo(i);
				else if (n <= 20)
					pushSysInfo(n);
				else
					push(pop(n - 21));
				break; //endregion
			case 'z': //region No Operation
				break; //endregion
			case '{': //region Begin Block
				ArrayList<FungeCell> newStack = new ArrayList<>();
				n = pop().value;
				if (n > 0)
					for (int i = 0; i < n; i++)
						newStack.add(0, pop());
				else if (n < 0)
					for (int i = n; i < 0; i++)
						newStack.add(new FungeCell(0));
				addStack(newStack);
				pushVector(storageOffset);
				storageOffset = position.copy().add(delta);
				break; //endregion
			case '|': //region North-South If
				if (relativeDelta) delta = new BefungeVector(0, pop().truish ? -1 : 1);
				else delta.add(new BefungeVector(0, pop().truish ? -1 : 1));
				break; //endregion
			case '}': //region End Block
				if (stackOfStacks.size() < 2)
					reflect();
				else {
					n = pop().value;
					storageOffset = popVector();
					if (n < 0)
						for (int i = 0; i < n; i++)
							pop(SOSS);
					else if (n > 0)
						if (n > TOSS.size()) {
							SOSS.addAll(TOSS);
							n -= TOSS.size();
							for (int i = 0; i < n; i++)
								SOSS.add(new FungeCell(0));
						} else
							SOSS.addAll(TOSS.subList(TOSS.size() - n, TOSS.size()));
					stackOfStacks.remove(TOSS);
				}
				break; //endregion
			case '~': //region Input Character (TODO)
				reflect();
				break; //endregion
			default: //region Unknown
				reflect();
				break; //endregion
		}
		skipForward();
	}

	void reflect() {
		delta.reflect();
	}

	private int sizeOfTOSS = 0;

	private void pushSysInfo(int item) {
		switch (item) {
			case 1:  //region Flags
				push((SUPPORTS_CONCURRENT ? 0x1 : 0)
						+ (SUPPORTS_IO ? 0x6 : 0)
						+ (SUPPORTS_EXEC ? 0x8 : 0));
				break; //endregion
			case 2:  //region Bytes per cell
				push(4);
				break; //endregion
			case 3:  //region Handprint
				push(('Z' << 24)
						+ ('F' << 16)
						+ ('n' << 8)
						+ ('g'));
				break; //endregion
			case 4:  //region Version Number
				push(VERSION);
				break; //endregion
			case 5:  //region Global Environment (Exec)
				push(0);
				break; //endregion
			case 6:  //region Path Separator (IO)
				push(0);
				break; //endregion
			case 7:  //region Dimensions
				push(2);
				break; //endregion
			case 8:  //region Instruction Pointer ID
				push(id);
				break; //endregion
			case 9:  //region Team Number (NetFunge/BeGlad)
				push(0);
				break; //endregion
			case 10: //region Funge-Space Position
				pushVector(position);
				break; //endregion
			case 11: //region Funge-Space Delta
				pushVector(delta);
				break; //endregion
			case 12: //region Funge-Space Storage Offset
				pushVector(storageOffset);
				break; //endregion
			case 13: //region Top-Left Coordinate
				pushVector(0, 0);
				break; //endregion
			case 14: //region Bottom-Right Coordinate
				pushVector(world.getSize().copy().offset(-1, -1));
				break; //endregion
			case 15: //region Date
				Calendar calendar = Calendar.getInstance();
				push((calendar.get(Calendar.YEAR) * 256 * 256)
						+ (calendar.get(Calendar.MONTH) * 256)
						+ (calendar.get(Calendar.DAY_OF_MONTH)));
				break; //endregion
			case 16: //region Time
				calendar = Calendar.getInstance();
				push((calendar.get(Calendar.HOUR_OF_DAY) * 256 * 256)
						+ calendar.get(Calendar.MINUTE) * 256
						+ calendar.get(Calendar.SECOND));
				break; //endregion
			case 17: //region Size of Stack Stack
				push(stackOfStacks.size());
				break; //endregion
			case 18: //region Sizes of Stacks on Stack Stack (this is getting worse every time)
				push(sizeOfTOSS);
				for (int i = stackOfStacks.size() - 2; i >= 0; i--) push(stackOfStacks.get(i).size());
				break; //endregion
			case 19: //region Command-Line Arguments
				push(0);
				push(0);
				push(0);
				pushString("ZeFunge.b98");
				break; //endregion
			case 20: //region Environment Variables (TODO)
				push(0);
				break; //endregion
		}
	}

	//region Moving
	void skipForward() {
		skipForward(true);
	}

	private void skipForward(boolean notifyWorld) {
		position.add(delta);
		GET_BACK_IN_BOUNDS();
		if (notifyWorld) world.notifyIPMoved();
	}

	void skipForward(int steps) {
		skipForward(steps, true);
	}

	private void skipForward(int steps, boolean notifyWorld) {
		if (steps > 0)
			for (int i = 0; i < steps; i++) skipForward(false);
		if (steps < 0)
			for (int i = 0; i > steps; i--) {
				position.sub(delta);
				GET_BACK_IN_BOUNDS();
			}
		if (notifyWorld) world.notifyIPMoved();
	}

	boolean isOutOfBounds() {
		return position.x < 0 || position.x >= world.getSize().x || position.y < 0 || position.y >= world.getSize().y;
	}

	private void GET_BACK_IN_BOUNDS() {
		if (isOutOfBounds()) {
			position.sub(delta);
			if (isOutOfBounds()) {
				do position.add(delta);
				while (isOutOfBounds());
				return;
			}
			do position.sub(delta);
			while (isOutOfBounds());
			do position.sub(delta);
			while (!isOutOfBounds());
			position.add(delta);
			if (onChange != null) onChange.run();
		}
	}

	public BefungeVector getPosition() {
		return position;
	}

	public void setPosition(BefungeVector position) {
		this.position = position;
		if (onChange != null) onChange.run();
	}

	public BefungeVector getDelta() {
		return delta;
	}

	public void setDelta(BefungeVector delta) {
		this.delta = delta;
		world.notifyIPMoved();
		if (onChange != null) onChange.run();
	}

	//region onChange
	private Runnable onChange;

	void setOnChange(Runnable onChange) {
		this.onChange = onChange;
	}

	Runnable getOnChange() {
		return onChange;
	}

	//endregion
	//endregion
	//region Stack
	void addStack(ArrayList<FungeCell> newStack) {
		stackOfStacks.add(newStack);
		TOSS = getStack(0);
		SOSS = getStack(1);
	}

	FungeCell pop() {
		return pop(TOSS);
	}

	FungeCell pop(int i) {
		if (TOSS.size() <= i)
			return new FungeCell(0);
		else return TOSS.remove(TOSS.size() - i - 1);
	}

	FungeCell pop(ArrayList<FungeCell> stack) {
		if (stack.isEmpty())
			return new FungeCell(0);
		else return stack.remove(stack.size() - 1);
	}

	void push(FungeCell cell) {
		TOSS.add(cell.copy());
	}

	void push(int number) {
		TOSS.add(new FungeCell(number));
	}

	void push(boolean truish) {
		TOSS.add(new FungeCell(truish));
	}

	void push(FungeCell cell, ArrayList<FungeCell> stack) {
		stack.add(cell.copy());
	}

	String popString() {
		StringBuilder builder = new StringBuilder();
		FungeCell poppedCell = pop();
		while (poppedCell.value != 0)
			builder.append(poppedCell.character);
		return builder.toString();
	}

	void pushString(String string) {
		push(0);
		for (char c : new StringBuffer(string).reverse().toString().toCharArray())
			push(c);
	}

	BefungeVector popVector() {
		return new BefungeVector(pop(1).value, pop().value);
	}

	void pushVector(BefungeVector vector) {
		pushVector(vector.x, vector.y);
	}

	void pushVector(int x, int y) {
		push(x);
		push(y);
	}

	static FungeCell getTopOfStack(ArrayList<FungeCell> stack) {
		return stack.get(stack.size() - 1);
	}

	ArrayList<FungeCell> getStack(int indexFromTop) {
		if (indexFromTop++ < stackOfStacks.size())
			return stackOfStacks.get(stackOfStacks.size() - indexFromTop);
		else return new ArrayList<>();
	}

	FungeCell peek() {
		if (TOSS.isEmpty())
			return new FungeCell(0);
		else return TOSS.get(TOSS.size() - 1);
	}

	FungeCell peek(int i) {
		if (TOSS.size() <= i)
			return new FungeCell(0);
		else return TOSS.get(TOSS.size() - i - 1);
	}

	int popFingerprint() {
		int n = pop().value;
		String s = "";
		for (int i = 0; i < n; i++) s += (char) pop().value;
		return Semantic.getFingerprint(s);
	}

	void pushFingerprint(int i) {
		push(i % 256);
		push((i / 256) % 256);
		push((i / 256 / 256) % 256);
		push((i / 256 / 256 / 256) % 256);
		push(4);
	}

	//endregion
	//region Cardinal Directions
	static BefungeVector EAST() {
		return new BefungeVector(1, 0);
	}

	static BefungeVector WEST() {
		return new BefungeVector(-1, 0);
	}

	static BefungeVector NORTH() {
		return new BefungeVector(0, -1);
	}

	static BefungeVector SOUTH() {
		return new BefungeVector(0, 1);
	}

	//endregion
	//region Semantics
	final ArrayList<Semantic> loadedSemantics = new ArrayList<>();
	final Map<Character, Semantic.Instruction> semanticInstructions = new HashMap<>();

	void loadSemantic(Semantic semantic) {
		loadedSemantics.add(0, semantic);
	}

	void unloadSemantic(Semantic semantic) {
		loadedSemantics.remove(semantic);
		if (semantic.fingerprint == Semantic.getFingerprint("MODE")) {
			inversePop = false;
			inversePush = false;
			relativeDelta = false;
			switchMode = false;
		}
	}
	//endregion

	public BefungeVector getStorageOffset() {
		return storageOffset;
	}

	public void setStorageOffset(BefungeVector storageOffset) {
		this.storageOffset = storageOffset;
	}

}
