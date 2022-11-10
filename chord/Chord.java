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
    BigInteger id;

    NodeInfo(int port, BigInteger id) {
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

            this.me = new NodeInfo(port, new BigInteger(1, messageDigest));

        } catch (NoSuchAlgorithmException e) {
            System.out.println("No Algo Exception");
        }

        this.mutex = new ReentrantLock();

        if (firstContactPort == null) {
            this.pred = new NodeInfo(port, this.me.id);
            this.succ = new NodeInfo(port, this.me.id);
        } else {
            NodeInfo fc = new NodeInfo(firstContactPort, null);
            mySender.sendJoinRequest(Message.getJoinrequest(this.me, fc, true), fc);
        }

        new Thread(myLsnr).start();
        new Thread(myPinger).start();
    }

    @Override
    public void run(){
    }


    public void isSuccCorrect(NodeInfo sucPredInfo) {
        if (sucPredInfo.id.compareTo(this.me.id) != 0) {
            System.out.println("I AM NOT THE RIGHT PRED");

            this.succ = sucPredInfo;
            mySender.sendNotify(Message.getNotifyMessage(this.me), sucPredInfo);
        }
    }

    public NodeInfo amISucc(NodeInfo query) {
        if (me.id.compareTo(succ.id) == 0 && me.id.compareTo(pred.id) == 0) {
            return this.me;
        }

        if ((query.id.compareTo(this.me.id) == 0 || query.id.compareTo(this.me.id) == -1) && query.id.compareTo(this.pred.id) == 1) {
            return this.me;
        }
        return null; 
    }


    public void Join(Message req) {
        if (req.firstReq == true) {
            req.firstReq = false;
            // req.firstContact.id = this.me.id;
        }

        NodeInfo succ = amISucc(req.sender);

        if (succ == null) {
            this.mySender.sendJoinRequest(req, this.succ);
        }

        // send successor
        this.mySender.retLookupRes(Message.getLookupMessage(succ, this.pred, false), req.sender);
    }

    public void setSucc(Message req) {
        System.out.println("SETTING SUCCESSOR: " + this.me + " to " + req.successInfo);
        this.succ = req.successInfo;
        this.pred = req.succPred;
    }


    public void handleNotify(NodeInfo newPred) {
        this.pred = newPred;

        // SPECIAL CASE WHEN N = 2 
        if (this.me.id.compareTo(this.succ.id) == 0) {
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
