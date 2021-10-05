package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

    private Lock lock;

    private Serializable o;

    private int id;

    /* JvnObject Constructor */
    public JvnObjectImpl(Serializable o){
        this.o = o;
        this.lock = Lock.NL;
    }

	/**
	* Get a Read lock on the shared object 
	* @throws JvnException
	**/
	public void jvnLockRead()
	throws jvn.JvnException {
        this.lock = this.lock == Lock.WC ? Lock.RWC : Lock.R;
    }

	/**
	* Get a Write lock on the object 
	* @throws JvnException
	**/
	public void jvnLockWrite()
     	throws jvn.JvnException {
        this.lock = Lock.W;
    }

	/**
	* Unlock  the object 
	* @throws JvnException
	**/
	public void jvnUnLock()
	throws jvn.JvnException {
        this.lock = Lock.NL;
    }
	
	/**
	* Get the object identification
	* @throws JvnException
	**/
	public int jvnGetObjectId()
	throws jvn.JvnException {
        return this.id;
    }
	
	/**
	* Get the shared object associated to this JvnObject
	* @throws JvnException
	**/
	public Serializable jvnGetSharedObject()
	throws jvn.JvnException {
        return this.o;
    }
	
	/**
	* Invalidate the Read lock of the JVN object 
	* @throws JvnException
	**/
  public void jvnInvalidateReader()
	throws jvn.JvnException {
        this.lock = Lock.NL;
    }
	    
	/**
	* Invalidate the Write lock of the JVN object  
	* @return the current JVN object state
	* @throws JvnException
	**/
  public Serializable jvnInvalidateWriter()
	throws jvn.JvnException {
        this.lock = Lock.NL;
        return this.o;
    }
	
	/**
	* Reduce the Write lock of the JVN object 
	* @return the current JVN object state
	* @throws JvnException
	**/
   public Serializable jvnInvalidateWriterForReader()
	 throws jvn.JvnException {
         this.lock = this.lock == Lock.W ? Lock.RC : Lock.R;
         return this.o;
     } 

    public Lock getLock(){
        return this.lock;
    }
}
