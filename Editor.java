//package ps6; // comment this out before pushing

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * Client-server graphical editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 * @author Eitan Vilker and Dustin Wilen
 */

public class Editor extends JFrame {	
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	//Goal of this map is to store the shapes crated with unique integers as keys.
	private HashMap<Integer,Shape> shapeMap = new HashMap<Integer, Shape>();
	
	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Integer movingId = -1;				// current shape id (if any; else -1) being moved
	private Integer addingId = -1;				// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged
	private static boolean canDraw = true;

	// Communication
	private EditorCommunicator comm;			// communication with the sketch server
	
	// sending format "i,command type,command"
	// put format "shapeType,parameter 1, 2"
	// put format "shapeType,parameter 1, 2, 3, 4"
	// recolor format "color(int)"
	// delete format empty
	// setCorners format 
	// setEnd format "x,y"
	// addPoint format "x,y"
	// moveBy format "dx,dy"
	
	public Editor() {
		super("Graphical Editor");

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};
		
		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease(event.getX(), event.getY());
			}
		});		

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				try {
					handleDrag(event.getPoint());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "polyline", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE
		try {
			if(canDraw) {
				for(Integer number: shapeMap.keySet()) {
					shapeMap.get(number).draw(g);
				}
			}
		} catch (Exception e) {
			// do nothing; all will be fixed in a millisecond
		}
	}

	// Helpers for event handlers
	
	public void addToShapeMap(Integer i, String shape, int x, int y, Color color) {
		canDraw = false;
		
		if(shape.equals("ellipse")) {
			shapeMap.put(i, new Ellipse(x, y, color));
		}
		else if(shape.equals("rectangle")) {
			shapeMap.put(i, new Rectangle(x, y, color));
		}
		else if(shape.equals("segment")) {
			shapeMap.put(i, new Segment(x, y, color));
			
		}
		else if(shape.equals("polyline")) {
			shapeMap.put(i, new Polyline(x, y, color));
		} 
		else {
			System.out.println("weird shape:"+shape);
		}
		repaint();
		addingId = i;
		canDraw = true;
		try {
			Thread.sleep(10);
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addCompleteToShapeMap(Integer i, String shape, int x1, int y1, int x2, int y2, Color color) {
		canDraw = false;
		if(shape.equals("ellipse")) {
			shapeMap.put(i, new Ellipse(x1, y1, x2, y2, color));
		}
		else if(shape.equals("rectangle")) {
			shapeMap.put(i, new Rectangle(x1, y1, x2, y2, color));
		}
		else if(shape.equals("segment")) {
			shapeMap.put(i, new Segment(x1, y1, x2, y2, color));
		}
		else if(shape.equals("polyline")) {
			shapeMap.put(i, new Polyline(x1, y1, color));
			((Polyline) shapeMap.get(i)).addPoint(x2, y2);
		}
		repaint();
		addingId = i;
		canDraw = true;
	}
	
	public void addCompletePolylineToShapeMap(Integer i, ArrayList<Integer> coordinateList, Color color) {
		shapeMap.put(i, new Polyline(coordinateList.get(0), coordinateList.get(1), color));
		for(int j = 2; j < coordinateList.size(); j+= 2) {
			((Polyline) shapeMap.get(i)).addPoint(j, j + 1);
		}
	}
	
	public void recolorKnownShape(Integer i, Color color) {
		Sketch.recolorMessage(comm, i, color);
		repaint();
	}
	
	public void deleteKnownShape(Integer i) {
		canDraw = false;
		shapeMap.remove(i);
		repaint();
		canDraw = true;
	}
	
	public void updateKnownShapeCorners(Integer i, String shape, int x1, int y1, int x2, int y2) {
		canDraw = false;
		if(shape.equals("ellipse")) {
			((Ellipse)(shapeMap.get(i))).setCorners(x1, y1, x2, y2);
		}
		else if(shape.equals("rectangle")) {
			((Rectangle)(shapeMap.get(i))).setCorners(x1, y1, x2, y2);
		}
		repaint();
		canDraw = true;
	}
	
	public void updateKnownSegmentEnd(Integer i, int x, int y) {
		((Segment)(shapeMap.get(i))).setEnd(x, y);
		repaint();
	}
	
	public void updateKnownPolylineEnd(Integer i, int x, int y) {
		((Polyline)(shapeMap.get(i))).addPoint(x, y);
		repaint();
	}
	
	public void updateKnownShapePosition(Integer i, int x, int y) {
		shapeMap.get(i).moveBy(x, y);
		repaint();
	}
	
	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		// TODO: YOUR CODE HERE
		// In drawing mode, start drawing a new shape
		if(mode == Mode.DRAW) {
			Sketch.drawNewMessage(comm, shapeType, p, color);
			drawFrom = p;
		}
		
		// In moving mode, start dragging if clicked in the shape
		if(mode == Mode.MOVE) {
			for(Integer i: shapeMap.keySet()) {
				if(shapeMap.get(i).contains(p.x, p.y)) {
					movingId = i;
				}
			}
		}
		
		// In recoloring mode, change the shape's color if clicked in it
		if(mode == Mode.RECOLOR) {
			for(Object number: shapeMap.keySet()) {
				if(shapeMap.get(number).contains(p.x, p.y)) {
					shapeMap.get(number).setColor(color);
					// Write message to EditorCommunicator to change color
					Sketch.recolorMessage(comm, addingId, color);
				}
			}
		}
		
		// In deleting mode, delete the shape if clicked in it
		if(mode == Mode.DELETE) {
			for(Object number: shapeMap.keySet()) {
				if(shapeMap.get(number).contains(p.x, p.y)) {
					//shapeMap.remove(number);
					// Write message to EditorCommunicator to delete
					Sketch.deleteMessage(comm, addingId);
				}
			}
			moveFrom = null;
			drawFrom = null;
		}
		// Be sure to refresh the canvas (repaint) if the appearance has changed
		repaint();
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) throws Exception {
		// TODO: YOUR CODE HERE
		// In drawing mode, revise the shape as it is stretched out
		if(mode == Mode.DRAW) {
			try {
				String type = shapeMap.get(addingId).getType();
				
				//((Ellipse)(holdShape)).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
				Sketch.drawDragMessage(comm, addingId, type, p, drawFrom);
			}
			catch (NullPointerException e){
				// Do nothing; the program just needs time to process the massive amount of information that is the polyline
			}
		}
		// In moving mode, shift the object and keep track of where next step is from
		if(mode == Mode.MOVE) {
			if(moveFrom == null) {
				moveFrom = p;
			}
			Sketch.moveDragMessage(comm, p, moveFrom, movingId);			

		}
		// Be sure to refresh the canvas (repaint) if the appearance has changed
		moveFrom = p;
		repaint();
	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it		
	 */
	private void handleRelease(int x, int y) {
		// TODO: YOUR CODE HERE
		// In moving mode, stop dragging the object
		moveFrom = null;
		if(mode == Mode.DRAW) {

		}
		drawFrom = null;
		movingId = -1;
		// Be sure to refresh the canvas (repaint) if the appearance has changed
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});	
	}
}
