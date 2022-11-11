package chord;

import java.net.Socket;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Sender {
  	public void sendLookupRequest(Message req, NodeInfo recvr) {
		sendOnSocket("127.0.0.1", recvr.port, req);
	}
  	
	public void sendCrudMessage(Message req, NodeInfo recvr) {
		sendOnSocket("127.0.0.1", recvr.port, req);
	}
	
	public void retLookupRes(Message req, NodeInfo recvr) {
		sendOnSocket("127.0.0.1", recvr.port, req);
	}
	
	public void ping(Message req, NodeInfo recvr) {
		sendOnSocket("127.0.0.1", recvr.port, req);
	}
	
	public void sendPred(Message req, NodeInfo recvr) {
		sendOnSocket("127.0.0.1", recvr.port, req);
	}

	public void sendNotify(Message req, NodeInfo recvr) {
		sendOnSocket("127.0.0.1", recvr.port, req);
	}

	public void sendOnSocket(String ip, int port, Message msg) {
		try {
			Socket recv = new Socket(ip, port);
			ObjectOutputStream out = new ObjectOutputStream(recv.getOutputStream());

			out.writeObject(msg);
			out.close();
			recv.close();
		} catch (IOException e) {
		  System.out.println("Error Sending on port: " + port);
		  System.exit(1);
		}
	}
  
}
