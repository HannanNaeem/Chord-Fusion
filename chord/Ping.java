package chord;

public class Ping implements Runnable {

	Chord parent;

	Ping(Chord parent) {
		this.parent = parent;
	}

	@Override
	public void run() {
		while(true) {
			try {
				if (parent.succ != null && parent.succ.id.compareTo(parent.me.id) != 0) {
					parent.mySender.ping(Message.getPingMessage(parent.me), parent.succ);
				}

				Thread.sleep(5000);
			} catch (Exception e) {
				System.out.println("Ping Failed");
			}
		}
	}

}
