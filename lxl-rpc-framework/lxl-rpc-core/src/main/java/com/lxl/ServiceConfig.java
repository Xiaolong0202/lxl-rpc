package com.lxl;

public class ServiceConfig <T>{

    private Class<?> interfaceRef;
    private Object ref;
    private String group = "default";

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

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
