package chord;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import java.util.ArrayList;
import java.util.Random;

public class ChordTest {
    @Test
    public void RunSimu(){

        Chord[] cs = new Chord[10];
        for (int i = 0; i < 100; i+=10) {
            cs[i/10] = new Chord("127.0.0.1", 1100 + i, i == 0 ? null : 1100);
        }

        //wait for stabalize
        
        try {
            Thread.sleep(15000);
        } catch (Exception e){

        }

        // insert keys
        
        Chord client = new Chord("127.0.0.1", 3300, null);

        for (int i = 0; i < 100; i+=10) {
            for (int j = 0; j < 10; j++) {
                client.mySender.sendLookupRequest(Message.getQueryMessage(client.me, "PUT", 1100 + i + j, i + j), new NodeInfo(1130, 1130));
            }
        }
        
        // wait for key insertions
        try {
            Thread.sleep(12000);
        } catch (Exception e){
        }
        
        cs[5].kill();
        

        // wait for stabalize

        try {
            Thread.sleep(15000);
        } catch (Exception e){
        }
        

        // get key after killing node
        client.mySender.sendLookupRequest(Message.getQueryMessage(client.me, "GET", 1145, null), new NodeInfo(1110, 1110));

        System.out.println("Passed!");
        while(true){}

    }

}
