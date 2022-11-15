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
				if (!parent.killed && parent.fingerTable[0] != null && parent.fingerTable[0].id != parent.me.id) {
					if (parent.pongCount < 0 ) {
						System.out.println("SUCC DEAD!");
						System.out.println("START RECOVERY");
						int succId = parent.fingerTable[0].id;
						
						// System.out.println("ME: " + parent.me);
						// System.out.println("OLD PARENT: " + succId);
						
						for (int i = 0; i < parent.fingerTable.length; i++) { 
							if (parent.fingerTable[i] != null && parent.fingerTable[i].id != succId ) {
								parent.fingerTable[0] = parent.fingerTable[i];
								break;
							}
						}
						// System.out.println("NEW PARENT: " + parent.fingerTable[0]);
					}
					
					parent.mutex.lock();
					if (parent.predPongCount < 0) {
						// System.out.println("PRED DEAD!: " + parent.me);
						parent.pred = null;
					} 
					parent.mutex.unlock();
					// System.out.println("====== " + (parent.pred != null ? parent.pred.id : null) + " -> " + parent.me.id + " -> " + parent.fingerTable[0].id + " =======");
					// System.out.println("====== " + parent.me + " FTABLE: " + Arrays.toString(parent.fingerTable) + " =======");
					parent.pongCount--;
					parent.mySender.ping(Message.getPingMessage(parent.me, false), parent.fingerTable[0]);
					if (parent.pred != null) {
						parent.predPongCount--;
						parent.mySender.ping(Message.getPingMessage(parent.me, true), parent.pred);
					}
				}

				Thread.sleep(1000);
			} catch (Exception e) {
				System.out.println("Ping Failed");
			}
		}
	}

}
