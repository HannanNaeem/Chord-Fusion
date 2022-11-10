package chord;

import java.net.Socket;
import java.io.IOException;
import java.net.ServerSocket;
import java.io.ObjectInputStream;

public class SocketListener implements Runnable {

  	String ip;
  	int port;
		Chord parent;
		ServerSocket server;

  	SocketListener(String ip, int port, Chord parent) {
    	this.ip = ip;
			this.port = port;
			this.parent = parent;

		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Error serving on port: " + port);
			System.exit(1);
		}
	}

	public void serve(){
		try {
			while(true) {
				Socket clientSocket = server.accept();
				ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
				
				Message msg = Message.class.cast(input.readObject());
				
				if (msg.msgType.equals("JOIN")) {
					parent.Join(msg);

				} else if (msg.msgType.equals("LOOKUP_RESULT")) {
					
					if (!msg.isQuery) parent.setSucc(msg);
					else {
						// QUERY RESULT
					}
				} else if (msg.msgType.equals("PING")) {
					parent.mySender.sendPred(Message.getPongMessage(parent.pred), msg.sender);
				} else if (msg.msgType.equals("PONG")) {
					// parent.mySender.sendPred(Message.getPongMessage(parent.pred), msg.sender);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

  	@Override
  	public void run() {
		try {
			this.serve();
		} catch (Exception e) {
			System.out.println("Cant serve");
		}
  	}
  
}
