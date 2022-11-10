package chord;

import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.io.Serializable;


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


    public Chord(String ip, int port, Integer firstContactPort){

        this.ip = ip;
        this.pred = null;
        this.succ = null;
        mySender = new Sender();
        myPinger = new Ping(this);
        myLsnr = new SocketListener(ip, port, this);

        // Set Me
        try {

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(Integer.toString(port).getBytes());

            this.me = new NodeInfo(
                port,
                (new BigInteger(1, messageDigest).mod(new BigInteger(Integer.toString((int) Math.pow(2, 30))))).intValue());

        } catch (NoSuchAlgorithmException e) {
            System.out.println("No Algo Exception");
        }

        this.mutex = new ReentrantLock();

        if (firstContactPort == null) {
            this.pred = new NodeInfo(port, this.me.id);
            this.succ = new NodeInfo(port, this.me.id);
        } else {
            NodeInfo fc = new NodeInfo(firstContactPort, -1);
            mySender.sendJoinRequest(Message.getJoinrequest(this.me, fc, true), fc);
        }

        new Thread(myLsnr).start();
        new Thread(myPinger).start();
    }

    @Override
    public void run(){
    }


    public void isSuccCorrect(NodeInfo sucPredInfo) {

        if (sucPredInfo.id > this.me.id && sucPredInfo.id < this.succ.id) {
            System.out.println("I AM NOT THE RIGHT PRED");
            this.succ = sucPredInfo;
        }
        mySender.sendNotify(Message.getNotifyMessage(this.me), this.succ);
    }

    public void handlePong(NodeInfo sucPredInfo) {
        isSuccCorrect(sucPredInfo);
    }

    public NodeInfo findSuccessor(NodeInfo query) {
        if (me.id == succ.id && me.id == pred.id) {
            return this.me;
        }

        if (query.id == this.succ.id || (query.id < this.succ.id && query.id > this.me.id)) {
            return this.succ;
        }

        return null; 
    }


    public void Join(Message req) {
        if (req.firstReq == true) {
            req.firstReq = false;
            // req.firstContact.id = this.me.id;
        }

        NodeInfo succ = findSuccessor(req.sender);

        if (succ == null) {
            this.mySender.sendJoinRequest(req, this.succ);
            return;
        }

        // send successor
        this.mySender.retLookupRes(Message.getLookupMessage(succ, this.me, false), req.sender);
    }

    public void setSucc(Message req) {
        System.out.println("SETTING SUCCESSOR: " + this.me + " to " + req.successInfo);
        this.succ = req.successInfo;
        this.pred = req.succPred;
    }


    public void handleNotify(NodeInfo newPred) {
        System.out.println("NOTIFY");
        this.pred = newPred;

        // SPECIAL CASE WHEN N = 2 
        if (this.me.id == this.succ.id) {
            this.succ = newPred;
        }
    }

    public Response Ping(Message req){
        return new Response();
    }
    
    public Response Stabilize(Message req){
        return new Response();
    }
    
    public Response Notify(Message req){
        return new Response();
    }
}
