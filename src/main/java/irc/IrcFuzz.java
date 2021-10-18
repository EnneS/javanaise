/***
 * Irc class : simple implementation of a chat using JAVANAISE 
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.util.Random;
import java.util.Scanner;

import jvn.*;

public class IrcFuzz {

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

            CounterItf counter = (CounterItf) JvnObjectProxy.newInstance("IRC", Counter.class);

            int c = 0;
            // Count to 100.
            Random r = new Random();
            while (c < 1000) {
                if (r.nextInt(2) == 1) {
                    c = write(js, counter);
                } else {
                    read(js, counter);
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
    public IrcFuzz(JvnObject jo) {
        counter = jo;
    }

    /**
     * 
     * @param jo
     * @param js
     * @return -1 if error, n>0 otherwise
     */
    public static int read(JvnServerImpl js, CounterItf jo) {
        int res = -1;
        try {
            Random r = new Random();
            // invoke the method
            res = jo.getCounter();
            System.out.println("[" + js.hashCode() + "]" + "Reading " + res);
            // Sleep between 0 and 100 ms
            Thread.sleep(r.nextInt(100));
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        return res;
    }

    public static int write(JvnLocalServer js, CounterItf jo) {
        int res = 0;
        try {
            // lock the object in read mode
            Random r = new Random();
            // invoke the method
            jo.plus();
            res = jo.getCounter();
            System.out.println("[" + js.hashCode() + "]" + "Writing " + res);
            // Sleep between 0 and 100 ms
            Thread.sleep(r.nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

}