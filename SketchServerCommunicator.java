
package ps6; // comment out

import java.awt.Color;
import java.io.*;
import java.net.Socket;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}
	
	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			
			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			// TODO: YOUR CODE HERE
			if(!server.isEmpty()) {
				String[] holdState = server.getWorldState().split("/n");
				for(int i=0; i<holdState.length;i++) {
					out.println(holdState[i]);
					System.out.println(holdState[i]);
				}
								
			}
			// Keep getting and handling messages from the client
			// TODO: YOUR CODE HERE
			String line;
			while((line = in.readLine()) != null) {
				System.out.println("received:" + line);
				String[] splitLine = line.split(",");
				Integer id = Integer.valueOf(splitLine[0]);
				if (id == -1) {
					id = server.getAddingId();
					System.out.println("new id:" + id);
					
				}
				String command = splitLine[1];
				// Put statments don't check to see if it is in the map
				if(command.equals("put")) {
					String shape = splitLine[2];
//					System.out.println(splitLine.length);
					if(splitLine.length == 6) {
//						System.out.println("inside");
//						System.out.println(id);
//						System.out.println(shape);
//						System.out.println(splitLine[3]);
//						System.out.println(splitLine[4]);
//						System.out.println(splitLine[5]);
						
						server.addToShapeMap(id, shape, Integer.valueOf(splitLine[3]), Integer.valueOf(splitLine[4]), new Color(Integer.valueOf(splitLine[5])));
					}
					else {
						server.addCompleteToShapeMap(id, shape, Integer.valueOf(splitLine[3]), Integer.valueOf(splitLine[4]), 
								Integer.valueOf(splitLine[5]), Integer.valueOf(splitLine[6]), new Color(Integer.valueOf(splitLine[7])));
					}
					System.out.println("added");
				}
				else if(command.equals("recolor")) {
					server.recolorKnownShape(id, new Color(Integer.valueOf(splitLine[2])));
				}
				else if(command.equals("delete")) {
					server.deleteKnownShape(id);
				}
				else if(command.equals("setCorners")) {
					//System.out.println("setting corners");
					server.updateKnownShapeCorners(id, splitLine[2], Integer.valueOf(splitLine[3]), 
							Integer.valueOf(splitLine[4]), Integer.valueOf(splitLine[5]), Integer.valueOf(splitLine[6]));
				}
				else if(command.equals("setEnd")) {
					server.updateKnownSegmentEnd(id, Integer.valueOf(splitLine[2]), Integer.valueOf(splitLine[3]));
				}
				else if(command.equals("addPoint")) {
					server.updateKnownPolylineEnd(id, Integer.valueOf(splitLine[2]), Integer.valueOf(splitLine[3]));
				}
				else if(command.equals("moveBy")) {
					server.updateKnownShapePosition(id, Integer.valueOf(splitLine[2]), Integer.valueOf(splitLine[3]));
				}
				line = id.toString();
				for(int i=1; i< splitLine.length; i++) {
					line += ","+splitLine[i];
				}
				
				server.broadcast(line);
				
//				if(server.retract) {
//					server.broadcast(server.retractionStatement);
//					server.retract = false;
//					System.out.println("retracted");
//				}
			
				
				
			}
			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
