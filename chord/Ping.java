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
				if (parent.fingerTable[0] != null && parent.fingerTable[0].id != parent.me.id) {
					if (parent.pongCount < 0 ) {
						System.out.println("SUCC DEAD!");
					}
					// System.out.println("====== " + parent.pred.id + " -> " + parent.me.id + " -> " + parent.fingerTable[0].id + " =======");
					// System.out.println("====== " + parent.me + " FTABLE: " + Arrays.toString(parent.fingerTable) + " =======");
					parent.mySender.ping(Message.getPingMessage(parent.me), parent.fingerTable[0]);
				}

				Thread.sleep(1000);
			} catch (Exception e) {
				System.out.println("Ping Failed");
			}
		}
	}

}
