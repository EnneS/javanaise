/***
 * JAVANAISE API
 * JvnRemoteCoord interface
 * This interface defines the remote interface provided by the Javanaise coordinator
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.*;
import java.io.*;

/**
 * Remote Interface of the JVN Coordinator
 */

public interface JvnRemoteCoord extends Remote {

    /**
     * Allocate a NEW JVN object id (usually allocated to a newly created JVN
     * object).
     *
     * @return The object id.
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
     **/
    public int jvnGetObjectId() throws java.rmi.RemoteException, jvn.JvnException;

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @param js  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
     **/
    public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
            throws java.rmi.RemoteException, jvn.JvnException;

    /**
     * Get the reference of a JVN object managed by a given JVN server
     *
     * @param jon : the JVN object name
     * @param js  : the remote reference of the JVNServer
     * @return reference to a jvnObject
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
     **/
    public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException, jvn.JvnException;

    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
     **/
    public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException;

    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
     **/
    public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException;

    /**
     * A JVN server terminates
     *
     * @param js : the remote reference of the server
     * @throws java.rmi.RemoteException Remote exception
     * @throws JvnException             Jvn exception
     **/
    public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException;

}
