package io.github.hactarce;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {
		MainGUI gui = new MainGUI(new BefungeVector(80, 25));
		JFrame frame = new JFrame("ZeFunge");
		gui.show(frame);
	}

}
