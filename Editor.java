import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.swing.*;


/**
 * Client-server graphical editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
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
//	private Sketch sketch;						// holds and handles all the completed objects
	private Integer movingId = -1;				// current shape id (if any; else -1) being moved
	private Integer addingId = 0;
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged


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
	// 		return "rectangle,"+x1+","+y1+","+x2+","+y2+","+color;

	
	
	public Editor() {
		super("Graphical Editor");

//		sketch = new Sketch();

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
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
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

//	/**
//	 * Getter for the sketch instance variable
//	 */
//	public Sketch getSketch() {
//		return sketch;
//	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE
		for(Object number: shapeMap.keySet()) {
			shapeMap.get(number).draw(g);
		}
	}

	// Helpers for event handlers
	
	public void addToShapeMap(Integer i, String shape, int x, int y, Color color) {
		if(shape.equals("ellipse")) {
			shapeMap.put(i, new Ellipse(x, y, color));
		}
		else if(shape.equals("rectangle")) {
			shapeMap.put(i, new Rectangle(x, y, color));
		}
		else if(shape.equals("segment")) {
			Segment seg = new Segment(x, y, color);
			shapeMap.put(i, seg);
			
		}
		else if(shape.equals("polyline")) {
			shapeMap.put(i, new Polyline(x, y, color));
		} 
		else {
			System.out.println("weird shape:"+shape);
		}
		System.out.println(shapeMap);
		repaint();
	}
	
	public void addCompleteToShapeMap(Integer i, String shape, int x1, int y1, int x2, int y2, Color color) {
		if(shape.equals("ellipse")) {
			shapeMap.put(i, new Ellipse(x1, y1, x2, y2, color));
		}
		else if(shape.equals("rectange")) {
			shapeMap.put(i, new Rectangle(x1, y1,x2, y2, color));
		}
		else if(shape.equals("segment")) {
			shapeMap.put(i, new Segment(x1, y1, x2, y2, color));
		}
		else if(shape.equals("polyline")) {
			shapeMap.put(i, new Polyline(x1, y1, color));
			((Polyline) shapeMap.get(i)).addPoint(x2, y2);
		}
		repaint();
	}
	
	public void recolorKnownShape(Integer i, Color color) {
		shapeMap.get(i).setColor(color);
		repaint();
	}
	
	public void deleteKnownShape(Integer i) {
		shapeMap.remove(i);
		repaint();
	}
	
	public void updateKnownShapeCorners(Integer i, String shape, int x1, int y1, int x2, int y2) {
		if(shape.equals("ellipse")) {
			((Ellipse)(shapeMap.get(i))).setCorners(x1, y1, x2, y2);
		}
		else if(shape.equals("rectangle")) {
			((Rectangle)(shapeMap.get(i))).setCorners(x1, y1, x2, y2);
		}
		repaint();
	}
	
	public void updateKnownSegmentEnd(Integer i, int x, int y) {
		((Segment)(shapeMap.get(i))).setEnd(x, y);
		repaint();
	}
	
	public void updateKnownPolylineEnd(Integer i, int x, int y) {
		((Polyline)(shapeMap.get(i))).updateLastPoint(x, y);
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
			if(shapeType.equals("ellipse")) {
				comm.send(addingId+",put,"+shapeType+","+p.x+","+p.y+","+color.getRGB());
				drawFrom = p;

			} else if(shapeType.equals("rectangle")) {
				//shapeMap.put(addingId, new Rectangle(p.x,p.y,color));
				comm.send(addingId+",put,"+shapeType+","+p.x+","+p.y+","+color.getRGB());
				drawFrom = p;
				
			} else if(shapeType.equals("segment")){
				
				comm.send(addingId+",put,"+shapeType+","+p.x+","+p.y+","+color.getRGB());
				drawFrom = p;
			} else if(shapeType.equals("polyline")) {
				//shapeMap.put(addingId, new Polyline(p.x, p.y, color));
				comm.send(addingId+",put,"+shapeType+","+p.x+","+p.y+","+color.getRGB());
			}
		}
		
		
		// In moving mode, start dragging if clicked in the shape
		if(mode == Mode.MOVE) {
			for(Integer i: shapeMap.keySet()) {
				if(shapeMap.get(i).contains(p.x, p.y)) {
					movingId = i;
//					try {
//						handleDrag(new Point(p.x, p.y));
//					} 
//					catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}
			}
		}
		
		// In recoloring mode, change the shape's color if clicked in it
		if(mode == Mode.RECOLOR) {
			for(Object number: shapeMap.keySet()) {
				if(shapeMap.get(number).contains(p.x, p.y)) {
					shapeMap.get(number).setColor(color);
					// Write message to EditorCommunicator to change color
					comm.send(number+",recolor,"+color.getRGB());
				}
			}
		}
		// In deleting mode, delete the shape if clicked in it
		if(mode == Mode.DELETE) {
			for(Object number: shapeMap.keySet()) {
				if(shapeMap.get(number).contains(p.x, p.y)) {
					//shapeMap.remove(number);
					// Write message to EditorCommunicator to delete
					comm.send(number+",delete");
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
			Shape holdShape = shapeMap.get(addingId);
			System.out.println(shapeMap.get(addingId));
			if(shapeMap.get(addingId).getType().equals("ellipse")) {
				//((Ellipse)(holdShape)).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
				comm.send(addingId+",setCorners,"+shapeType+","+drawFrom.x+","+drawFrom.y+","+p.x+","+p.y);
			} else if(((Shape)(shapeMap.get(addingId))).getType().equals("rectangle")) {
				comm.send(addingId+",setCorners,"+shapeType+","+drawFrom.x+","+drawFrom.y+","+p.x+","+p.y);
				
			} else if(((Shape)(shapeMap.get(addingId))).getType().equals("segment")) {
				//((Segment)(holdShape)).setEnd(p.x, p.y);
				comm.send(addingId+",setEnd,"+p.x+","+p.y);
			} else if(((Shape)(shapeMap.get(addingId))).getType().equals("polyline")) {
				//((Polyline)(holdShape)).updateLastPoint(p.x, p.y);
				comm.send(addingId+",updateLastPoint,"+p.x+","+p.y);
			}
		}
		// In moving mode, shift the object and keep track of where next step is from
		if(mode == Mode.MOVE) {
			if(moveFrom == null) {
				moveFrom = p;
			}
			shapeMap.get(movingId).moveBy(p.x - moveFrom.x, p.y - moveFrom.y);
//			for(Object number: shapeMap.keySet()) {
//				if(shapeMap.get(number).contains(p.x, p.y)) {
//					shapeMap.get(number).moveBy(p.x - moveFrom.x, p.y - moveFrom.y);
//					comm.send(number+",moveBy,"+(p.x - moveFrom.x)+","+(p.y - moveFrom.y));
//				}
//			}
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
			if(shapeMap.get(addingId).getType().equals("polyline")) {
				((Polyline)shapeMap.get(addingId)).addPoint(x, y);
			}
			System.out.println("addingID: " + addingId + "; shapeType: " + shapeMap.get(addingId).getType());
			addingId++;
		}
		drawFrom = null;
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
