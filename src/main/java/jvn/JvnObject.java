/***
 * JAVANAISE API
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.io.*;

/**
 * Interface of a JVN object. A JVN object is used to acquire read/write locks
 * to access a given shared object
 */

public interface JvnObject extends Serializable {
    /*
     * A JvnObject should be serializable in order to be able to transfer a
     * reference to a JVN object remotely
     */

    /**
     * Get a Read lock on the shared object
     *
     * @throws JvnException Jvn exception
     **/
    public void jvnLockRead() throws jvn.JvnException;

    /**
     * Get a Write lock on the object
     *
     * @throws JvnException Jvn exception
     **/
    public void jvnLockWrite() throws jvn.JvnException;

    /**
     * Unlock the object
     *
     * @throws JvnException Jvn exception
     **/
    public void jvnUnLock() throws jvn.JvnException;

    /**
     * Get the object identification
     *
     * @return the object id
     * @throws JvnException Jvn exception
     **/
    public int jvnGetObjectId() throws jvn.JvnException;

    /**
     * Set the object identification
     *
     * @param id the object id
     * @throws JvnException Jvn exception
     **/
    public void jvnSetObjectId(int id) throws jvn.JvnException;

    /**
     * Get the shared object associated to this JvnObject
     *
     * @return the current JVN object state
     * @throws JvnException Jvn exception
     **/
    public Serializable jvnGetSharedObject() throws jvn.JvnException;

    /**
     * 
     * Set the shared object associated to this JvnObject
     *
     * @param o refence to shared object
     * @throws jvn.JvnException Jvn exception
     **/
    public void jvnSetSharedObject(Serializable o) throws jvn.JvnException;

    /**
     * Invalidate the Read lock of the JVN object
     *
     * @throws JvnException Jvn exception
     **/
    public void jvnInvalidateReader() throws jvn.JvnException;

    /**
     * Invalidate the Write lock of the JVN object
     *
     * @return the current JVN object state
     * @throws JvnException Jvn exception
     **/
    public Serializable jvnInvalidateWriter() throws jvn.JvnException;

    /**
     * Reduce the Write lock of the JVN object
     *
     * @return the current JVN object state
     * @throws JvnException Jvn exception
     **/
    public Serializable jvnInvalidateWriterForReader() throws jvn.JvnException;

    /**
     * Get the JVN object's lock
     * 
     * @return the current JVN object's lock
     * @throws JvnException Jvn exception
     **/
    public Lock getLock() throws jvn.JvnException;

    /**
     * Create a clone
     * 
     * @return the clone
     */
    public JvnObject clone();

    /**
     * Set the lock of the object only locally
     *
     * @param lock the state of the lock
     */
    public void setLock(Lock lock);
}
