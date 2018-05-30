//package ps6; // cometn out

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
}
