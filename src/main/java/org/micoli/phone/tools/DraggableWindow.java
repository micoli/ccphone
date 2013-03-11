package org.micoli.phone.tools;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class DraggableWindow {
	private JFrame mainFrame;
	private Component fComponent;
	private int dX;
	private int dY;

	public DraggableWindow(JFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
		fComponent = mainFrame.getComponent(0);
		fComponent.addMouseListener(createMouseListener());
		fComponent.addMouseMotionListener(createMouseMotionListener());
	}

	private MouseListener createMouseListener() {
		return new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				Point clickPoint = new Point(e.getPoint());
				SwingUtilities.convertPointToScreen(clickPoint, fComponent);
				dX = clickPoint.x - mainFrame.getX();
				dY = mainFrame.getY();
			}
		};
	}

	private MouseMotionAdapter createMouseMotionListener() {
		return new MouseMotionAdapter() {

			public void mouseDragged(MouseEvent e) {
				Point dragPoint = new Point(e.getPoint());
				SwingUtilities.convertPointToScreen(dragPoint, fComponent);
				mainFrame.setLocation(dragPoint.x - dX, dY);
			}
		};
	}

}
