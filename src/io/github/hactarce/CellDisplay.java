package io.github.hactarce;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;

/**
 * Created by Andy on 8/5/2016.
 */
class CellDisplay extends JLabel implements FocusListener, KeyListener, MouseListener, MouseMotionListener {

	private static final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	private static final Dimension fontRect = getFontRect(font);
	//region private static final Region colors = new StealthyRegion();
	private static final Color backgroundNormal = Color.BLACK;
	private static final Color foregroundNormal = Color.WHITE;
	private static final Color backgroundActive = Color.RED;
	private static final Color foregroundActive = Color.WHITE;
	private static final Color backgroundSelect = Color.RED.darker();
	private static final Color foregroundSelect = Color.WHITE;
	private static final Color borderColor = new Color(64, 64, 64);
	private static final Color borderColorSelected = Color.PINK;
	//endregion
	//region private static final Region borders = new StealthyRegion();
	private static final Border border = new LineBorder(borderColor);
	private static final Border borderUp = new MatteBorder(3, 1, 1, 1, borderColorSelected);
	private static final Border borderLeft = new MatteBorder(1, 3, 1, 1, borderColorSelected);
	private static final Border borderDown = new MatteBorder(1, 1, 3, 1, borderColorSelected);
	private static final Border borderRight = new MatteBorder(1, 1, 1, 3, borderColorSelected);
	private static final Border borderIP = new LineBorder(borderColorSelected);
	//endregion
	private static BefungeVector selectionCorner;

	private final BefungeWorld world;
	private final BefungeVector befungePosition;

	private static Dimension getFontRect(Font font) {
		FontMetrics metrics = new Canvas().getFontMetrics(font);
		int maxWidth = 0;
		for (int width : metrics.getWidths())
			if (width > maxWidth) maxWidth = width;
		return new Dimension(maxWidth + 4, metrics.getHeight());
	}

	CellDisplay(BefungeWorld world, BefungeVector position) {
		super();
		this.world = world;
		befungePosition = position;
		setFont(font);
		setPreferredSize(fontRect);
		setHorizontalAlignment(JLabel.CENTER);
		setVerticalAlignment(JLabel.CENTER);
		setForeground(foregroundNormal);
		setBackground(backgroundNormal);
		setBorder(border);
		setOpaque(true);
		addFocusListener(this);
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		update();
	}

	void select() {
		if (!hasFocus()) {
			setBackground(backgroundSelect);
			setForeground(foregroundSelect);
		}
	}

	void deselect() {
		if (!hasFocus()) {
			setBackground(backgroundNormal);
			setForeground(foregroundNormal);
			setBorder(border);
		}
	}

	void update() {
		setText(Character.toString(world.getCell(befungePosition).character));
	}

	//region Focus Listener
	@Override
	public void focusGained(FocusEvent e) {
		world.select(new BefungeCellBlock(befungePosition, befungePosition));
		world.instructionPointer.setPosition(befungePosition.copy());
		setForeground(foregroundActive);
		setBackground(backgroundActive);
		setBorder(borderIP);
	}

	@Override
	public void focusLost(FocusEvent e) {
	}

	//endregion
	//region KeyListener
	@Override
	public void keyTyped(KeyEvent e) {
		if (32 <= e.getKeyChar() && e.getKeyChar() <= 126) {
			world.putCell(befungePosition, e.getKeyChar());
			world.instructionPointer.skipForward();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_DOWN:
				world.focusCell(befungePosition.x, befungePosition.y + 1);
				break;
			case KeyEvent.VK_UP:
				world.focusCell(befungePosition.x, befungePosition.y - 1);
				break;
			case KeyEvent.VK_RIGHT:
				world.focusCell(befungePosition.x + 1, befungePosition.y);
				break;
			case KeyEvent.VK_LEFT:
				world.focusCell(befungePosition.x - 1, befungePosition.y);
				break;
			case KeyEvent.VK_HOME:
				world.focusCell(0, befungePosition.y);
				break;
			case KeyEvent.VK_END:
				world.focusCell(world.getSize().x - 1, befungePosition.y);
				break;
			case KeyEvent.VK_PAGE_UP:
				world.focusCell(befungePosition.x, 0);
				break;
			case KeyEvent.VK_PAGE_DOWN:
				world.focusCell(befungePosition.x, world.getSize().y - 1);
				break;
			case KeyEvent.VK_BACK_SPACE:
				world.instructionPointer.skipForward(-1);
				world.select(new BefungeCellBlock(world.instructionPointer.getPosition(), world.instructionPointer.getPosition()));
//				break;
			case KeyEvent.VK_DELETE:
				world.clearCells(world.getSelection());
				break;
			case KeyEvent.VK_ENTER:
				if ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0)
					world.toggleRun();
				else world.step();
				break;
			case KeyEvent.VK_X:
				if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
					BefungeClipboardSelection newClipboard = world.getClipboardSelection(world.getSelection());
					getToolkit().getSystemClipboard().setContents(newClipboard, null);
					world.clearCells(world.getSelection());
				}
				break;
			case KeyEvent.VK_C:
				if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
					BefungeClipboardSelection newClipboard = world.getClipboardSelection(world.getSelection());
					getToolkit().getSystemClipboard().setContents(newClipboard, null);
				}
				break;
			case KeyEvent.VK_V:
				if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
					Transferable clipContents = getToolkit().getSystemClipboard().getContents(null);
					try {
						if (clipContents.isDataFlavorSupported(BefungeClipboardSelection.cellBlockFlavor))
							world.pasteBlock((int[][]) clipContents.getTransferData(BefungeClipboardSelection.cellBlockFlavor), befungePosition);
						else
							world.pasteString((String) clipContents.getTransferData(DataFlavor.stringFlavor), befungePosition);
					} catch (NullPointerException | UnsupportedFlavorException | IOException exception) {
						exception.getLocalizedMessage();
						Toolkit.getDefaultToolkit().beep();
					}
				}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	//endregion
	//region MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {
		grabFocus();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		selectionCorner = null;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (selectionCorner != null)
			world.select(new BefungeCellBlock(selectionCorner, befungePosition));
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	//endregion
	//region MouseMotionListener
	@Override
	public void mouseDragged(MouseEvent e) {
		if (!hasFocus()) grabFocus();
		if (selectionCorner == null) selectionCorner = befungePosition;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}
	//endregion

}
