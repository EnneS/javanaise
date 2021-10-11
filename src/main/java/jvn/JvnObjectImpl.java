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
	 * @throws JvnException
	 **/
	public void jvnLockRead() throws jvn.JvnException {
		if(this.lock == Lock.RC)
			this.lock = Lock.R;
		else if(this.lock == Lock.WC)
			this.lock = Lock.RWC;
		else if (this.lock == Lock.W || this.lock == Lock.R || this.lock == Lock.RWC)
			return;

		JvnLocalServer js = JvnServerImpl.jvnGetServer("localhost");
		this.o = js.jvnLockRead(this.jvnGetObjectId());
	}

	/**
	 * Get a Write lock on the object
	 * 
	 * @throws JvnException
	 **/
	public void jvnLockWrite() throws jvn.JvnException {
		if(this.lock == Lock.WC)
			this.lock = Lock.W;
		else if (this.lock == Lock.W )
			return;

		JvnLocalServer js = JvnServerImpl.jvnGetServer("localhost");
		this.o = js.jvnLockWrite(this.jvnGetObjectId());
		this.lock = Lock.W;
	}

	/**
	 * Unlock the object
	 * 
	 * @throws JvnException
	 **/
	public synchronized void jvnUnLock() throws jvn.JvnException {
		if(this.lock == Lock.R)
			this.lock = Lock.RC;
		else if(this.lock == Lock.W)
			this.lock = Lock.WC;

		notify();
	}

	/**
	 * Get the object identification
	 * 
	 * @throws JvnException
	 **/
	public int jvnGetObjectId() throws jvn.JvnException {
		return this.id;
	}

	/**
	 * Set the object identification
	 * 
	 * @throws JvnException
	 **/
	public void jvnSetObjectId(int id) throws jvn.JvnException {
		this.id = id;
	}

	/**
	 * Get the shared object associated to this JvnObject
	 * 
	 * @throws JvnException
	 **/
	public Serializable jvnGetSharedObject() throws jvn.JvnException {
		return this.o;
	}

	/**
	 * Set the shared object associated to this JvnObject
	 * 
	 * @throws JvnException
	 **/
	public void jvnSetSharedObject(Serializable o) throws jvn.JvnException {
		this.o = o;
	}

	/**
	 * Invalidate the Read lock of the JVN object
	 * 
	 * @throws JvnException
	 **/
	public void jvnInvalidateReader() throws jvn.JvnException {
		this.lock = Lock.NL;
	}

	/**
	 * Invalidate the Write lock of the JVN object
	 * 
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public synchronized Serializable jvnInvalidateWriter() throws jvn.JvnException {
		if(this.lock != Lock.WC && this.lock != Lock.RC && this.lock != Lock.NL) {
			try {
				wait();
			}catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		this.lock = Lock.NL;
		return o;
	}

	/**
	 * Reduce the Write lock of the JVN object
	 * 
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public synchronized Serializable jvnInvalidateWriterForReader() throws jvn.JvnException {
		if(this.lock != Lock.WC && this.lock != Lock.RC && this.lock != Lock.NL) {
			try {
				wait();
			}catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		this.lock = this.lock == Lock.W ? Lock.RC : Lock.R;
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
