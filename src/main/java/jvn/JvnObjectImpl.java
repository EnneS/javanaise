package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

    private Lock lock;

    private Serializable o;

    private int id;

    /* JvnObject Constructor */
    public JvnObjectImpl(Serializable o) {
        this.o = o;
    }

    /**
     * Get a Read lock on the shared object
     * 
     * @throws JvnException Jvn exception
     **/
    public void jvnLockRead() throws jvn.JvnException {
        if (JvnGlobals.debug)
            System.out.print("[jvnLockRead] Lock " + getLock() + " ==> ");

        synchronized (this) {
            if (this.lock == Lock.RC) {
                this.lock = Lock.R;
                if (JvnGlobals.debug)
                    System.out.println(getLock());
                return;
            } else if (this.lock == Lock.WC) {
                this.lock = Lock.RWC;
                if (JvnGlobals.debug)
                    System.out.println(getLock());
                return;
            } else if (this.lock == Lock.W || this.lock == Lock.R || this.lock == Lock.RWC) {
                if (JvnGlobals.debug)
                    System.out.println(getLock());
                return;
            }
        }

        this.lock = Lock.R;
        JvnLocalServer js = JvnServerImpl.jvnGetServer("localhost");
        this.o = js.jvnLockRead(this.jvnGetObjectId());

        if (JvnGlobals.debug)
            System.out.println(getLock());
    }

    /**
     * Get a Write lock on the object
     * 
     * @throws JvnException Jvn exception
     **/
    public void jvnLockWrite() throws jvn.JvnException {
        if (JvnGlobals.debug)
            System.out.print("[jvnLockWrite] Lock " + getLock() + " ==> ");

        synchronized (this) {
            if (this.lock == Lock.WC || this.lock == Lock.RWC) {
                this.lock = Lock.W;
                if (JvnGlobals.debug)
                    System.out.println(getLock());
                return;
            } else if (this.lock == Lock.W) {
                if (JvnGlobals.debug)
                    System.out.println(getLock());
                return;
            }
        }

        this.lock = Lock.W;
        JvnLocalServer js = JvnServerImpl.jvnGetServer("localhost");
        this.o = js.jvnLockWrite(this.jvnGetObjectId());

        if (JvnGlobals.debug)
            System.out.println(getLock());
    }

    /**
     * Unlock the object
     * 
     * @throws JvnException Jvn exception
     **/
    public synchronized void jvnUnLock() throws jvn.JvnException {
        if (JvnGlobals.debug)
            System.out.print("[jvnUnlock] Lock " + getLock() + " ==> ");

        if (this.lock == Lock.R)
            this.lock = Lock.RC;
        else if (this.lock == Lock.W || this.lock == Lock.RWC)
            this.lock = Lock.WC;

        notify();

        if (JvnGlobals.debug)
            System.out.println(getLock());
    }

    /**
     * Get the object identification
     * 
     * @throws JvnException Jvn exception
     **/
    public int jvnGetObjectId() throws jvn.JvnException {
        return this.id;
    }

    /**
     * Set the object identification
     *
     * @param id the object id
     * @throws JvnException Jvn exception
     **/
    public void jvnSetObjectId(int id) throws jvn.JvnException {
        this.id = id;
    }

    /**
     * Get the shared object associated to this JvnObject
     * 
     * @throws JvnException Jvn exception
     **/
    public Serializable jvnGetSharedObject() throws jvn.JvnException {
        return this.o;
    }

    /**
     * Set the shared object associated to this JvnObject
     *
     * @param o a reference to the shared object
     * @throws JvnException Jvn exception
     **/
    public void jvnSetSharedObject(Serializable o) throws jvn.JvnException {
        this.o = o;
    }

    /**
     * Invalidate the Read lock of the JVN object
     * 
     * @throws JvnException Jvn exception
     **/
    public synchronized void jvnInvalidateReader() throws jvn.JvnException {
        if (JvnGlobals.debug)
            System.out.print("[InvalidateReader] Lock " + getLock() + " ==> ");

        this.lock = Lock.NL;

        if (JvnGlobals.debug)
            System.out.println(getLock());
    }

    /**
     * Invalidate the Write lock of the JVN object
     * 
     * @return the current JVN object state
     * @throws JvnException Jvn exception
     **/
    public synchronized Serializable jvnInvalidateWriter() throws jvn.JvnException {
        if (JvnGlobals.debug)
            System.out.print("[InvalidateWriter] Lock " + getLock() + " ==> ");

        while (this.lock == Lock.W) {
            try {
                wait();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        this.lock = Lock.NL;

        if (JvnGlobals.debug)
            System.out.println(getLock());

        return o;
    }

    /**
     * Reduce the Write lock of the JVN object
     * 
     * @return the current JVN object state
     * @throws JvnException Jvn exception
     **/
    public synchronized Serializable jvnInvalidateWriterForReader() throws jvn.JvnException {
        if (JvnGlobals.debug)
            System.out.print("[InvalidateWriterForReader] Lock " + getLock() + " ==> ");

        while (this.lock == Lock.W) {
            try {
                wait();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        this.lock = Lock.RC;

        if (JvnGlobals.debug)
            System.out.println(getLock());

        return this.o;
    }

    public Lock getLock() {
        return this.lock;
    }

    public JvnObject clone() {
        JvnObjectImpl clone = new JvnObjectImpl(this.o);
        try {
            clone.id = this.jvnGetObjectId();
        } catch (JvnException e) {
            e.printStackTrace();
        }
        return clone;
    }

    public void setLock(Lock lock) {
        this.lock = lock;
    }
}
