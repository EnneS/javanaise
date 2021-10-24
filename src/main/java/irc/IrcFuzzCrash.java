/***
 * Irc class : simple implementation of a chat using JAVANAISE 
 * Contact: 
 *
 * Authors: 
 */

package irc;

import jvn.JvnException;
import jvn.JvnLocalServer;
import jvn.JvnObject;
import jvn.JvnServerImpl;

import java.util.Random;

public class IrcFuzzCrash {

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
            while (c < 1000) {
                int next = r.nextInt(100);
                if (next < 49) {
                    c = write(js, jo);
                } else if(next < 99) {
                    read(jo, js);
                } else {
                    writeCrash(js, jo);
                }

                // Sleep between 0 and 100 ms
                Thread.sleep(r.nextInt(100));
            }
            System.out.print("fini\n");
            while (true) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error IrcCrash");
            System.out.println(e.getMessage());
        }
    }

    /**
     * IRC Constructor
     *
     * @param jo the JVN object representing the Chat
     **/
    public IrcFuzzCrash(JvnObject jo) {
        counter = jo;
    }

    /**
     * 
     * @param jo
     * @param js
     * @return -1 if error, n>0 otherwise
     */
    public static int read(JvnObject jo, JvnServerImpl js) {
        int res = -1;
        try {
            // lock the object in read mode
            jo.jvnLockRead();
            Random r = new Random();
            // invoke the method
            res = ((Counter) (jo.jvnGetSharedObject())).getCounter();
            System.out.println("[" + js.hashCode() + "]" + "Reading " + res);
            // Sleep between 0 and 100 ms
            Thread.sleep(r.nextInt(100));
            // unlock the object
            jo.jvnUnLock();
        } catch (JvnException je) {
            System.out.println("IRC problem : " + je.getMessage());
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        return res;
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
            System.out.println("[" + js.hashCode() + "]" + "Writing " + res);
            // Sleep between 0 and 100 ms
            Thread.sleep(r.nextInt(100));
            // unlock the object
            jo.jvnUnLock();

        } catch (JvnException je) {
            System.out.println("IRC problem : " + je.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static int writeCrash(JvnLocalServer js, JvnObject jo) {
        int res = 0;
        try {

            jo.jvnLockWrite();

            System.out.println("[" + js.hashCode() + "]" + "Will crash");
            int crash = 12/0;

            jo.jvnUnLock();

        } catch (JvnException je) {
            System.out.println("IRC problem : " + je.getMessage());
        }
        return res;
    }

}