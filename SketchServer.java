//package ps6; // comment out

import java.net.*;
import java.util.*;
import java.awt.Color;
import java.io.*;

/**
 * A server to handle sketches: getting requests from the clients,
 * updating the overall state, and passing them on to the clients
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 */
public class SketchServer {
	private ServerSocket listen;						// for accepting connections
	private ArrayList<SketchServerCommunicator> comms;	// all the connections with clients
	private Sketch sketch;								// the state of the world
	private int addingId = -1;
	
	public SketchServer(ServerSocket listen) {
		System.out.println("Server dinosaur");
		this.listen = listen;
		sketch = new Sketch();
		comms = new ArrayList<SketchServerCommunicator>();
	}
	
//	public void printAtWill() {
//		System.out.println("WHERE IS MY WALRUS?");
//	}

	public Sketch getSketch() {
		return sketch;
	}
	public synchronized int getAddingId() {
		addingId++;
		//System.out.println("this is the adding Id" + addingId);
		return addingId;
	}
	
	private HashMap<Integer, Shape> shapeMap = new HashMap<Integer, Shape>();
	
	/**
	 * The usual loop of accepting connections and firing off new threads to handle them
	 */
	public void getConnections() throws IOException {
		System.out.println("server ready for connections");
		while (true) {
			SketchServerCommunicator comm = new SketchServerCommunicator(listen.accept(), this);
			comm.setDaemon(true);
			System.out.println("start");
			comm.start();
			addCommunicator(comm);
		}
	}
	public synchronized String getWorldState() {
		String hold = "";
		for(Integer i: shapeMap.keySet()) {
			hold += i+","+"put,"+shapeMap.get(i).toString() +"/n";
		}
		return hold;
	}
	
	public Boolean isEmpty() {
		return shapeMap.isEmpty();
	}
	/**
	 * Adds the communicator to the list of current communicators
	 */
	public synchronized void addCommunicator(SketchServerCommunicator comm) {
		comms.add(comm);
//		for(Integer i: shapeMap.keySet()) {
//			comm.send(i+","+"put"+shapeMap.get(i).toString());
//			System.out.println("addding new client");
//		}
	}

	/**
	 * Removes the communicator from the list of current communicators
	 */
	public synchronized void removeCommunicator(SketchServerCommunicator comm) {
		comms.remove(comm);
	}

	/**
	 * Sends the message from the one communicator to all (including the originator)
	 */
	public synchronized void broadcast(String msg) {
		for (SketchServerCommunicator comm : comms) {
			comm.send(msg);
		}
	}
	
	public void addToShapeMap(Integer i, String shape, int x, int y, Color color) {
		if(shape.equals("ellipse")) {
			shapeMap.put(i, new Ellipse(x, y, color));
		}
		if(shape.equals("rectangle")) {
			shapeMap.put(i, new Rectangle(x, y, color));
		}
		if(shape.equals("segment")) {
			shapeMap.put(i, new Segment(x, y, color));
		}
		if(shape.equals("polyline")) {
			shapeMap.put(i, new Polyline(x, y, color));
		}
	}
	
	public void addCompleteToShapeMap(Integer i, String shape, int x1, int y1, int x2, int y2, Color color) {
		if(shape.equals("ellipse")) {
			shapeMap.put(i, new Ellipse(x1, y1, x2, y2, color));
		}
		else if(shape.equals("rectangle")) {
			shapeMap.put(i, new Rectangle(x1, y1,x2, y2, color));
		}
		else if(shape.equals("segment")) {
			shapeMap.put(i, new Segment(x1, y1, x2, y2, color));
		}
		else if(shape.equals("polyline")) {
			shapeMap.put(i, new Polyline(x1, y1, color));
			((Polyline) shapeMap.get(i)).addPoint(x2, y2);
		}
	}
	
	public void recolorKnownShape(Integer i, Color color) {
		Sketch.recolorKnownShape(addingId, color, shapeMap);
	}
	
	public void deleteKnownShape(Integer i) {
		shapeMap.remove(i);
	}
	
	public void updateKnownShapeCorners(Integer i, String shape, int x1, int y1, int x2, int y2) {
		if(shape.equals("ellipse")) {
			((Ellipse)(shapeMap.get(i))).setCorners(x1, y1, x2, y2);
		}
		if(shape.equals("rectangle")) {
			((Rectangle)(shapeMap.get(i))).setCorners(x1, y1, x2, y2);
		}
	}
	
	public void updateKnownSegmentEnd(Integer i, int x, int y) {
		((Segment)(shapeMap.get(i))).setEnd(x, y);
	}
	
	public void updateKnownPolylineEnd(Integer i, int x, int y) {
		((Polyline)(shapeMap.get(i))).updateLastPoint(x, y);
	}
	
	public void updateKnownShapePosition(Integer i, int x, int y) {
		shapeMap.get(i).moveBy(x, y);
	}
	
	public static void main(String[] args) throws Exception {
		new SketchServer(new ServerSocket(4242)).getConnections();

	}
}
