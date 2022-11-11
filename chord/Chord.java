package chord;

import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.io.Serializable;
import java.util.HashMap;


class NodeInfo implements Serializable {
    static final long serialVersionUID=1L;

    int port;
    int id;

    NodeInfo(int port, int id) {
        this.port = port;
        this.id = id;
    }

    public String toString() {
        return "ID: " + this.id + ", Port: " + this.port;
    }
}

public class Chord implements Runnable{

    ReentrantLock mutex;
    String ip;
    
    NodeInfo pred;
    NodeInfo succ;
    
    NodeInfo me; // my SHA-1
    Sender mySender;
    Ping myPinger;
    SocketListener myLsnr;
    
    Registry registry;

    AtomicBoolean dead;
    AtomicBoolean unreliable;

    HashMap<Integer, String> datastore;


    public Chord(String ip, int port, Integer firstContactPort){

        this.ip = ip;
        this.pred = null;
        this.succ = null;
        mySender = new Sender();
        myPinger = new Ping(this);
        myLsnr = new SocketListener(ip, port, this);
        datastore = new HashMap<Integer, String>();

        // Set Me
        try {

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(Integer.toString(port).getBytes());

            this.me = new NodeInfo(
                port,
                port);
                // (new BigInteger(1, messageDigest).mod(new BigInteger(Integer.toString((int) Math.pow(2, 30))))).intValue());

        } catch (NoSuchAlgorithmException e) {
            System.out.println("No Algo Exception");
        }

        this.mutex = new ReentrantLock();

        if (firstContactPort == null) {
            this.pred = new NodeInfo(port, this.me.id);
            this.succ = new NodeInfo(port, this.me.id);
        } else {
            NodeInfo fc = new NodeInfo(firstContactPort, -1);
            mySender.sendLookupRequest(Message.getJoinMessage(this.me, true), fc);
        }

        new Thread(myLsnr).start();
        new Thread(myPinger).start();
    }

    @Override
    public void run(){
    }


    public void isSuccCorrect(NodeInfo sucPredInfo) {

        if (getRelativeVal(sucPredInfo.id, me.id) > 0 && getRelativeVal(succ.id, me.id) > getRelativeVal(sucPredInfo.id, me.id)) {
            System.out.println("I AM NOT THE RIGHT PRED");
            this.succ = sucPredInfo;
        }
        mySender.sendNotify(Message.getNotifyMessage(this.me), this.succ);
    }

    public void handlePong(NodeInfo sucPredInfo) {
        isSuccCorrect(sucPredInfo);
    }

    public NodeInfo isMySuccSucc(NodeInfo query) {
        if (me.id == succ.id && me.id == pred.id) {
            return this.me;
        }

        if (getRelativeVal(query.id, me.id) >= 0 && getRelativeVal(succ.id, me.id) > getRelativeVal(query.id, me.id)) {
            return this.succ;
        }

        return null; 
    }


    public void Lookup(Message req) {
        NodeInfo succ = isMySuccSucc(req.isJoin ? req.sender : new NodeInfo(-1, req.key));

        if (succ == null) {
            this.mySender.sendLookupRequest(req, this.succ);
            return;
        }

        // send successor
        if (!req.isJoin) {
            req.msgType = "CRUD";
            this.mySender.sendCrudMessage(req, succ);
        } else {
            this.mySender.retLookupRes(Message.getJoinResultMessage(succ, this.me, req.isJoin), req.sender);
        }
    }

    public void handleCrudOp(Message msg) {
        if (msg.opType.equals("GET")) {
            String result = datastore.get(msg.key);
            System.out.println("----------------------------- ME: " + this.me.id + " SENDING RESULT: " + msg.key + " --------------------------");
            this.mySender.retLookupRes(Message.getLookupMessage(succ, this.me, msg.isJoin, result), msg.sender);
        } else {
            datastore.put(msg.key, msg.value);
        }
    }

    public void handleQueryResult(Message msg) {
        System.out.println("----------------------------- ME: " + this.me.id + " GOT RESULT: " + msg.value + " --------------------------");
    }

    public void setSucc(Message req) {
        System.out.println("SETTING SUCCESSOR: " + this.me + " to " + req.successInfo);
        this.succ = req.successInfo;
        this.pred = req.succPred;
    }


    public void handleNotify(NodeInfo newPred) {
        if ((getRelativeVal(this.me.id, newPred.id) > 0 && getRelativeVal(this.me.id, this.pred.id) > getRelativeVal(this.me.id, newPred.id)) || getRelativeVal(succ.id, pred.id) == 0)
            this.pred = newPred;

        // SPECIAL CASE WHEN N = 2 
        if (this.me.id == this.succ.id) {
            this.succ = newPred;
        }
    }

    public int getRelativeVal(int x, int y) {
        int val = x - y;
        if (val < 0) val += (int) Math.pow(2, 30);

        return val;
    }
}
