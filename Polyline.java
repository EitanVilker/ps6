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

	@Override
	public void moveBy(int dx, int dy) {
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
	
	@Override
	public boolean contains(int x, int y) {
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
