package chord;

import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;

class NodeInfo implements Serializable {
    static final long serialVersionUID=1L;

    int port;
    int id;

    NodeInfo(int port, int id) {
        this.port = port;
        this.id = id;
    }

    public String toString() {
        return "" + this.id;
    }
}

public class Chord implements Runnable{

    public static final int MAX_KEY_SPACE = 30;

    ReentrantLock mutex;
    String ip;
    
    NodeInfo pred;

    NodeInfo me; // my SHA-1
    Sender mySender;
    Ping myPinger;
    SocketListener myLsnr;
    FingerTable fingerTableUpdater;
    
    Registry registry;

    AtomicBoolean dead;
    AtomicBoolean unreliable;

    HashMap<Integer, String> datastore;
    NodeInfo[] fingerTable;


    public Chord(String ip, int port, Integer firstContactPort){

        this.ip = ip;
        this.pred = null;

        mySender = new Sender();
        myPinger = new Ping(this);
        myLsnr = new SocketListener(ip, port, this);
        fingerTableUpdater = new FingerTable(this);
        datastore = new HashMap<Integer, String>();
        fingerTable = new NodeInfo[8];

        for (int i = 0; i < 8; i++) {
            fingerTable[i] = null;
        }

        // Set Me
        try {

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(Integer.toString(port).getBytes());

            this.me = new NodeInfo(
                port,
                port);
                // (new BigInteger(1, messageDigest).mod(new BigInteger(Integer.toString((int) Math.pow(2, MAX_KEY_SPACE))))).intValue());

        } catch (NoSuchAlgorithmException e) {
            System.out.println("No Algo Exception");
        }

        this.mutex = new ReentrantLock();

        if (firstContactPort == null) {
            this.pred = new NodeInfo(port, this.me.id);
            fingerTable[0] = new NodeInfo(port, this.me.id);
        } else {
            NodeInfo fc = new NodeInfo(firstContactPort, -1);
            mySender.sendLookupRequest(Message.getJoinMessage(this.me), fc);
        }

        new Thread(myLsnr).start();
        new Thread(myPinger).start();
        new Thread(fingerTableUpdater).start();
    }

    @Override
    public void run(){
    }


    public void isSuccCorrect(NodeInfo sucPredInfo) {

        if (getRelativeVal(sucPredInfo.id, me.id) > 0 && getRelativeVal(fingerTable[0].id, me.id) > getRelativeVal(sucPredInfo.id, me.id)) {
            System.out.println("I AM NOT THE RIGHT PRED");

            fingerTable[0] = sucPredInfo;
        }
        mySender.sendNotify(Message.getNotifyMessage(this.me), fingerTable[0]);
    }

    public void handlePong(NodeInfo sucPredInfo) {
        isSuccCorrect(sucPredInfo);
    }

    // public NodeInfo getClosestFinger(NodeInfo query) {
    // }

    public NodeInfo isMySuccSucc(NodeInfo query) {
        if (me.id == fingerTable[0].id && me.id == pred.id) {
            return this.me;
        }

        if (getRelativeVal(query.id, me.id) >= 0 && getRelativeVal(fingerTable[0].id, me.id) > getRelativeVal(query.id, me.id)) {
            return fingerTable[0];
        }

        return null; 
    }


    public void Lookup(Message req) {
        NodeInfo succ = isMySuccSucc(req.qType == QueryType.JOIN ? req.sender : new NodeInfo(-1, req.key));

        if (succ == null) {
            this.mySender.sendLookupRequest(req, fingerTable[0]);
            return;
        }

        // send successor
        if (req.qType == QueryType.QUERY) {
            req.msgType = "CRUD";
            this.mySender.sendCrudMessage(req, succ);
        } else if (req.qType == QueryType.JOIN) {
            this.mySender.retLookupRes(Message.getJoinResultMessage(succ, this.me, req.qType), req.sender);
        } else if (req.qType == QueryType.FINDSUC) {
            System.out.println("LOOKUP KEY: " + req.key + " IDX: " + req.index + " RESULT: " + succ);
            this.mySender.retLookupRes(Message.getFindSuccResultMessage(succ, req.key, req.index), req.sender);

        }
    }

    public void handleCrudOp(Message msg) {
        if (msg.opType.equals("GET")) {
            String result = datastore.get(msg.key);
            System.out.println("----------------------------- ME: " + this.me.id + " SENDING RESULT: " + msg.key + " --------------------------");
            this.mySender.retLookupRes(Message.getLookupMessage(fingerTable[0], this.me, msg.qType, result), msg.sender);
        } else {
            datastore.put(msg.key, msg.value);
        }
    }

    public void handleQueryResult(Message msg) {
        System.out.println("----------------------------- ME: " + this.me.id + " GOT RESULT: " + msg.value + " --------------------------");
    }

    public void setSucc(Message req) {
        System.out.println("SETTING SUCCESSOR: " + this.me + " to " + req.successInfo);
        fingerTable[0] = req.successInfo;
        this.pred = req.succPred;
    }


    public void handleNotify(NodeInfo newPred) {
        if ((getRelativeVal(this.me.id, newPred.id) > 0 && getRelativeVal(this.me.id, this.pred.id) > getRelativeVal(this.me.id, newPred.id)) || getRelativeVal(fingerTable[0].id, pred.id) == 0)
            this.pred = newPred;

        // SPECIAL CASE WHEN N = 2
        if (this.me.id == fingerTable[0].id) {
            fingerTable[0] = newPred;
        }
    }

    public int getRelativeVal(int x, int y) {
        int val = x - y;
        if (val < 0) val += (int) Math.pow(2, Chord.MAX_KEY_SPACE);

        return val;
    }

    public void updateFingerTable(Message msg) {
        fingerTable[msg.index] = msg.successInfo;
    }
}
