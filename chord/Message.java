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
    NodeInfo firstContact;
    Boolean firstReq;
    
    // Lookup Args
    NodeInfo successInfo;
    Boolean isQuery;
    NodeInfo succPred;

    //SendPred Args
    NodeInfo pred;

    public static Message getJoinrequest(NodeInfo sender, NodeInfo firstContact, boolean firstReq) {
        Message req = new Message();
        req.msgType = "JOIN";
        req.sender = sender;
        req.firstContact = firstContact;
        req.firstReq = firstReq;

        return req;
    }

    public static Message getLookupMessage(NodeInfo successInfo, NodeInfo succPred, Boolean isQuery) {
        Message req = new Message();
        req.msgType = "LOOKUP_RESULT";
        req.successInfo = successInfo;
        req.isQuery = isQuery;
        req.succPred = succPred;

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
