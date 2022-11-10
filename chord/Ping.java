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
				if (parent.succ != null && parent.succ.id != parent.me.id) {
					System.out.println("======" + parent.me +"=======");
					System.out.println("PRED: " + parent.pred);
					System.out.println("SUCC: " + parent.succ);
					System.out.println("=============");
					parent.mySender.ping(Message.getPingMessage(parent.me), parent.succ);
				}

				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Ping Failed");
			}
		}
	}

}
