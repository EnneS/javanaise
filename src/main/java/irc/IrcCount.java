/***
 * Irc class : simple implementation of a chat using JAVANAISE 
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.util.Random;

import jvn.JvnException;
import jvn.JvnLocalServer;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class IrcCount {

    JvnObject counter;

    /**
     * main method create a JVN object nammed IRC for representing the Chat
     * application
     **/
    public static void main(String argv[]) {
        try {
            Thread.sleep(1000);
            // initialize JVN
            JvnServerImpl js = JvnServerImpl.jvnGetServer("localhost");

            // look up the IRC object in the JVN server
            // if not found, create it, and register it in the JVN server
            JvnObject jo = js.jvnLookupObject("IRC");

            if (jo == null) {
                jo = js.jvnCreateObject(new Counter());
                // after creation, I have a write lock on the object
                jo.jvnUnLock();

                js.jvnRegisterObject("IRC", jo);
            }
            int c = 0;
            // Count to 100.
            Random r = new Random();

            while (c < 500) {
                c = write(js, jo);
                Thread.sleep(r.nextInt(80));
            }
            System.out.print("fini\n");
            while (true) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    /**
     * IRC Constructor
     *
     * @param jo the JVN object representing the Chat
     **/
    public IrcCount(JvnObject jo) {
        counter = jo;
    }

    public static int write(JvnLocalServer js, JvnObject jo) {
        int res = 0;
        try {
            // lock the object in read mode
            jo.jvnLockWrite();
            Random r = new Random();
            // invoke the method
            ((Counter) jo.jvnGetSharedObject()).plus();
            res = ((Counter) jo.jvnGetSharedObject()).getCounter();
            System.out.println("[" + js.hashCode() + "]" + "Writing " + ((Counter) jo.jvnGetSharedObject()).counter);
            // Sleep between 0 and 1000 ms
            Thread.sleep(r.nextInt(80));
            // unlock the object
            jo.jvnUnLock();

        } catch (JvnException je) {
            System.out.println("IRC problem : " + je.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

}