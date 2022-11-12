package chord;
import java.io.Serializable;
import java.util.ArrayList;

enum QueryType {
    JOIN,
    QUERY,
    FINDSUC
}

 public class Message implements Serializable {
    static final long serialVersionUID=1L;
    // Join Args
    String msgType;
    NodeInfo sender;
    
    // Lookup Args
    NodeInfo successInfo;
    QueryType qType;
    NodeInfo succPred;

    //SendPred Args
    NodeInfo pred;

    //CRUD ARGS
    String opType;
    int key;
    Integer value;

    //FingerTable Args

    int index;

    // Redundancy Args

    ArrayList<Integer> datastore;

    // PING
    boolean isPred;


    public static Message getFindSuccMessage(NodeInfo sender, int key, int idx) {
        Message req = new Message();
        req.msgType = "QUERY";
        req.sender = sender;
        req.qType = QueryType.FINDSUC;
        req.index = idx;
        req.key = key;

        return req;
    }


    public static Message getJoinMessage(NodeInfo sender) {
        Message req = new Message();
        req.msgType = "QUERY";
        req.sender = sender;
        req.qType = QueryType.JOIN;

        return req;
    }


    public static Message getQueryMessage(NodeInfo sender, String opType, int key, Integer value) {
        Message req = new Message();
        req.msgType = "QUERY";
        req.opType = opType;
        req.key = key;
        req.value = value;
        req.sender = sender;
        req.qType = QueryType.QUERY;

        return req;
    }

    public static Message getFindSuccResultMessage(NodeInfo successInfo, int key, int idx) {
        Message req = new Message();
        req.msgType = "LOOKUP_RESULT";
        req.successInfo = successInfo;
        req.qType = QueryType.FINDSUC;
        req.key = key;
        req.index = idx;

        return req;
    }

    public static Message getJoinResultMessage(NodeInfo successInfo, NodeInfo succPred, QueryType qt) {
        Message req = new Message();
        req.msgType = "LOOKUP_RESULT";
        req.successInfo = successInfo;
        req.succPred = succPred;
        req.qType = qt;

        return req;
    }
    
    public static Message getSuccResultMessage(NodeInfo successInfo, NodeInfo succPred, QueryType qt) {
        Message req = new Message();
        req.msgType = "LOOKUP_RESULT";
        req.successInfo = successInfo;
        req.succPred = succPred;
        req.qType = qt;

        return req;
    }

    public static Message getLookupMessage(NodeInfo successInfo, NodeInfo succPred, QueryType qt, Integer value) {
        Message req = new Message();
        req.msgType = "LOOKUP_RESULT";
        req.value = value;
        req.successInfo = successInfo;
        req.succPred = succPred;
        req.qType = qt;

        return req;
    }
    
    public static Message getPingMessage(NodeInfo sender, boolean isPred) {
        Message req = new Message();
        req.sender = sender;
        req.msgType = "PING";
        req.isPred = isPred;

        return req;
    }
    
    public static Message getPongMessage(NodeInfo pred, boolean isPred) {
        Message req = new Message();
        req.pred = pred;
        req.msgType = "PONG";
        req.isPred = isPred;

        return req;
    }

    public static Message getNotifyMessage(NodeInfo self) {
        Message req = new Message();
        req.pred = self;
        req.msgType = "NOTIFY";

        return req;
    }
    
    public static Message getDistributeDataMessage(NodeInfo self, ArrayList<Integer> data) {
        Message req = new Message();
        req.sender = self;
        req.msgType = "DISTRIBUTE";
        req.datastore = data;

        return req;
    }

    public String toString() {
        return sender.toString() + " MSG TYPE: " + msgType;
    }
}
