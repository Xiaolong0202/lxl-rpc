package com.lxl;

public class ServiceConfig <T>{

    private Class<?> interfaceRef;
    private Object ref;


    public Class<?> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<?> interfaceProvider) {
        this.interfaceRef = interfaceProvider;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }
}
