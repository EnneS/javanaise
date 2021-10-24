/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
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

    private Hashtable<String, JvnObject> storeByName = new Hashtable<>();
    private Hashtable<Integer, JvnObject> storeById = new Hashtable<>();

    /**
     * Default constructor
     *
     * @throws JvnException Jvn exception
     **/
    private JvnServerImpl(String host) throws Exception {
        super();
        this.registry = LocateRegistry.getRegistry(host, 9393);
        // TODO regarder le retour du registry si rmiregistry pas lancé

        boolean find = false;

        while (!find) {
            try {
                coord = (JvnRemoteCoord) registry.lookup("Coordinator");
                find = true;
            } catch (Exception e) {
                System.out.println("Coordinateur non disponible, nouvelle tentative dans 2s...");
                Thread.sleep(2000);
            }
        }

        if (JvnGlobals.debug)
            System.out.println("Coord reçu - Server Hash : " + this.hashCode());
    }

    /**
     * Static method allowing an application to get a reference to a JVN server
     * instance
     *
     * @param host the host name
     * @return the Jvn Server object
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
     * @throws JvnException Jvn exception
     **/
    public void jvnTerminate() throws jvn.JvnException {
        try {
            coord.jvnTerminate(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * creation of a JVN object
     *
     * @return the JVN object
     * @param o : the JVN object state
     * @throws JvnException Jvn exception
     **/
    public JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException {

        JvnObject obj = new JvnObjectImpl(o);
        obj.setLock(Lock.W);

        return obj;
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @throws JvnException Jvn exception
     **/
    public void jvnRegisterObject(String jon, JvnObject jo) throws jvn.JvnException {
        boolean response = false;

        // Attempt to register the object coordinator side until we get a response.
        while (!response) {
            try {
                this.coord.jvnRegisterObject(jon, jo, this);
                this.storeByName.put(jon, jo);
                this.storeById.put(jo.jvnGetObjectId(), jo);
                response = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (!response) {
                System.out.println("Erreur: coordinateur non disponible, nouvelle tentative dans 2s...");
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    /**
     * Provide the reference of a JVN object beeing given its symbolic name
     *
     * @param jon : the JVN object name
     * @return the JVN object
     * @throws JvnException Jvn exception
     **/
    public JvnObject jvnLookupObject(String jon) throws jvn.JvnException {

        JvnObject jo = null;
        boolean coordResponded = false;
        while (!coordResponded) {
            try {
                jo = this.coord.jvnLookupObject(jon, this);
                if (jo != null) {
                    this.storeByName.put(jon, jo);
                    this.storeById.put(jo.jvnGetObjectId(), jo);
                }
                coordResponded = true;
            } catch (RemoteException e) {
                System.err.println(e.getMessage());
            }

            if (!coordResponded) {
                System.out.println("Erreur: coordinateur non disponible, nouvelle tentative dans 2s...");
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }

        return jo;
    }

    /**
     * Get a Read lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException Jvn exception
     **/
    public Serializable jvnLockRead(int joi) throws JvnException {

        Serializable s = null;
        boolean received = false;

        while (!received) {
            try {
                s = this.coord.jvnLockRead(joi, this);
                received = true;
            } catch (RemoteException e) {
                System.err.println(e.getMessage());
            }

            if (!received) {
                System.out.println("Erreur: coordinateur non disponible, nouvelle tentative dans 2s...");
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }

        return s;
    }

    /**
     * Get a Write lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException Jvn exception
     **/
    public Serializable jvnLockWrite(int joi) throws JvnException {

        Serializable s = true;
        boolean received = false;

        while (!received) {
            try {
                s = this.coord.jvnLockWrite(joi, this);
                received = true;
            } catch (RemoteException e) {
                System.err.println(e.getMessage());
            }

            if (!received) {
                System.out.println("Erreur: coordinateur non disponible, nouvelle tentative dans 2s...");
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }

        return s;
    }

    /**
     * Invalidate the Read lock of the JVN object identified by id called by the
     * JvnCoord
     *
     * @param joi : the JVN object id
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
     **/
    public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
        JvnObject o = this.storeById.get(joi);
        if (o != null) {
            o.jvnInvalidateReader();
        }
    }

    /**
     * Invalidate the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
     **/
    public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException, jvn.JvnException {
        JvnObject o = this.storeById.get(joi);
        if (o != null) {
            return o.jvnInvalidateWriter();
        }
        return null;
    }

    /**
     * Reduce the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
     **/
    public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
        JvnObject o = this.storeById.get(joi);
        if (o != null) {
            return o.jvnInvalidateWriterForReader();
        }
        return null;
    }

}
