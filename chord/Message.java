package chord;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the request message for each RMI call.
 * Hint: You may need the sequence number for each paxos instance and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 * Hint: Easier to make each variable public
 */
public class Message implements Serializable {
    static final long serialVersionUID=1L;
    // Join Args
    String msgType;
    NodeInfo sender;
    
    // Lookup Args
    NodeInfo successInfo;
    Boolean isJoin;
    NodeInfo succPred;

    //SendPred Args
    NodeInfo pred;

    //CRUD ARGS
    String opType;
    int key;
    String value;


    public static Message getJoinMessage(NodeInfo sender, boolean isJoin) {
        Message req = new Message();
        req.msgType = "QUERY";
        req.sender = sender;
        req.isJoin = isJoin;

        return req;
    }


    public static Message getQueryMessage(NodeInfo sender, boolean isJoin, String opType, int key, String value) {
        Message req = new Message();
        req.msgType = "QUERY";
        req.opType = opType;
        req.key = key;
        req.value = value;
        req.sender = sender;
        req.isJoin = isJoin;

        return req;
    }

    public static Message getJoinResultMessage(NodeInfo successInfo, NodeInfo succPred, Boolean isJoin) {
        Message req = new Message();
        req.msgType = "LOOKUP_RESULT";
        req.successInfo = successInfo;
        req.succPred = succPred;
        req.isJoin = isJoin;

        return req;
    }

    public static Message getLookupMessage(NodeInfo successInfo, NodeInfo succPred, Boolean isJoin, String value) {
        Message req = new Message();
        req.msgType = "LOOKUP_RESULT";
        req.value = value;
        req.successInfo = successInfo;
        req.succPred = succPred;
        req.isJoin = isJoin;

        return req;
    }
    
    public static Message getPingMessage(NodeInfo sender) {
        Message req = new Message();
        req.sender = sender;
        req.msgType = "PING";

        return req;
    }
    
    public static Message getPongMessage(NodeInfo pred) {
        Message req = new Message();
        req.pred = pred;
        req.msgType = "PONG";

        return req;
    }

    public static Message getNotifyMessage(NodeInfo self) {
        Message req = new Message();
        req.pred = self;
        req.msgType = "NOTIFY";

        return req;
    }

    public String toString() {
        return sender.toString() + " MSG TYPE: " + msgType;
    }
}
