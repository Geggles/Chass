package GUI;

import com.sun.istack.internal.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Used to listen to the signal 'SettingSet' but only if key fits filter value.
 */
public class SettingSetSignalFilter {
    private final String key;
    private LinkedList<Listener> listeners;

    // key == null -> accept all
    public SettingSetSignalFilter(String key){
        this.key = key;
        this.listeners = new LinkedList<>();
        Signals.getInstance().settingSet.connect(this, "slot(String, Object)");
    }

    public void slot(String key, Object value)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException{
        if (this.key == null || key.equals(this.key)){
            listeners.stream().forEach(listener -> callSlot(
                    listener.receiver, listener.method, listener.type, value)
            );
        }
    }

    private void callSlot(Object receiver, String methodName, Class type, Object value) {
        try{
            Method method = receiver.getClass().getMethod(methodName, type);
            method.invoke(receiver, value);
        } catch (Exception e){
            throw new RuntimeException(
                    "Error when calling slot "+methodName+
                    " of "+String.valueOf(receiver)+
                    " with signature "+String.valueOf(type), e);
        }
    }

    public void addListener(Object receiver, String method, Class type){
        this.listeners.add(new Listener(receiver, method, type));
    }

    public void removeListeners(Object receiver, String method, Class type){
        listeners.stream().filter(listener -> (
            !listener.receiver.equals(receiver) ||
            !listener.method.equals(method) ||
            !listener.type.equals(type)
        ));
    }

    private class Listener{
        public final Object receiver;
        public final String method;
        public final Class type;
        public Listener(Object receiver, String method, Class type){
            this.receiver = receiver;
            this.method = method;
            this.type = type;
        }
    }
}
