package chord;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import java.util.ArrayList;

/**
 * This is a subset of entire test cases
 * For your reference only.
 */
public class ChordTest {

    // private int ndecided(Chord[] pxa, int seq){
    //     int counter = 0;
    //     Object v = null;
    //     Chord.retStatus ret;
    //     for(int i = 0; i < pxa.length; i++){
    //         if(pxa[i] != null){
    //             ret = pxa[i].Status(seq);
    //             if(ret.state == State.Decided) {
    //                 assertFalse("decided values do not match: seq=" + seq + " i=" + i + " v=" + v + " v1=" + ret.v, counter > 0 && !v.equals(ret.v));
    //                 counter++;
    //                 v = ret.v;
    //             }

    //         }
    //     }
    //     return counter;
    // }

    // private void waitn(Chord[] pxa, int seq, int wanted){
    //     int to = 10;
    //     for(int i = 0; i < 30; i++){
    //         if(ndecided(pxa, seq) >= wanted){
    //             break;
    //         }
    //         try {
    //             Thread.sleep(to);
    //         } catch (Exception e){
    //             e.printStackTrace();
    //         }
    //         if(to < 1000){
    //             to = to * 2;
    //         }
    //     }

    //     int nd = ndecided(pxa, seq);
    //     assertFalse("too few decided; seq=" + seq + " ndecided=" + nd + " wanted=" + wanted, nd < wanted);

    // }

    // private void waitmajority(Chord[] pxa, int seq){
    //     waitn(pxa, seq, (pxa.length/2) + 1);
    // }

    // private void cleanup(Chord[] pxa){
    //     for(int i = 0; i < pxa.length; i++){
    //         if(pxa[i] != null){
    //             pxa[i].Kill();
    //         }
    //     }
    // }

    // private Chord[] initPaxos(int npaxos){
    //     String host = "127.0.0.1";
    //     String[] peers = new String[npaxos];
    //     int[] ports = new int[npaxos];
    //     Chord[] pxa = new Chord[npaxos];
    //     for(int i = 0 ; i < npaxos; i++){
    //         ports[i] = 1100+i;
    //         peers[i] = host;
    //     }
    //     for(int i = 0; i < npaxos; i++){
    //         pxa[i] = new Chord(i, peers, ports);
    //     }
    //     return pxa;
    // }

    @Test
    public void TestStartNewNet(){

        // Chord first = new Chord("127.0.0.1", 1100, null);
        // assertFalse("Succ: " + first.succ.id + " not same as me: " + first.me.id, first.succ.id != first.me.id);
        // assertFalse("Pred: " + first.pred.id + " not same as me: " + first.me.id, first.pred.id != first.me.id);

        // System.out.println("Second Joining!");

        // Chord second = new Chord("127.0.0.1", 1101, 1100);
        // Chord third = new Chord("127.0.0.1", 1102, 1100);
        Chord[] cs = new Chord[20];
        for (int i = 0; i < 100; i+=10) {
            cs[i/10] = new Chord("127.0.0.1", 1100 + i, i == 0 ? null : 1100);
        }
        
        
        try {
            Thread.sleep(10000);
        } catch (Exception e){

        }
        
        // Chord client = new Chord("127.0.0.1", 3300, null);

        // for (int i = 0; i < 2000; i+=10) {
        //     client.mySender.sendLookupRequest(Message.getQueryMessage(client.me, "PUT", 1100 + (i % 200), i), new NodeInfo(1190, 1110));
        // }
        
        // client.mySender.sendLookupRequest(Message.getQueryMessage(client.me, "GET", 20, null), new NodeInfo(1110, 1110));
        // client.mySender.sendLookupRequest(Message.getQueryMessage(client.me, "PUT", 1105, 1231241231), new NodeInfo(1190, 1110));
        // client.mySender.sendLookupRequest(Message.getQueryMessage(client.me, "GET", 1115, null), new NodeInfo(1100, 1100));
        // client.mySender.sendLookupRequest(Message.getQueryMessage(client.me, "GET", 1140, null), new NodeInfo(1160, 1160));
        // client.mySender.sendLookupRequest(Message.getQueryMessage(client.me, "GET", 20000, null), new NodeInfo(1110, 1110));
        try {
            Thread.sleep(15000);
        } catch (Exception e){
        }

        cs[5].kill();
        
        // for (int i = 0 ; i < 20; i++) {
        //     cs[i].distributeDatastore();
        // }
        // try {
        //     Thread.sleep(2000);
        // } catch (Exception e){
        // }

        // for (int i = 1 ; i < 19; i++) {
        //     ArrayList<Integer> fd = cs[i].fusedData;
        //     ArrayList<Integer> A = cs[i - 1].datastore;
        //     ArrayList<Integer> B = cs[i + 1].datastore;

        //     for (int j = 0; j < A.size(); j++) {
        //         assertFalse("XOR NOT EQUAL: " + fd.get(j) + " XOR " + A.get(j) + " == " + B.get(j), (fd.get(j) ^ A.get(j)) != B.get(j));
        //         assertFalse("XOR NOT EQUAL: " + fd.get(j) + " XOR " + B.get(j) + " == " + A.get(j), (fd.get(j) ^ B.get(j)) != A.get(j));
        //     }
        // }

        // client.mySender.sendLookupRequest(Message.getQueryMessage(client.me, "GET", 1105, null), new NodeInfo(1120, 1110));

        System.out.println("Passed!");
        while(true){}

    }

}
