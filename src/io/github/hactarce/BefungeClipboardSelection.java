package io.github.hactarce;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Created by OWNER on 8/8/2016.
 */
public class BefungeClipboardSelection implements Transferable {

	public static final DataFlavor cellBlockFlavor = new DataFlavor(int[][].class, "Befunge Cell Block");

	private final int[][] cells;
	private final String string;

	public BefungeClipboardSelection(int[][] cells) {
		this.cells = cells;
		StringBuilder builder = new StringBuilder();
		int[][] transposed = new int[cells[0].length][cells.length];
		for (int x = 0; x < cells.length; x++) {
			int[] column = cells[x];
			for (int y = 0; y < column.length; y++)
				transposed[y][x] = column[y] != '•' ? column[y] : ' ';
		}
		for (int[] row : transposed) {
			for (int cellValue : row)
				builder.append((char) cellValue);
			builder.append('\n');
		}
		string = builder.toString();
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{DataFlavor.stringFlavor, cellBlockFlavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for (DataFlavor supportedFlavor : getTransferDataFlavors())
			if (supportedFlavor.equals(flavor)) return true;
		return false;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(DataFlavor.stringFlavor))
			return string;
		if (flavor.equals(cellBlockFlavor))
			return cells;
		throw new UnsupportedFlavorException(flavor);
	}

}
