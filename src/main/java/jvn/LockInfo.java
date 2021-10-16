package jvn;

public class LockInfo {
    private JvnRemoteServer js;
    private Lock lock;

    public LockInfo(JvnRemoteServer js, Lock lock){
        this.js = js;
        this.lock = lock;
    }

    public Lock getLock(){
        return this.lock;
    }

    public void setLock(Lock lock){
        this.lock = lock;
    }

    public JvnRemoteServer getJvnRemoteServer(){
        return js;
    }
}
