/***
 * JAVANAISE Implementation JvnCoordImpl class This class implements the Javanaise central
 * coordinator Contact:
 *
 * Authors:
 */

package jvn;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.Serializable;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Registry registry;

    private int lastId;

    private HashMap<String, JvnObject> storeByName = new HashMap<>();
    private HashMap<Integer, JvnObject> storeById = new HashMap<>();
    private HashMap<Integer, List<LockInfo>> storeLocks = new HashMap<>();

    /**
     * Default constructor
     *
     * @throws JvnException
     **/
    private JvnCoordImpl() throws Exception {
        // to be completed
        this.registry = LocateRegistry.createRegistry(9393);
        this.registry.bind("Coordinator", this);
    }

    public static JvnCoordImpl getJvnCoordImpl() throws Exception {
        return new JvnCoordImpl();
    }

    /**
     * Allocate a NEW JVN object id (usually allocated to a newly created JVN
     * object)
     *
     * @throws java.rmi.RemoteException,JvnException
     **/
    public int jvnGetObjectId() throws java.rmi.RemoteException, jvn.JvnException {
        return this.lastId++;
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @param js  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/
    public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
            throws java.rmi.RemoteException, jvn.JvnException {
        // Add the JvnObject to the store if it doesn't already exist
        if (storeByName.get(jon) == null) {
            storeByName.put(jon, jo);
            jo.jvnSetObjectId(this.jvnGetObjectId());
            storeById.put(jo.jvnGetObjectId(), jo);

            List<LockInfo> locks = new ArrayList<LockInfo>();
            storeLocks.put(jo.jvnGetObjectId(), locks);

            // Add the lock info to the list
            locks.add(new LockInfo(js, jo.getLock()));
        } else {
            throw new jvn.JvnException("Object " + jon + " is already registered.");
        }
    }

    /**
     * Get the reference of a JVN object managed by a given JVN server
     *
     * @param jon : the JVN object name
     * @param js  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/
    public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException, jvn.JvnException {
        JvnObject jo = storeByName.get(jon);

        if (jo != null) {
            jo = jo.clone();
            jo.setLock(Lock.NL);
        }

        return jo;
    }

    private JvnRemoteServer getJsWithWriteLock(int joi) {
        List<LockInfo> locks = storeLocks.get(joi);
        JvnRemoteServer jsWithLock = null;

        if (locks != null) {
            for (LockInfo lockInfo : locks) {
                
                if (lockInfo.getLock() == Lock.W || lockInfo.getLock() == Lock.WC) {
                    jsWithLock = lockInfo.getJvnRemoteServer();
                    break;
                }
            }
        }

        return jsWithLock;
    }

    private List<JvnRemoteServer> getJsWithReadLock(int joi, JvnRemoteServer current) {
        List<LockInfo> locks = storeLocks.get(joi);
        List<JvnRemoteServer> jsWithReadLock = new ArrayList<JvnRemoteServer>();

        if (locks != null) {
            for (LockInfo lockInfo : locks) {
                if (lockInfo.getLock() == Lock.R && !current.equals(lockInfo.getJvnRemoteServer())) {
                    jsWithReadLock.add(lockInfo.getJvnRemoteServer());
                }
            }
        }

        return jsWithReadLock;
    }

    public void updateLockInfo(int joi, JvnRemoteServer js, Lock lock){
        // Update LockInfo for the server asking for a read lock
        List<LockInfo> locks = storeLocks.get(joi);
        Boolean found = false;
        if (locks != null) {
            for (LockInfo lockInfo : locks) {
                if (lockInfo.getJvnRemoteServer().equals(js)) {
                    found = true;
                    lockInfo.setLock(lock);
                }
            }

            // Si pas de lockInfo dans la liste on en créé un
            if (!found) {
                locks.add(new LockInfo(js, lock));
            }
        } else {
            // if lock list was null for joi create a new list containing the
            // newly acquired readlock
            locks = new ArrayList<LockInfo>();
            locks.add(new LockInfo(js, lock));
            storeLocks.put(joi, locks);
        }

                  
        System.out.println("Locks for " + joi +" : ");
        for(LockInfo lockInfo : this.storeLocks.get(joi)){
            System.out.println("\t" + lockInfo.getJvnRemoteServer().hashCode() + " - " + lockInfo.getLock());
        }
    }

    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/
    public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        System.out.println(js.hashCode() + " wants to acquire lock read on " + joi);

        JvnRemoteServer jsWithLock = null;
        Serializable s = null;

        while ((jsWithLock = getJsWithWriteLock(joi)) != null) {
            System.out.println("Invalidate Writer for Reader : " + jsWithLock.hashCode());
            s = jsWithLock.jvnInvalidateWriterForReader(joi);
            this.updateLockInfo(joi, jsWithLock, Lock.R);
            System.out.println(jsWithLock.hashCode());
        }

        updateLockInfo(joi, js, Lock.R);

        // On met à jour l'objet qui possèdait le lock write dans le store
        // (Il faut aussi le faire dans le storeByName mais pas eu le temps : marche
        // sans pour l'instant à priori)
        JvnObject o = storeById.get(joi);
        if(s != null)
            o.jvnSetSharedObject(s);

        // Renvoyer l'objet courant
        return o.jvnGetSharedObject();
    }

    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/
    public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        System.out.println(js.hashCode() + " wants to acquire lock write on " + joi);

        JvnRemoteServer jsWithLock = null;
        List<JvnRemoteServer> jsWithReadLock = null;
        Serializable s = null;

        // Acquire the lock
        jsWithLock = getJsWithWriteLock(joi);
        if(jsWithLock != null) {
            System.out.println("Invalidate Writer : " + jsWithLock.hashCode());
            s = jsWithLock.jvnInvalidateWriter(joi);
            this.updateLockInfo(joi, jsWithLock, Lock.NL);
        }

        while (!(jsWithReadLock = getJsWithReadLock(joi, js)).isEmpty()) {
            // invalidate readers one by one
            System.out.println("Invalidate Reader : " + jsWithReadLock.get(0).hashCode());
            jsWithReadLock.get(0).jvnInvalidateReader(joi);
            if(jsWithLock != jsWithReadLock.get(0))
                this.updateLockInfo(joi, jsWithReadLock.get(0), Lock.NL);
            jsWithReadLock.remove(0);
        }

        updateLockInfo(joi, js, Lock.W);

        // On met à jour l'objet qui possèdait le lock write dans le store
        // (Il faut aussi le faire dans le storeByName mais pas eu le temps : marche
        // sans pour l'instant à priori)
        if(s != null) {
            JvnObject o = storeById.get(joi);
            o.jvnSetSharedObject(s);
        }

        // Renvoyer l'objet courant
        return storeById.get(joi).jvnGetSharedObject();

    }

    /**
     * A JVN server terminates
     *
     * @param js : the remote reference of the server
     * @throws java.rmi.RemoteException, JvnException
     **/
    public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        // to be completed
    }
}
