/***
 * Irc class : simple implementation of a chat using JAVANAISE 
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.util.Random;

import jvn.*;

public class IrcCount {

    JvnObject counter;

    /**
     * main method create a JVN object nammed IRC for representing the Chat
     * application
     *
     * @param argv arguments passed to the program
     *
     **/
    public static void main(String argv[]) {
        try {
            Thread.sleep(1000);
            // initialize JVN
            JvnServerImpl js = JvnServerImpl.jvnGetServer("localhost");

            // look up the IRC object in the JVN server
            // if not found, create it, and register it in the JVN server
            CounterItf s = (CounterItf) JvnObjectProxy.newInstance("IRC", Counter.class);

            int c = 0;
            Random r = new Random();

            // Count to 500
            while (c < 500) {
                c = write(s, js);
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

    /**
     * Increment a counter object
     *
     * @param jo CounterItf object
     * @param js JvnLocalServer
     * @return 0 if error, n greater than 0 otherwise
     */
    public static int write(CounterItf jo, JvnServerImpl js) {
        int res = 0;
        try {
            // lock the object in read mode
            Random r = new Random();
            // invoke the method
            jo.plus();
            res = jo.getCounter();
            System.out.println("[" + js.hashCode() + "]" + "Writing " + res);
            // Sleep between 0 and 1000 ms
            Thread.sleep(r.nextInt(80));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

}