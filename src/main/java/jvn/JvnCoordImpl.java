/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
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


public class JvnCoordImpl
        extends UnicastRemoteObject
        implements JvnRemoteCoord {


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
     * Allocate a NEW JVN object id (usually allocated to a
     * newly created JVN object)
     *
     * @throws java.rmi.RemoteException,JvnException
     **/
    public int jvnGetObjectId()
            throws java.rmi.RemoteException, jvn.JvnException {
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
        if(storeByName.get(jon) == null){
          storeByName.put(jon, jo);
          jo.jvnSetObjectId(this.jvnGetObjectId());
          storeById.put(jo.jvnGetObjectId(), jo);
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
    public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
            throws java.rmi.RemoteException, jvn.JvnException {
        JvnObject jo = storeByName.get(jon);

        if(jo != null) {
            jo = jo.clone();

            // Get the locks list for this JvnObject
            List<LockInfo> locks = storeLocks.get(jo.jvnGetObjectId());
            if(locks == null){
              // No locks registered for this JvnObject ; add it to the hashmap
              locks = new ArrayList<LockInfo>();
              storeLocks.put(jo.jvnGetObjectId(), locks);

              // Add the lock info to the list
              locks.add(new LockInfo(js, jo.getLock()));
            }
    
        }

        return jo;
    }

    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/
    public Serializable jvnLockRead(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        List<LockInfo> locks = storeLocks.get(joi);
        // Look for a server with a write lock on the object
        JvnRemoteServer jsWithLock = null;
        do {
            for (LockInfo lockInfo : locks) {
                if(lockInfo.getLock() == Lock.W) {
                    jsWithLock = lockInfo.getJvnRemoteServer();
                    break;
                }
            }

            // Found a server with a lock
            if(jsWithLock != null){
                try {
                    // Wait for it to unlock the object
                    wait();

                    // Ici je crois qu'il faut mettre la suite du code dans un bloc séparé (si notifyAll alors tous les threads vont se réveiller et invalidate donc pas bon)
                    // mais pour l'instant balec ; c'est juste pour tester
                    Serializable s = jsWithLock.jvnInvalidateWriterForReader(joi);
                    
                    // Update LockInfo for the server asking for a read lock
                    Boolean found = null;
                    for (LockInfo lockInfo : locks) {
                        if(lockInfo.getJvnRemoteServer() == js){
                            found = true;
                            lockInfo.setLock(Lock.R);
                        }
                    }
                    // Si pas de lockInfo dans la liste on en créé un
                    if(!found){
                        locks.add(new LockInfo(js, Lock.R));
                    }

                    // On met à jour l'objet dans le store
                    // (Il faut aussi le faire dans le storeByName mais pas eu le temps : marche sans pour l'instant à priori)
                    JvnObject o = storeById.get(joi);
                    o.jvnSetSharedObject(s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while(jsWithLock != null);

        // Renvoyer l'objet courant
        return storeById.get(joi).jvnGetSharedObject();
    }

    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/
    public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        List<LockInfo> locks = storeLocks.get(joi);
        // Look for a server with a write lock on the object
        JvnRemoteServer jsWithLock = null;
        do {
            for (LockInfo lockInfo : locks) {
                if(lockInfo.getLock() == Lock.W) {
                    jsWithLock = lockInfo.getJvnRemoteServer();
                    break;
                }
            }

            if(jsWithLock != null){
                try {
                    wait();
                    jsWithLock.jvnInvalidateWriter(joi);

                    // Update LockInfo for the server asking for a read lock
                    Boolean found = null;
                    for (LockInfo lockInfo : locks) {
                        if(lockInfo.getJvnRemoteServer() == js){
                            found = true;
                            lockInfo.setLock(Lock.W);
                        }
                    }
                    if(!found){
                        locks.add(new LockInfo(js, Lock.W));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while(jsWithLock != null);

        return storeById.get(joi).jvnGetSharedObject();
    }

    /**
     * A JVN server terminates
     *
     * @param js : the remote reference of the server
     * @throws java.rmi.RemoteException, JvnException
     **/
    public void jvnTerminate(JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        // to be completed
    }
}

 
