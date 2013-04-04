package org.micoli.phone.tools;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * The Class DraggableWindow.
 */
public class DraggableWindow {

	/** The main frame. */
	private JFrame mainFrame;

	/** The component. */
	private Component fComponent;

	/** The d x. */
	private int dX;

	/** The d y. */
	private int dY;

	/**
	 * Instantiates a new draggable window.
	 *
	 * @param mainFrame the main frame
	 */
	public DraggableWindow(JFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
		fComponent = mainFrame.getComponent(0);
		fComponent.addMouseListener(createMouseListener());
		fComponent.addMouseMotionListener(createMouseMotionListener());
	}

	/**
	 * Creates the mouse listener.
	 *
	 * @return the mouse listener
	 */
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

	/**
	 * Creates the mouse motion listener.
	 *
	 * @return the mouse motion adapter
	 */
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
