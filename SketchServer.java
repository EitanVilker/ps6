package ps6;
import java.net.*;
import java.util.*;
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
		String line;
		while ((line = in.readLine()) != null) {
			System.out.println("received:" + line);
			String[] splitLine = line.split(" ");
			if(shapeMap.containsKey(splitLine[0])) {
				if(shapeMap.get(splitLine[0]).getType().equals("ellipse")) {
					out.println("hi! your word means: " + theDictionary.get(splitLine[1]));
				}

				else {
					out.println("Try a real word next time, scrub.");
				}

			}
			if(splitLine[0].equals("SET")) {
				if(!splitLine[1].equals(null)) {
					String tempString = "";
					for(int i = 2; i < splitLine.length; i++) {
						tempString += splitLine[i] + " ";
					}
 					theDictionary.put(splitLine[1], tempString);
					out.println("hi! your word means: " + theDictionary.get(splitLine[1]));
				}
 				else {
					out.println("Try a real word next time, scrub.");
				}

			}
		}
		System.out.println("client hung up");

		// Clean up shop
		out.close();
		in.close();
		sock.close();
		listen.close();
	}
}
