/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.rmi.registry.*;


public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // A JVN server is managed as a singleton
    private static JvnServerImpl js = null;

    private Registry registry;

    private JvnRemoteCoord coord;

    /**
     * Default constructor
     *
     * @throws JvnException
     **/
    private JvnServerImpl(String host) throws Exception {
        super();
        this.registry = LocateRegistry.getRegistry(host, 9393);
        //TODO regarder le retour du registry si rmiregistry pas lancé

        boolean find = false;

        while(!find) {
            try {
                coord = (JvnRemoteCoord) registry.lookup("Coordinator");
                find = true;
            } catch (Exception e) {
                System.out.println("Coordinateur non disponible, nouvelle tentative dans 2s...");
                Thread.sleep(2000);
            }
        }

        System.out.println("Coord reçu");
    }

    /**
     * Static method allowing an application to get a reference to
     * a JVN server instance
     *
     * @throws JvnException
     **/
    public static JvnServerImpl jvnGetServer(String host) {
        if (js == null) {
            try {
                js = new JvnServerImpl(host);
            } catch (Exception e) {
                return null;
            }
        }
        return js;
    }

    /**
     * The JVN service is not used anymore
     *
     * @throws JvnException
     **/
    public void jvnTerminate()
            throws jvn.JvnException {
        // to be completed
    }

    /**
     * creation of a JVN object
     *
     * @param o : the JVN object state
     * @throws JvnException
     **/
    public JvnObject jvnCreateObject(Serializable o)
            throws jvn.JvnException {
        // to be completed
        return null;
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @throws JvnException
     **/
    public void jvnRegisterObject(String jon, JvnObject jo)
            throws jvn.JvnException {
        // to be completed
    }

    /**
     * Provide the reference of a JVN object beeing given its symbolic name
     *
     * @param jon : the JVN object name
     * @return the JVN object
     * @throws JvnException
     **/
    public JvnObject jvnLookupObject(String jon)
            throws jvn.JvnException {
        // to be completed
        return null;
    }

    /**
     * Get a Read lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public Serializable jvnLockRead(int joi)
            throws JvnException {
        // to be completed
        return null;

    }

    /**
     * Get a Write lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public Serializable jvnLockWrite(int joi)
            throws JvnException {
        // to be completed
        return null;
    }


    /**
     * Invalidate the Read lock of the JVN object identified by id
     * called by the JvnCoord
     *
     * @param joi : the JVN object id
     * @return void
     * @throws java.rmi.RemoteException,JvnException
     **/
    public void jvnInvalidateReader(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
        // to be completed
    }

    ;

    /**
     * Invalidate the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public Serializable jvnInvalidateWriter(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
        // to be completed
        return null;
    }

    ;

    /**
     * Reduce the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public Serializable jvnInvalidateWriterForReader(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
        // to be completed
        return null;
    }

    ;

}

 
