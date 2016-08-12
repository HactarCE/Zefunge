package io.github.hactarce;

import javax.swing.*;
import java.awt.*;

/**
 * Created by OWNER on 8/8/2016.
 */
public class MainGUI {

	BefungeWorld world;

	JPanel panel_main;
	JSlider slider_simSpeed;
	JLabel label_simSpeed;
	JButton button_toggleRun;
	JButton button_step;
	private JButton button_reset;
	private JPanel panel_cellGrid;
	private JButton button_down;
	private JButton button_left;
	private JButton button_right;
	private JButton button_up;
	private JLabel label_delta;
	private JLabel label_position;
	private JLabel label_selection;
	private JTextArea textArea_output;
	private Icon runIcon = new ImageIcon(getClass().getResource("/icons/run.png"));
	private Icon stopIcon = new ImageIcon(getClass().getResource("/icons/stop.png"));

	MainGUI(BefungeVector worldSize) {
		panel_cellGrid.setMinimumSize(new Dimension(480, 360));
		panel_cellGrid.setLayout(new GridBagLayout());
		world = new BefungeWorld(worldSize, panel_cellGrid);
		world.setOnRunStateChanged(new Runnable() {
			@Override
			public void run() {
				if (world.isRunning()) {
					button_toggleRun.setIcon(stopIcon);
					button_toggleRun.setText("Stop");
				} else {
					button_toggleRun.setIcon(runIcon);
					button_toggleRun.setText("Run");
				}
			}
		});
		world.setOnEnd(new Runnable() {
			@Override
			public void run() {
				button_toggleRun.setEnabled(false);
				button_step.setEnabled(false);
			}
		});
		world.setOnReset(new Runnable() {
			@Override
			public void run() {
				button_toggleRun.setEnabled(true);
				button_step.setEnabled(true);
				updatePosAndDelta();
				updateSelection();
				updateOutput();
				world.focusCell(world.instructionPointer.getPosition());
			}
		});
		world.setOnRegionSelect(this::updateSelection);
		world.setOnOutput(this::updateOutput);
		world.instructionPointer.setOnChange(this::updatePosAndDelta);
		button_toggleRun.addActionListener(e -> world.toggleRun());
		button_toggleRun.setText("Run");
		button_step.addActionListener(e -> world.step());
		button_reset.addActionListener(e -> world.restart());
		button_down.addActionListener(e -> world.instructionPointer.setDelta(BefungeIP.SOUTH()));
		button_left.addActionListener(e -> world.instructionPointer.setDelta(BefungeIP.WEST()));
		button_right.addActionListener(e -> world.instructionPointer.setDelta(BefungeIP.EAST()));
		button_up.addActionListener(e -> world.instructionPointer.setDelta(BefungeIP.NORTH()));
		slider_simSpeed.addChangeListener(e -> updateSimSpeed());
		slider_simSpeed.setValue(5);
		updatePosAndDelta();
		updateSelection();
	}

	void show(JFrame frame) {
		frame.getContentPane().add(panel_main);
		frame.pack();
		frame.setVisible(true);
	}

	private void updateSimSpeed() {
		world.simSpeed = slider_simSpeed.getValue();
		label_simSpeed.setText(String.format("Speed: %d steps per second", slider_simSpeed.getValue()));
	}

	private void updatePosAndDelta() {
		BefungeIP ip = world.instructionPointer;
		BefungeVector delta = ip.getDelta();
		label_delta.setText(String.format("Delta: %s (%d, %d)",
				delta.equals(BefungeIP.EAST()) ? "east"
						: delta.equals(BefungeIP.NORTH()) ? "north"
						: delta.equals(BefungeIP.SOUTH()) ? "south"
						: delta.equals(BefungeIP.WEST()) ? "west"
						: "flying",
				delta.x, delta.y
		) + (ip.isOutOfBounds() ? " OUT OF BOUNDS" : ""));
		BefungeVector pos = ip.getPosition();
		label_position.setText(String.format("(%d, %d) %d [%c]", pos.x, pos.y, world.getCell(pos).value, world.getCell(pos).character));
	}

	private void updateSelection() {
		BefungeCellBlock selection = world.getSelection();
		label_selection.setText(String.format("Selection: (%d, %d, %d, %d); size %dx%d",
				selection.left, selection.top, selection.right, selection.bottom,
				selection.right - selection.left + 1, selection.bottom - selection.top + 1
		));
	}

	private void updateOutput() {
		textArea_output.setText(world.getOutput());
	}

}
