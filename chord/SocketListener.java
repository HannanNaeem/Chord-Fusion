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

				if (parent.killed) {
					clientSocket.close();
					continue;
				}

				ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
				
				Message msg = Message.class.cast(input.readObject());

				if (msg == null) continue;
				
				if (msg.msgType.equals("QUERY")) {
					parent.Lookup(msg);

				} else if (msg.msgType.equals("LOOKUP_RESULT")) {
					
					if (msg.qType == QueryType.JOIN) {
						parent.setSucc(msg);
					}
					else if (msg.qType == QueryType.QUERY) {
						parent.handleQueryResult(msg);
					
					} else if (msg.qType == QueryType.FINDSUC) {
						parent.updateFingerTable(msg);
					}
				} else if (msg.msgType.equals("PING")) {
					// System.out.println("PING: FROM " + msg.sender.port + " TO " + parent.me.port);
					parent.mySender.sendPred(Message.getPongMessage(parent.pred, msg.isPred), msg.sender);
				} else if (msg.msgType.equals("PONG")) {
					if (!msg.isPred)
						parent.handlePong(msg.pred);
					else
						parent.handlePredPong();
				} else if (msg.msgType.equals("NOTIFY")) {
					parent.handleNotify(msg.pred);
				} else if (msg.msgType.equals("CRUD")) {
						parent.handleCrudOp(msg);
				} else if (msg.msgType.equals("DISTRIBUTE")) {
					parent.handleDistribute(msg);
				}

				input.close();
				clientSocket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
