/***
 * Irc class : simple implementation of a chat using JAVANAISE 
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.io.Serializable;
import java.util.Random;
import java.util.Scanner;

import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class IrcNw {

    JvnObject sentence;

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
                jo = js.jvnCreateObject((Serializable) new Sentence());
                // after creation, I have a write lock on the object
                jo.jvnUnLock();

                js.jvnRegisterObject("IRC", jo);
            }
            int c = 0;
            Random r = new Random();
            // Count to 20.
            while (c < 1000) {
                // Read or write at random
                if (r.nextInt(2) == 1) {
                    write(jo, "from " + js.hashCode() + " : " + c + "\n");
                    c++;
                } else {
                    int res = read(jo, js);
                    if (res > 0) {
                        c = res;
                    }
                }
                // Sleep between 0 and 100 ms
                Thread.sleep(r.nextInt(100));
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
    public IrcNw(JvnObject jo) {
        sentence = jo;
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
            String s = ((Sentence) (jo.jvnGetSharedObject())).read();
            // Sleep between 0 and 100 ms
            Thread.sleep(r.nextInt(1000));
            // unlock the object
            jo.jvnUnLock();

            // display the read value
            System.out.print((js.hashCode() + " read " + s + "\n"));
            Scanner sc = new Scanner(s);

            try {
                sc.skip("from \\d+ : ");
                res = sc.nextInt();
            } catch (Exception e) {
                res = -1;
            }
            sc.close();
        } catch (JvnException je) {
            System.out.println("IRC problem : " + je.getMessage());
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        return res;
    }

    public static void write(JvnObject jo, String s) {
        try {
            // lock the object in read mode
            jo.jvnLockWrite();
            Random r = new Random();
            System.out.print("Write " + s + "\n");
            // invoke the method
            ((Sentence) (jo.jvnGetSharedObject())).write(s);
            // Sleep between 0 and 1000 ms
            Thread.sleep(r.nextInt(1000));
            // unlock the object
            jo.jvnUnLock();

        } catch (JvnException je) {
            System.out.println("IRC problem : " + je.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}