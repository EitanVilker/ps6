package ps6;
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
	
	public SketchServer(ServerSocket listen) {
		this.listen = listen;
		sketch = new Sketch();
		comms = new ArrayList<SketchServerCommunicator>();
	}

	public Sketch getSketch() {
		return sketch;
	}
	
	private static HashMap<Integer, Shape> shapeMap = new HashMap<Integer, Shape>();
	
	/**
	 * The usual loop of accepting connections and firing off new threads to handle them
	 */
	public void getConnections() throws IOException {
		System.out.println("server ready for connections");
		while (true) {
			SketchServerCommunicator comm = new SketchServerCommunicator(listen.accept(), this);
			comm.setDaemon(true);
			comm.start();
			addCommunicator(comm);
		}
	}

	/**
	 * Adds the communicator to the list of current communicators
	 */
	public synchronized void addCommunicator(SketchServerCommunicator comm) {
		comms.add(comm);
		for(Integer i: shapeMap.keySet()) {
			Shape shape = shapeMap.get(i);
			String[] shapeString = shape.toString().split(",");
			if(shape.getType().equals("ellipse")){
				comm.send(i+","+"put"+shape.getType()+","+shapeString[2]+","+shapeString[3]+","+shapeString[4]+","+shapeString[5]+","+shapeString[6]);
			}
			else if(shape.getType().equals("rectangle")){
				comm.send(i+","+"put"+shape.getType()+","+shapeString[2]+","+shapeString[3]+","+shapeString[4]+","+shapeString[5]+","+shapeString[6]);
			}
		}
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
		if(shape.equals("rectange")) {
			shapeMap.put(i, new Rectangle(x, y, color));
		}
		if(shape.equals("segment")) {
			shapeMap.put(i, new Segment(x, y, color));
		}
		if(shape.equals("polyline")) {
			shapeMap.put(i, new Polyline(x, y, color));
		}
	}
	
	public void recolorKnownShape(Integer i, Color color) {
		shapeMap.get(i).setColor(color);
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
		((Polyline)(shapeMap.get(i))).addPoint(x, y);
	}
	
	public void updateKnownShapePosition(Integer i, int x, int y) {
		shapeMap.get(i).moveBy(x, y);
	}
	
	public static void main(String[] args) throws Exception {
		new SketchServer(new ServerSocket(4242)).getConnections();
		System.out.println("waiting for someone to connect");
		ServerSocket listen = new ServerSocket(4242);
		// When someone connects, create a specific socket for them
		Socket sock = listen.accept();
		System.out.println("someone connected");

		// Now talk with them
		PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		out.println("Let's create a masterpiece!");
		
		System.out.println("client hung up");

		// Clean up shop
		out.close();
		in.close();
		sock.close();
		listen.close();
	}
}
