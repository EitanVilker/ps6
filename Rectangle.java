import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 * A rectangle-shaped Shape
 * Defined by an upper-left corner (x1,y1) and a lower-right corner (x2,y2)
 * with x1<=x2 and y1<=y2
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author CBK, updated Fall 2016
 */
public class Rectangle implements Shape {
	// TODO: YOUR CODE HERE
	Color color;
	private int x1, y1, x2, y2;
	private String type = "rectangle";
	
	public Rectangle(int x1, int y1, Color color) {
		this.x1 = x1; this.y1 = y1;
		this.color = color;
	}
	
	public Rectangle(int x1, int y1, int x2, int y2, Color color) {
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
		ensureCorrectBounds();
		this.color = color;
	}
	
	public void ensureCorrectBounds(){
		if(x1 > x2) {
			int temp = x1;
			x1 = x2;
			x2 = temp;
		}
		if(y1 > y2) {
			int temp = y1;
			y1 = y2;
			y2 = temp;
		}
	}
	
	@Override
	public void moveBy(int dx, int dy) {
		x1 += dx; x2 += dx;
		y1 += dy; y2 += dy;
		ensureCorrectBounds();
	}
	@Override
	public String getType() {
		return type;
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}
		
	@Override
	public boolean contains(int x, int y) {
		if(x1 -3 < x && x < x2 + 3 && y1 - 3 < y && y < y2 + 3) {
			return true;
		}
		return false;
	}
	public void setCorners(int x1,int y1, int x2, int y2) {
		this.x1 = x1; this.x2 = x2; this.y1 = y1; this.y2 = y2;
		ensureCorrectBounds();
	}
	public Point getTopLeft() {
		return new Point(x1,y1);
	}
	public Point getBottomRight() {
		return new Point(x2,y2);
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		g.drawRect(x1, y1, x2 - x1, y2 - y1);
	}

	public String toString() {
		return "rectangle,"+x1+","+y1+","+x2+","+y2+","+color;
	}
}
