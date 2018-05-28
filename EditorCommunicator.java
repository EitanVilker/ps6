package ps6;
import java.awt.Color;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;

/**
 * Handles communication to/from the server for the editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;		// to server
	private BufferedReader in;		// from server
	protected Editor editor;		// handling communication for


	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		}
		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		out.println(msg);
	}
	// key,command,command info
	
	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {
			// Handle messages
			// TODO: YOUR CODE HERE
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println("received:" + line);
				String[] splitLine = line.split(",",3);
				Integer id = Integer.getInteger(splitLine[0]);
				String shape = splitLine[1];
				// Put statments don't check to see if it is in the map
				if(shape.equals("put")) {
					editor.addToShapeMap(id, shape, Integer.getInteger(splitLine[2]), Integer.getInteger(splitLine[3]), new Color(Integer.getInteger(splitLine[4])));
					editor.repaint();
				}
				if(shape.equals("recolor")) {
					editor.recolorKnownShape(id, new Color(Integer.getInteger(splitLine[2])));
				}
				else if(shape.equals("delete")) {
					editor.deleteKnownShape(id);
				}
				else if(shape.equals("setCorners")) {
					editor.updateKnownShapeCorners(id, shape, Integer.getInteger(splitLine[2]), 
							Integer.getInteger(splitLine[3]), Integer.getInteger(splitLine[4]), Integer.getInteger(splitLine[5]));
				}
				else if(shape.equals("setEnd")) {
					editor.updateKnownSegmentEnd(id, Integer.getInteger(splitLine[2]), Integer.getInteger(splitLine[3]));
				}
				else if(shape.equals("addPoint")) {
					editor.updateKnownPolylineEnd(id, Integer.getInteger(splitLine[2]), Integer.getInteger(splitLine[3]));
				}
				else if(shape.equals("moveBy")) {
					editor.updateKnownShapePosition(id, Integer.getInteger(splitLine[2]), Integer.getInteger(splitLine[3]));
				}
			Thread.sleep(5000);
			}
		}
		catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("server hung up");
		}
	}

	// Send editor requests to the server
	// TODO: YOUR CODE HERE
//	send(something, this code isn't meant to work);
	
}
