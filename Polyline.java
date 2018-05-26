package ps6;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 */
public class Polyline implements Shape {
	// TODO: YOUR CODE HERE
	private Color color;
	private ArrayList<Point> pointList = new ArrayList<Point>();

	/**
	 * Initial 0-length segment at a point
	 */
	public Polyline(int x1, int y1, Color color) {
		pointList.add(new Point(x1, y1));
		this.color = color;
	}

	/**
	 * Complete segment from one point to the other
	 */
	public Polyline(int x1, int y1, int x2, int y2, Color color) {
		pointList.add(new Point(x1, y1));
		pointList.add(new Point(x2, y2));
		this.color = color;
	}	
	@Override
	public void moveBy(int dx, int dy) {
		for(int i = 0; i < pointList.size(); i++) {
			pointList.get(i).x += dx;
			pointList.get(i).y += dy;
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}
	
	public void addPoint(int x, int y) {
		pointList.add(new Point(x, y));
	}
	
	/**
	 * Euclidean distance squared between (x1,y1) and (x2,y2)
	 */
	public static double dist2(double x1, double y1, double x2, double y2) {
		return (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1);
	}
	
	/**
	 * Helper method to compute the distance between a point (x,y) and a segment (x1,y1)-(x2,y2)
	 * http://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment
	 */
	public static double pointToSegmentDistance(int x, int y, int x1, int y1, int x2, int y2) {
		double l2 = dist2(x1, y1, x2, y2);
		if (l2 == 0) return Math.sqrt(dist2(x, y, x1, y1)); // segment is a point
		// Consider the line extending the segment, parameterized as <x1,y1> + t*(<x2,y2> - <x1,y1>).
		// We find projection of point <x,y> onto the line. 
		// It falls where t = [(<x,y>-<x1,y1>) . (<x2,y2>-<x1,y1>)] / |<x2,y2>-<x1,y1>|^2
		double t = ((x-x1)*(x2-x1) + (y-y1)*(y2-y1)) / l2;
		// We clamp t from [0,1] to handle points outside the segment.
		t = Math.max(0, Math.min(1, t));
		return Math.sqrt(dist2(x, y, x1+t*(x2-x1), y1+t*(y2-y1)));
	}
	
	@Override
	public boolean contains(int x, int y) {
		for(int i = 0; i < pointList.size() - 1; i++) {
			if(pointToSegmentDistance(x, y, pointList.get(i).x, pointList.get(i).y, pointList.get(i + 1).x, pointList.get(i + 1).y) < 3) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		for(int i = 0; i < pointList.size() - 1; i++) {
			g.drawLine(pointList.get(i).x, pointList.get(i).y, pointList.get(i + 1).x, pointList.get(i + 1).y);
		}
	}

	@Override
	public String toString() {
		String temp = "";
		for(Point p: pointList) {
			temp += "x: " + p.x + ", y: " + p.y;
		}
		return "PolyLine: " + temp + ", Color: " + color;
	}
}
