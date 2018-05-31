//package ps6;


import java.awt.Color;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

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
		System.out.println("run begun");
		try {
			//System.out.println("there is indeed a try");
			//editor.addToShapeMap(-2, "ellipse", -1, -1, new Color(-16777216));
			// Handle messages
			// TODO: YOUR CODE HERE
			String line;
			while ((line = in.readLine()) != null) {
				try {
					String[] splitLine = line.split(",");
					Integer id = Integer.valueOf(splitLine[0]);
					String command = splitLine[1];
					// Put statments don't check to see if it is in the map
					if(command.equals("put")) {
						String shape = splitLine[2];
						
						if(splitLine.length == 6) {	
							editor.addToShapeMap(id, shape, Integer.valueOf(splitLine[3]), Integer.valueOf(splitLine[4]), new Color(Integer.valueOf(splitLine[5])));
						}
	
						else if(splitLine.length == 8){
							editor.addCompleteToShapeMap(id, shape, Integer.valueOf(splitLine[3]), Integer.valueOf(splitLine[4]), 
									Integer.valueOf(splitLine[5]), Integer.valueOf(splitLine[6]), new Color(Integer.valueOf(splitLine[7])));
						}
						
						else {
							ArrayList<Integer> polylineCoordinateList = new ArrayList<Integer>();
							for(int i = 3; i < splitLine.length; i ++) {
								if(splitLine[i].equals("]")) {
									break;
								}
								polylineCoordinateList.add(Integer.valueOf(splitLine[i]));
								
							}
							editor.addCompletePolylineToShapeMap(id, polylineCoordinateList, new Color(Integer.valueOf(splitLine[splitLine.length - 1])));
						}
					}
					else if(command.equals("recolor")) {
						editor.recolorKnownShape(id, new Color(Integer.valueOf(splitLine[2])));
					}
					else if(command.equals("delete")) {
						editor.deleteKnownShape(id);
					}
					else if(command.equals("setCorners")) {
						editor.updateKnownShapeCorners(id, splitLine[2], Integer.valueOf(splitLine[3]), 
								Integer.valueOf(splitLine[4]), Integer.valueOf(splitLine[5]), Integer.valueOf(splitLine[6]));
					}
					else if(command.equals("setEnd")) {
						editor.updateKnownSegmentEnd(id, Integer.valueOf(splitLine[2]), Integer.valueOf(splitLine[3]));
					}
					else if(command.equals("addPoint")) {
						editor.updateKnownPolylineEnd(id, Integer.valueOf(splitLine[2]), Integer.valueOf(splitLine[3]));
					}
					else if(command.equals("moveBy")) {
						editor.updateKnownShapePosition(id, Integer.valueOf(splitLine[2]), Integer.valueOf(splitLine[3]));
					} 
					else if(command.equals("polyRetract")) {
						editor.polyLineRetract(id, Integer.valueOf(splitLine[2]), Integer.valueOf(splitLine[3]));
					}
				} catch (Exception e) {
					System.out.println(line);
				}

			}
		}
		catch (Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		finally {
			System.out.println("server hung up");
		}
	}

	// Send editor requests to the server
	// TODO: YOUR CODE HERE
	// This is handled within the Editor class using methods condensed within the Sketch class

}
