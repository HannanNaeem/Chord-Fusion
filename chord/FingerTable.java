package chord;

public class FingerTable implements Runnable {
	Chord parent;

	FingerTable(Chord p) {
		parent = p;
	}

	public int getIthStart(int id, int i) {
		int x = (id + (int) Math.pow(2, i)) % 1290;
		return x < 1100 ? 1100+x : x;
	}

	@Override
	public void run() {
		int i = 1;
		while (true) {
			try {
				Thread.sleep(4000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (parent.fingerTable[0] != null) {
				parent.mySender.sendFindSuccMessage(
						Message.getFindSuccMessage(parent.me, getIthStart(parent.me.id, i), i),
						parent.fingerTable[0]);

				i = (i + 1) % 8;
				if (i == 0) i = 1;
			}
		}
	}
}
