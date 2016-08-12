package io.github.hactarce;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Andy on 8/11/2016.
 */
class Semantic {

	static final Map<Integer, Semantic> ALL_SEMANTICS = new HashMap<Integer, Semantic>() {{
		put(getFingerprint("TOYS"), new Semantic("TOYS", new HashMap<Character, Instruction>() {{
			put('C', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					BefungeVector source = instructionPointer.popVector();
					BefungeVector size = instructionPointer.popVector();
					BefungeVector dest = instructionPointer.popVector();
					int sourceX = source.x;
					int sourceY = source.y;
					int destX = dest.x;
					int destY = dest.y;
					for (int y = 0; y < size.x; y++)
						for (int x = 0; y < size.y; x++)
							world.putCell(destX + x, destY + y, world.getCell(sourceX + x, sourceY + y));
					return true;
				}
			}); // Low-order copy ("bracelet")
			put('K', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					BefungeVector source = instructionPointer.popVector();
					BefungeVector size = instructionPointer.popVector();
					BefungeVector dest = instructionPointer.popVector();
					int sourceX = source.x;
					int sourceY = source.y;
					int destX = dest.x;
					int destY = dest.y;
					for (int y = size.y - 1; y >= 0; y--)
						for (int x = size.x - 1; x >= 0; x--)
							world.putCell(destX + x, destY + y, world.getCell(sourceX + x, sourceY + y));
					return true;
				}
			}); // High-order copy ("scissors")
			put('M', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					BefungeVector source = instructionPointer.popVector();
					BefungeVector size = instructionPointer.popVector();
					BefungeVector dest = instructionPointer.popVector();
					int sourceX = source.x;
					int sourceY = source.y;
					int destX = dest.x;
					int destY = dest.y;
					for (int y = 0; y < size.y; y++)
						for (int x = 0; x < size.x; x++)
							world.putCell(destX + x, destY + y, world.getCell(sourceX + x, sourceY + y));
					for (int y = 0; y < size.y; y++)
						for (int x = 0; x < size.x; x++)
							world.putCell(sourceX + x, sourceY + y, 0);
					return true;
				}
			}); // Low-order move ("kittycat")
			put('V', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					BefungeVector source = instructionPointer.popVector();
					BefungeVector size = instructionPointer.popVector();
					BefungeVector dest = instructionPointer.popVector();
					int sourceX = source.x;
					int sourceY = source.y;
					int destX = dest.x;
					int destY = dest.y;
					for (int y = size.y - 1; y >= 0; y--)
						for (int x = size.x - 1; x >= 0; x--)
							world.putCell(destX + x, destY + y, world.getCell(sourceX + x, sourceY + y));
					for (int y = size.y - 1; y >= 0; y--)
						for (int x = size.x - 1; x >= 0; x--)
							world.putCell(sourceX + x, sourceY + y, 0);
					return true;
				}
			}); // High-order move ("dixiecup")
			put('S', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					BefungeCellBlock block = BefungeCellBlock.defineCornerAndSize(instructionPointer.popVector(), instructionPointer.popVector());
					FungeCell cell = instructionPointer.pop();
					for (BefungeVector vector : block.getVectors())
						world.putCell(vector, cell);
					return true;
				}
			}); // Fill ("chicane")
			put('J', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					int col = instructionPointer.getPosition().x;
					int offset = instructionPointer.pop().value;
					int end = world.getSize().y;
					if (offset > 0)
						for (int y = 0; y < end; y++)
							world.putCell(col, y, world.getCell(col, y - offset));
					else if (offset < 0)
						for (int y = end - 1; y >= 0; y--)
							world.putCell(col, y, world.getCell(col, y - offset));
					return true;
				}
			}); // Column shift ("fishhook")
			put('O', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					int row = instructionPointer.getPosition().y;
					int offset = instructionPointer.pop().value;
					int end = world.getSize().x;
					if (offset > 0)
						for (int x = 0; x < end; x++)
							world.putCell(x, row, world.getCell(x - offset, row));
					else if (offset < 0)
						for (int x = end - 1; x >= 0; x--)
							world.putCell(x, row, world.getCell(x - offset, row));
					return true;
				}
			}); // Row shift ("boulder")
			put('L', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.push(world.getCell(instructionPointer.getPosition().copy().add(instructionPointer.getDelta().copy().rotateLeft())));
					return true;
				}
			}); // Pick left ("corner")
			put('R', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.push(world.getCell(instructionPointer.getPosition().copy().add(instructionPointer.getDelta().copy().rotateRight())));
					return true;
				}
			}); // Pick right ("can opener")
			put('I', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.push(instructionPointer.pop().value + 1);
					return true;
				}
			}); // Increment ("doric column")
			put('D', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.push(instructionPointer.pop().value - 1);
					return true;
				}
			}); // Decrement ("toilet seat")
			put('N', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.push(0 - instructionPointer.pop().value);
					return true;
				}
			}); // Negate ("lightning bolt")
			put('H', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.push(instructionPointer.pop(1).value << instructionPointer.pop().value);
					return true;
				}
			}); // Binary shift ("pair of stilts")
			put('A', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					for (int i = instructionPointer.pop().value; i > 1; i--)
						instructionPointer.push(instructionPointer.peek());
					return true;
				}
			}); // Multi-duplicate ("gable")
			put('B', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					return false;
				}
			}); // Butterfly ("pair of shoes") TODO NOT IMPLEMENTED (what is a "butterfly" operation?)
			put('E', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					int sum = 0;
					for (int i = 0; i < instructionPointer.TOSS.size(); i++)
						sum += instructionPointer.pop().value;
					instructionPointer.push(sum);
					return true;
				}
			}); // Sum ("pitchfork head")
			put('P', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					int product = 0;
					for (int i = 0; i < instructionPointer.TOSS.size(); i++)
						product *= instructionPointer.pop().value;
					instructionPointer.push(product);
					return true;
				}
			}); // Product ("mailbox")
			put('F', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					return false;
				}
			}); // Stack to world ("calipers") TODO NOT IMPLEMENTED (unknown value of 'j'?)
			put('G', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					return false;
				}
			}); // World to stack ("counterclockwise") TODO NOT IMPLEMENTED (unknown value of 'j'?)
			put('Q', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					world.putCell(instructionPointer.getPosition().copy().sub(instructionPointer.getDelta()), instructionPointer.pop());
					return true;
				}
			}); // Store behind ("necklace")
			put('T', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					switch (instructionPointer.pop().value) {
						case 0:
							instructionPointer.setDelta(instructionPointer.pop().truish ? BefungeIP.WEST() : BefungeIP.EAST());
							return true;
						case 1:
							instructionPointer.setDelta(instructionPointer.pop().truish ? BefungeIP.NORTH() : BefungeIP.SOUTH());
							return true;
						default:
							return false;
					}
				}
			}); // Dimensional 'if' ("barstool")
			put('U', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					int n = new Random().nextInt(4);
					instructionPointer.setDelta(n == 0 ? BefungeIP.EAST()
							: n == 0 ? BefungeIP.NORTH()
							: n == 1 ? BefungeIP.SOUTH()
							: BefungeIP.WEST()
					);
					world.putCell(instructionPointer.getPosition(), "<^v>".charAt(n));
					return true;
				}
			}); // One-shot random ("tumbler")
			put('W', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					BefungeVector location = instructionPointer.popVector().add(instructionPointer.getStorageOffset());
					FungeCell cell = instructionPointer.pop();
					if (world.getCell(location).value < cell.value) {
						instructionPointer.push(cell);
						instructionPointer.pushVector(location);
						instructionPointer.skipForward(-1);
					} else if (world.getCell(location).value > cell.value) instructionPointer.reflect();
					return true;
				}
			}); // Compare to world ("television antenna")
			put('X', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.getPosition().offset(1, 0);
					return true;
				}
			}); // Increment X ("buried treasure")
			put('Y', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.getPosition().offset(0, 1);
					return true;
				}
			}); // Increment Y ("slingshot")
			put('Z', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.reflect();
					return true;
				}
			}); // Increment Z ("barn door") NOT IMPLEMENTED (not enough dimensions)
		}})); // TOYS - Standard Toys
		put(getFingerprint("ROMA"), new Semantic("ROMA", new HashMap<Character, Instruction>() {{
			put('C', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.push(100);
					return true;
				}
			}); // 100
			put('D', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.push(500);
					return true;
				}
			}); // 500
			put('I', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.push(1);
					return true;
				}
			}); // 1
			put('L', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.push(50);
					return true;
				}
			}); // 50
			put('M', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.push(1000);
					return true;
				}
			}); // 1000
			put('V', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.push(5);
					return true;
				}
			}); // 5
			put('X', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.push(10);
					return true;
				}
			}); // 10
		}})); // ROMA - Roman Numerals
		put(getFingerprint("MODE"), new Semantic("MODE", new HashMap<Character, Instruction>() {{
			put('H', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.relativeDelta = !instructionPointer.relativeDelta;
					return true;
				}
			}); // Toggle "hover mode" (relative delta; does NOT apply to '?')
			put('I', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.inversePush = !instructionPointer.inversePush;
					return true;
				}
			}); // Toggle "invert mode" (inverse push)
			put('Q', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.inversePop = !instructionPointer.inversePop;
					return true;
				}
			}); // Toggle "queue mode" (inverse pop)
			put('S', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					instructionPointer.switchMode = !instructionPointer.switchMode;
					return true;
				}
			}); // Toggle "switch mode" (swap opening/closing parentheses/brackets/braces when executed)
			put('[', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					if (instructionPointer.switchMode) world.putCell(instructionPointer.getPosition(), ']');
					return false;
				}
			});
			put(']', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					if (instructionPointer.switchMode) world.putCell(instructionPointer.getPosition(), '[');
					return false;
				}
			});
			put('{', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					if (instructionPointer.switchMode) world.putCell(instructionPointer.getPosition(), '}');
					return false;
				}
			});
			put('}', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					if (instructionPointer.switchMode) world.putCell(instructionPointer.getPosition(), '{');
					return false;
				}
			});
			put('(', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					if (instructionPointer.switchMode) world.putCell(instructionPointer.getPosition(), ')');
					return false;
				}
			});
			put(')', new Instruction() {
				@Override
				boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic) {
					if (instructionPointer.switchMode) world.putCell(instructionPointer.getPosition(), '(');
					return false;
				}
			});
		}})); // MODE - Standard Modes
	}};

	final Integer fingerprint;
	final Map<Character, Instruction> instructions;

	FungeCell[] internalData;

	Semantic(String fingerprint, Map<Character, Instruction> instructions) {
		this.fingerprint = getFingerprint(fingerprint);
		this.instructions = instructions;
	}

	static int getFingerprint(String s) {
		int result = 0;
		for (char c : s.toCharArray()) {
			result *= 256;
			result += c;
		}
		return result;
	}

	static abstract class Instruction {
		abstract boolean execute(BefungeWorld world, BefungeIP instructionPointer, Semantic semantic);
	}

}
