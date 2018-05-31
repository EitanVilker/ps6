import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;

public class Message {
	
	/**
	 * Messages server to draw a new shape
	 */
	public static void drawNewMessage(EditorCommunicator comm, String shape, Point p, Color color) {
		comm.send(-1+",put,"+shape+","+p.x+","+p.y+","+color.getRGB());
	}
	
	/**
	 * Messages server to change the dimesnions of an existing shape
	 */
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
			comm.send(addingId+",addPoint,"+p.x+","+p.y);
		}
	}
	
	/**
	 * Messages server to move an existing shape
	 */
	public static void moveDragMessage(EditorCommunicator comm, Point p, Point moveFrom, int movingId) {
		if(movingId != -1) {
			comm.send(movingId+",moveBy,"+(p.x - moveFrom.x)+","+(p.y - moveFrom.y));
		}
	}
	
	/**
	 * Messages server to change the color of an existing shape
	 */
	public static void recolorMessage(EditorCommunicator comm, Integer addingId, Color color) {
		comm.send(addingId+",recolor,"+color.getRGB());
	}
	
	/**
	 * Messages server to delete a shape
	 */
	public static void deleteMessage(EditorCommunicator comm, Integer addingId) {
		comm.send(addingId+",delete");
	}
}
