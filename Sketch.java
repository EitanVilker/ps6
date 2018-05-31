//package ps6; // comment out

import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;

public class Sketch {
	// This HashMap should constantly be copying from the server's version in order to draw
	private HashMap<Integer,Shape> subShapeMap = new HashMap<Integer, Shape>();
	
	public Sketch() {
	}
	public void update(HashMap<Integer,Shape> input) {
		subShapeMap = input;
	}
	public HashMap<Integer,Shape> getHashMap() {
		return subShapeMap;
	}
	
	public static void drawNewMessage(EditorCommunicator comm, String shape, Point p, Color color) {
		comm.send(-1+",put,"+shape+","+p.x+","+p.y+","+color.getRGB());
	}
	
	public static void drawDragMessage(EditorCommunicator comm, int addingId, String type, Point p, Point drawFrom) {
		if((type.equals("ellipse"))) {
			comm.send(addingId+",setCorners,"+type+","+drawFrom.x+","+drawFrom.y+","+p.x+","+p.y);
		}
		else if((type.equals("rectangle"))) {
			comm.send(addingId+",setCorners,"+type+","+drawFrom.x+","+drawFrom.y+","+p.x+","+p.y);
		}
		else if((type.equals("segment"))) {
			comm.send(addingId+",setEnd,"+p.x+","+p.y);
		}
		else if((type.equals("polyline"))) {
			comm.send(addingId+",updateLastPoint,"+p.x+","+p.y);
		}
	}
	
	public static void moveDragMessage(EditorCommunicator comm, Point p, Point moveFrom, int movingId) {
		comm.send(movingId+",moveBy,"+(p.x - moveFrom.x)+","+(p.y - moveFrom.y));
	}
	
	public static void recolorMessage(EditorCommunicator comm, Integer addingId, Color color) {
		comm.send(addingId+",recolor,"+color.getRGB());
	}
	
	public static void deleteMessage(EditorCommunicator comm, Integer addingId) {
		comm.send(addingId+",delete");
	}
	
	public static void recolorKnownShape(Integer i, Color color, HashMap<Integer, Shape> shapeMap) {
		shapeMap.get(i).setColor(color);
	}
	
	
}
