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
	private Message message;								// the state of the world
	private int addingId = 0;
	private boolean available = true;
	private boolean retract = false;
	private String retractionStatement = 0+",blah,rectangle,-1,-1,-16777216";
	
	public SketchServer(ServerSocket listen) {
		this.listen = listen;
		message = new Message();
		comms = new ArrayList<SketchServerCommunicator>();
	}
	
	/**
	 * Gets the next ID to be assigned
	 */
	public synchronized int getAddingId() {
		addingId++;
		return addingId;
	}

	/**
	 * returns whether or not a retraction is needed
	 */
	public boolean getRetraction() {
		return retract;
	}
	
	/**
	 * sets whether or not a retraction is needed
	 */
	public void setRetraction(boolean state) {
		retract = state;
	}
	
	/**
	 * returns String that contains command to retract erroneous change
	 */
	public String getRetractionStatement() {
		return retractionStatement;
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
	
	/**
	 * @return String representation of the server's shapeMap 
	 */
	public synchronized String getWorldState() {
		String hold = "";
		for(Integer i: shapeMap.keySet()) {
			hold += i+","+"put,"+shapeMap.get(i).toString() +"/n";
		}
		return hold;
	}
	
	/**
	 * Returns whether or not there are no shapes in the shapeMap
	 */
	public Boolean isEmpty() {
		return shapeMap.isEmpty();
	}
	
	/**
	 * Adds the communicator to the list of current communicators
	 */
	public synchronized void addCommunicator(SketchServerCommunicator comm) {
		comms.add(comm);
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
	
	/**
	 * Adds a new shape to the server's map
	 */
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
		available = false;
	}
	
	/**
	 * Updates the color of a shape in the server's shapeMap
	 */
	public void recolorKnownShape(Integer i, Color color) {
		if(available) {
			shapeMap.get(i).setColor(color);
			if(!retract) {
				retractionStatement = i+",recolor,"+color.getRGB();
			}
		} 
		else {
			retract = true;
			available = true;
		}
		
	}
	
	/**
	 * Removes a shape from the server's shapeMap
	 */
	public void deleteKnownShape(Integer i) {
		if(available) {
			shapeMap.remove(i);
			if(!retract) {
				retractionStatement = i+",delete";
			}
		} 
		else {
			retract = true;
			available = true;
		}	
	}
	
	/**
	 * Updates the corners of an ellipse or rectangle in tje server's shapeMap
	 */
	public void updateKnownShapeCorners(Integer i, String shape, int x1, int y1, int x2, int y2) {
		if(available) {
			if(shape.equals("ellipse")) {
				((Ellipse)(shapeMap.get(i))).setCorners(x1, y1, x2, y2);
			}
			if(shape.equals("rectangle")) {
				((Rectangle)(shapeMap.get(i))).setCorners(x1, y1, x2, y2);
			}
			if (!retract) {
				retractionStatement = i+",setCorners,"+shape+","+x1+","+y1+","+x2+","+y2;
			}
		}
		else {
			retract = true;
			available = true;
		}
	}
	
	/**
	 * Updates the endpoint of a segment in the server's shapeMap
	 */
	public void updateKnownSegmentEnd(Integer i, int x, int y) {
		if(available) {
			((Segment)(shapeMap.get(i))).setEnd(x, y);
			if (!retract) {
				retractionStatement = i+",setEnd,"+x+","+y;
			}
		} 
		else {
			retract = true;
			available = true;
		}
	}
	
	/**
	 * Update's the last point added to a polyline in the server's shapeMap
	 */
	public void updateKnownPolylineEnd(Integer i, int x, int y) {
		//((Polyline)(shapeMap.get(i))).addPoint(x, y);
		if(available) {
			((Polyline)(shapeMap.get(i))).addPoint(x, y);
			if (!retract) {
				retractionStatement = i+",polyRetract,"+x+","+y;
			}
		} 
		else {
			retract = true;
			available = true;
		}
		
	}
	
	/**
	 * Update's the position of a shape in the server's shapeMap
	 */
	public void updateKnownShapePosition(Integer i, int x, int y) {
		shapeMap.get(i).moveBy(x, y);
	}
	
	public static void main(String[] args) throws Exception {
		new SketchServer(new ServerSocket(4242)).getConnections();

	}
}
