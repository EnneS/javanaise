package jvn;

import annotations.Read;
import annotations.Write;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JvnObjectProxy implements InvocationHandler {

    private JvnObject jvnObject;

    private JvnObjectProxy(String jon, Class c) throws JvnException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        JvnObject o = JvnServerImpl.jvnGetServer("localhost").jvnLookupObject(jon);

        if(o == null) {
            try {
                System.out.println("Création de l'objet");
                this.jvnObject = JvnServerImpl.jvnGetServer("localhost").jvnCreateObject((Serializable) c.getDeclaredConstructor().newInstance());
                this.jvnObject.jvnUnLock();
                JvnServerImpl.jvnGetServer("localhost").jvnRegisterObject(jon, this.jvnObject);
            } catch (JvnException e) {
                this.jvnObject = JvnServerImpl.jvnGetServer("localhost").jvnLookupObject(jon);
            }

        } else {
            this.jvnObject = o;
        }
    }

    public static Object newInstance(String jon, Class c) throws JvnException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        return java.lang.reflect.Proxy.newProxyInstance(
                c.getClassLoader(),
                c.getInterfaces(),
                new JvnObjectProxy(jon, c));
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        Object r = null;

        try {
            if(method.isAnnotationPresent(Read.class))
                r = this.doMethodForRead(method, args);

            if(method.isAnnotationPresent(Write.class))
                this.doMethodForWrite(method, args);

        } catch (Exception e ) {
            System.err.println(e.getMessage());
        }

        return r;
    }

    private Object doMethodForRead(Method method, Object[] args) throws JvnException, InvocationTargetException, IllegalAccessException {
        this.jvnObject.jvnLockRead();
        Object r = this.doMethod(method, args);
        this.jvnObject.jvnUnLock();

        return r;
    }

    private void doMethodForWrite(Method method, Object[] args) throws JvnException, InvocationTargetException, IllegalAccessException {
        this.jvnObject.jvnLockWrite();
        this.doMethod(method, args);
        this.jvnObject.jvnUnLock();
    }

    private Object doMethod(Method m, Object[] args) throws JvnException, InvocationTargetException, IllegalAccessException {
        return m.invoke(this.jvnObject.jvnGetSharedObject(), args);
    }
}
