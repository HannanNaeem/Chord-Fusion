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
    boolean killed;
    
    NodeInfo pred;
    int pongCount;
    int predPongCount;

    NodeInfo me; // my SHA-1
    Sender mySender;
    Ping myPinger;
    SocketListener myLsnr;
    FingerTable fingerTableUpdater;
    
    Registry registry;

    AtomicBoolean dead;
    AtomicBoolean unreliable;

    HashMap<Integer, Integer> datastoreMap;
    ArrayList<Integer> datastore;

    ArrayList<Integer> fusedData;
    HashMap<Integer, Integer> fusedIndex;

    NodeInfo[] fingerTable;


    public Chord(String ip, int port, Integer firstContactPort){

        this.ip = ip;
        this.pred = null;

        mySender = new Sender(this);
        myPinger = new Ping(this);
        pongCount = 5;
        predPongCount = 5;
        myLsnr = new SocketListener(ip, port, this);
        killed = false;
        
        fingerTable = new NodeInfo[8];
        fingerTableUpdater = new FingerTable(this);

        fusedData = new ArrayList<Integer>();
        fusedIndex = new HashMap<Integer, Integer>();

        datastoreMap = new HashMap<Integer, Integer>();
        datastore = new ArrayList<Integer>();


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

        if (sucPredInfo != null && (getRelativeVal(sucPredInfo.id, me.id) > 0 && getRelativeVal(fingerTable[0].id, me.id) > getRelativeVal(sucPredInfo.id, me.id))) {
            System.out.println("I AM NOT THE RIGHT PRED");

            fingerTable[0] = sucPredInfo;
        }
        mySender.sendNotify(Message.getNotifyMessage(this.me), fingerTable[0]);
    }

    public void handlePong(NodeInfo sucPredInfo) {
        pongCount = 5;
        isSuccCorrect(sucPredInfo);
    }

    public void handlePredPong() {
        predPongCount = 5;
    }

    public NodeInfo getClosestFinger(NodeInfo query) {
        for (int i = this.fingerTable.length - 1; i >= 0; i--) {
            if (fingerTable[i] == null) continue;


            if (getRelativeVal(me.id, fingerTable[i].id) > 0 && (getRelativeVal(query.id, me.id) > getRelativeVal(fingerTable[i].id, me.id))) {
                return fingerTable[i];
            }
        }

        return fingerTable[0];
    }

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

        NodeInfo queryNode = req.qType == QueryType.JOIN ? req.sender : new NodeInfo(-1, req.key);
        NodeInfo succ = isMySuccSucc(queryNode);

        if (succ == null) {
            this.mySender.sendLookupRequest(req, this.getClosestFinger(queryNode));
            return;
        }

        // send successor
        if (req.qType == QueryType.QUERY) {
            req.msgType = "CRUD";
            this.mySender.sendCrudMessage(req, succ);
        } else if (req.qType == QueryType.JOIN) {
            this.mySender.retLookupRes(Message.getJoinResultMessage(succ, this.me, req.qType), req.sender);
        } else if (req.qType == QueryType.FINDSUC) {
            this.mySender.retLookupRes(Message.getFindSuccResultMessage(succ, req.key, req.index), req.sender);

        }
    }

    public void handleCrudOp(Message msg) {
        if (msg.opType.equals("GET")) {
            Integer result = null;
            Integer idx = datastoreMap.get(msg.key);
            System.out.println("idx: " + idx);
            if (idx != null) {
                result = datastore.get(idx);
            }
            System.out.println("----------------------------- ME: " + this.me.id + " SENDING RESULT: " + msg.key + ": " + result + " --------------------------");
            this.mySender.retLookupRes(Message.getLookupMessage(fingerTable[0], this.me, msg.qType, result), msg.sender);
        } else {
            System.out.println("----------------------------- ME: " + this.me.id + " PUTTING RESULT: " + msg.key + " VALUE: " + msg.value + " --------------------------");
            datastore.add(msg.value);
            datastoreMap.put(msg.key, datastore.size() - 1);
        }
    }

    public void handleQueryResult(Message msg) {
        System.out.println("----------------------------- ME: " + this.me.id + " GOT RESULT: " + msg.value + " --------------------------");
    }

    public void distributeDatastore() {
        this.mySender.sendDistributeDataMessage(Message.getDistributeDataMessage(this.me, datastore), fingerTable[0]);
        this.mySender.sendDistributeDataMessage(Message.getDistributeDataMessage(this.me, datastore), this.pred);
    }

    public void handleDistribute(Message msg) {
        System.out.println("HANDLE DISTIRBUTE BY: " + msg.sender + " TO: " + this.me);
        mutex.lock();
        NodeInfo sender = msg.sender;

        if (sender.id != fingerTable[0].id && sender.id != this.pred.id) {
            mutex.unlock();
            return;
        }
        
        if (fusedIndex.keySet().size() == 2) {
            fusedData = new ArrayList<Integer>();
            fusedIndex = new HashMap<Integer, Integer>();
        }
        
        if (fusedData.size() == 0) {
            // Simply copy this array
            for (Integer d: msg.datastore) {
                fusedData.add(d);
            }
            fusedIndex.put(msg.sender.id, msg.datastore.size() - 1);
        } else {
            for (int i = 0; i < msg.datastore.size(); i++) {
                if (i < fusedData.size()){
                    int newVal = fusedData.get(i) ^ msg.datastore.get(i);
                    fusedData.set(i , newVal);
                } else {
                    fusedData.add(msg.datastore.get(i));
                }
            }
            fusedIndex.put(msg.sender.id, msg.datastore.size() - 1);
        }
        
        mutex.unlock();
    }

    public void setSucc(Message req) {
        System.out.println("SETTING SUCCESSOR: " + this.me + " to " + req.successInfo);
        fingerTable[0] = req.successInfo;
        this.pred = req.succPred;
    }


    public void handleNotify(NodeInfo newPred) {
        mutex.lock();
        if (this.pred == null || ((getRelativeVal(this.me.id, newPred.id) > 0 && getRelativeVal(this.me.id, this.pred.id) > getRelativeVal(this.me.id, newPred.id)) || getRelativeVal(fingerTable[0].id, pred.id) == 0))
        this.pred = newPred;
        mutex.unlock();

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

    public void kill() {
        this.killed = true;
    }
}
