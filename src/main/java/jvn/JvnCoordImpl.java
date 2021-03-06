/***
 * JAVANAISE Implementation JvnCoordImpl class This class implements the Javanaise central
 * coordinator Contact:
 *
 * Authors:
 */

package jvn;

import java.io.*;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String STORE_BY_ID_FILENAME = "storeById.txt";
    private final String STORE_BY_NAME_FILENAME = "storeByName.txt";
    private final String PATH_TO_FILE = "src/main/resources/";

    private Registry registry;

    private int lastId;

    private HashMap<String, Integer> storeByName = new HashMap<>();
    private HashMap<Integer, JvnObject> storeById = new HashMap<>();
    private HashMap<Integer, List<LockInfo>> storeLocks = new HashMap<>();

    private final int NB_TRIES = 10;

    /**
     * Default constructor
     *
     * @throws JvnException
     **/
    private JvnCoordImpl() throws Exception {
        this.load();
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
     * @return The object id.
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
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
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
     **/
    public synchronized void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
            throws java.rmi.RemoteException, jvn.JvnException {
        // Add the JvnObject to the store if it doesn't already exist
        if (storeByName.get(jon) == null) {
            storeByName.put(jon, jo.jvnGetObjectId());
            storeById.put(jo.jvnGetObjectId(), jo);

            List<LockInfo> locks = new ArrayList<LockInfo>();
            storeLocks.put(jo.jvnGetObjectId(), locks);

            // Add the lock info to the list
            locks.add(new LockInfo(js, Lock.W));
        } else {
            throw new jvn.JvnException("Object " + jon + " is already registered.");
        }
    }

    /**
     * Get the reference of a JVN object managed by a given JVN server
     *
     * @param jon : the JVN object name
     * @param js  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
     **/
    public synchronized JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException, jvn.JvnException {
        JvnObject jo = storeById.get(storeByName.get(jon));

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

                if (lockInfo.getLock() == Lock.W) {
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

    private void updateLockInfo(int joi, JvnRemoteServer js, Lock lock) {
        // Update LockInfo for the server asking for a read lock
        List<LockInfo> locks = storeLocks.get(joi);
        Boolean found = false;
        if (locks != null) {
            int i = 0;
            while(i < locks.size() && !locks.get(i).getJvnRemoteServer().equals(js)){
                i++;
            }
            if(i == locks.size()){
                locks.add(new LockInfo(js, lock));
            } else {
                locks.get(i).setLock(lock);
            }

        } else {
            // if lock list was null for joi create a new list containing the
            // newly acquired readlock
            locks = new ArrayList<LockInfo>();
            locks.add(new LockInfo(js, lock));
            storeLocks.put(joi, locks);
        }

        if (JvnGlobals.debug) {
            System.out.println("Locks for " + joi + " : ");
            for (LockInfo lockInfo : this.storeLocks.get(joi)) {
                System.out.println("\t" + lockInfo.getJvnRemoteServer().hashCode() + " - " + lockInfo.getLock());
            }
        }
    }

    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
     **/
    public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        if (JvnGlobals.debug)
            System.out.println(js.hashCode() + " wants to acquire lock read on " + joi);

        JvnRemoteServer jsWithLock = null;
        Serializable s = null;

        while ((jsWithLock = getJsWithWriteLock(joi)) != null) {
            if (JvnGlobals.debug)
                System.out.println("Invalidate Writer for Reader : " + jsWithLock.hashCode());

            s = (Serializable) this.contactJvnServer(jsWithLock, "jvnInvalidateWriterForReader", joi);

            if (s != null)
                this.updateLockInfo(joi, jsWithLock, Lock.R);
        }

        updateLockInfo(joi, js, Lock.R);

        // On met ?? jour l'objet qui poss??dait le lock write dans le store
        // (Il faut aussi le faire dans le storeByName mais pas eu le temps : marche
        // sans pour l'instant ?? priori)
        JvnObject o = storeById.get(joi);
        if (s != null) {
            o.jvnSetSharedObject(s);
            try {
                this.save();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        // Renvoyer l'objet courant
        return o.jvnGetSharedObject();
    }

    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
     **/
    public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        if (JvnGlobals.debug)
            System.out.println(js.hashCode() + " wants to acquire lock write on " + joi);

        JvnRemoteServer jsWithLock = null;
        List<JvnRemoteServer> jsWithReadLock = null;
        Serializable s = null;

        // Acquire the lock
        jsWithLock = getJsWithWriteLock(joi);
        if (jsWithLock != null) {
            if (JvnGlobals.debug)
                System.out.println("Invalidate Writer : " + jsWithLock.hashCode());

            s = (Serializable) this.contactJvnServer(jsWithLock, "jvnInvalidateWriter", joi);

            if (s != null){
                this.updateLockInfo(joi, jsWithLock, Lock.NL);
            }
        }

        while (!(jsWithReadLock = getJsWithReadLock(joi, js)).isEmpty()) {
            // invalidate readers one by one
            if (JvnGlobals.debug)
                System.out.println("Invalidate Reader : " + jsWithReadLock.get(0).hashCode());

            this.contactJvnServer(jsWithReadLock.get(0), "jvnInvalidateReader", joi);

            if (jsWithLock != jsWithReadLock.get(0))
                this.updateLockInfo(joi, jsWithReadLock.get(0), Lock.NL);

            jsWithReadLock.remove(0);
        }

        updateLockInfo(joi, js, Lock.W);

        // On met ?? jour l'objet qui poss??dait le lock write dans le store
        // (Il faut aussi le faire dans le storeByName mais pas eu le temps : marche
        // sans pour l'instant ?? priori)
        if (s != null) {
            JvnObject o = storeById.get(joi);
            o.jvnSetSharedObject(s);
            try {
                this.save();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        // Renvoyer l'objet courant
        return storeById.get(joi).jvnGetSharedObject();

    }

    /**
     * A JVN server terminates
     *
     * @param js : the remote reference of the server
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
     **/
    public synchronized void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        // Remove lock infos for this js
        for (int joi : this.storeById.keySet()) {
            List<LockInfo> locksToRemove = new ArrayList<>();
            List<LockInfo> locks = this.storeLocks.get(joi);
            for (LockInfo lockInfo : locks) {
                if (lockInfo.getJvnRemoteServer().equals(js)) {
                    locksToRemove.add(lockInfo);
                }
            }
            locks.removeAll(locksToRemove);
        }
    }

    private Object contactJvnServer(JvnRemoteServer js, String method, int joi) {
        Object o = null;
        Method m;

        try {
            m = JvnRemoteServer.class.getDeclaredMethod(method, int.class);
        } catch (NoSuchMethodException e) {
            System.out.println("Error 1");
            System.err.println(e.getMessage());
            return null;
        }

        int tries = 0;
        boolean success = false;
        while (tries < NB_TRIES && !success) {
            try {
                o = m.invoke(js, joi);
                success = true;
            } catch (Exception e) {
                System.err.println("JvnServer unreachable");
            }
            if (!success)
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            tries++;
        }

        if (!success) {
            try {
                jvnTerminate(js);
            } catch (JvnException | RemoteException e) {
                System.out.println("Terminate");
                System.err.println(e.getMessage());
            }
        }

        return o;
    }

    public void load() throws IOException, ClassNotFoundException {
        FileInputStream file;
        ObjectInputStream reader;

        if (!this.createIfNotPresent(PATH_TO_FILE + STORE_BY_ID_FILENAME)) {
            file = new FileInputStream(PATH_TO_FILE + STORE_BY_ID_FILENAME);

            try {
                reader = new ObjectInputStream(file);
                this.storeById = (HashMap<Integer, JvnObject>) reader.readObject();
                reader.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
            file.close();
        }
        if (this.storeById == null)
            this.storeById = new HashMap<>();

        if (!this.createIfNotPresent(PATH_TO_FILE + STORE_BY_NAME_FILENAME)) {
            file = new FileInputStream(PATH_TO_FILE + STORE_BY_NAME_FILENAME);

            try {
                reader = new ObjectInputStream(file);
                this.storeByName = (HashMap<String, Integer>) reader.readObject();
                reader.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
            file.close();
        }
        if (this.storeByName == null)
            this.storeByName = new HashMap<>();

        this.lastId = 0;
        for (Integer key : this.storeById.keySet()) {
            if (this.lastId < key)
                this.lastId = key;
        }
    }

    public boolean createIfNotPresent(String path) throws IOException {
        File file = new File(path);
        boolean newFile = file.createNewFile();
        file.setWritable(true);
        file.setReadable(true);
        return newFile;

    }

    public void save() throws IOException {
        FileOutputStream file;
        ObjectOutputStream writer;

        file = new FileOutputStream(PATH_TO_FILE + STORE_BY_ID_FILENAME);
        writer = new ObjectOutputStream(file);
        writer.writeObject(this.storeById);
        writer.flush();
        writer.close();
        file.close();

        file = new FileOutputStream(PATH_TO_FILE + STORE_BY_NAME_FILENAME);
        writer = new ObjectOutputStream(file);
        writer.writeObject(this.storeByName);
        writer.flush();
        writer.close();
        file.close();
    }
}
