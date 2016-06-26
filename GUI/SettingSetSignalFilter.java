package GUI;

import com.sun.istack.internal.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Used to listen to the signal 'SettingSet' but only if key fits filter value.
 */
public class SettingSetSignalFilter {
    private final Object receiver;
    private final String methodName;
    private final String key;
    private final Class type;
    /**
     * Listen only to those emissions that have the key as key.
     * <p>
     *     If key is null, listen to all emissions.
     * </p>
     * @param receiver    See QtSignal definition.
     * @param method      See QtSignal definition.
     * @param key         The key to filter the emissions by.
     */
    public SettingSetSignalFilter(@NotNull Object receiver,
                                  @NotNull String method,
                                  String key,
                                  Class type) {
        this.receiver = receiver;
        this.methodName = method;
        this.key = key;
        this.type = type;
    }

    public void listen(String key, Object value) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException{
        System.out.print("Got a Signal in!: "+key+"! Value: ");
        System.out.print(value);
        if (key.equals(this.key)){
            Method method = receiver.getClass().getMethod(methodName, type);
            method.invoke(receiver, value);
        }
    }
}
