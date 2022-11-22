package com.ysydhc.interfaceipc.utils;

import android.os.Parcelable;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ysydhc.interfaceipc.model.MethodResultModel;
import java.io.Serializable;
import java.lang.reflect.Method;
import kotlin.Metadata;

public class MethodUtils {

    public static Class<?> getClassByObj(Object obj) throws ClassNotFoundException {
        // 标记设置了哪些回调有监听
        Metadata annotation = obj.getClass().getAnnotation(Metadata.class);
        Class<?> aClass = null;
        if (annotation != null) {
            String clazzName = annotation.d2()[1];
            String realClazzName = clazzName.substring(1, clazzName.length() - 1).replace("/", ".");
            aClass = Class.forName(realClazzName);
        }
        if (aClass == null) {
            aClass = obj.getClass();
        }
        return aClass;
    }


    @NonNull
    public static MethodResultModel createResultByInvoke(Object invoke) {
        if (invoke instanceof Parcelable) {
            return new MethodResultModel((Parcelable) invoke);
        } else if (invoke instanceof Serializable) {
            return new MethodResultModel((Serializable) invoke);
        } else {
            return MethodResultModel.VOID_RESULT;
        }
    }

    @Nullable
    public static Class<?>[] getClasses(Object[] objArr) {
        Class<?>[] clazzArr = null;
        if (objArr != null) {
            clazzArr = new Class[objArr.length];
            for (int i = 0; i < objArr.length; i++) {
                clazzArr[i] = objArr[i].getClass();
            }
        }
        return clazzArr;
    }

    // todo 优化寻找时间
    public static Method findMethod(Class<?> target, String methodName, Class<?>[] classes) {
        if (target == null) {
            return null;
        }
        Method[] declaredMethods = target.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (TextUtils.equals(method.getName(), methodName)) {
                if (classes == null) {
                    return method;
                }
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != classes.length) {
                    continue;
                }
                // 对比每个参数的类型
                boolean isAllEqual = true;
                for (int j = 0; j < parameterTypes.length; j++) {
                    if (parameterTypes[j] != classes[j] && !MethodUtils.dataTypeEquals(parameterTypes[j], classes[j])) {
                        isAllEqual = false;
                        break;
                    }
                }
                if (isAllEqual) {
                    return method;
                }
            }
        }
        return findMethod(target.getSuperclass(), methodName, classes);
    }


    public static boolean dataTypeEquals(Class<?> c1, Class<?> c2) {
        if (c1 == int.class && c2 == Integer.class || c1 == Integer.class && c2 == int.class) {
            return true;
        }
        if (c1 == byte.class && c2 == Byte.class || c1 == Byte.class && c2 == byte.class) {
            return true;
        }
        if (c1 == short.class && c2 == Short.class || c1 == Short.class && c2 == short.class) {
            return true;
        }
        if (c1 == float.class && c2 == Float.class || c1 == Float.class && c2 == float.class) {
            return true;
        }
        if (c1 == long.class && c2 == Long.class || c1 == Long.class && c2 == long.class) {
            return true;
        }
        if (c1 == boolean.class && c2 == Boolean.class || c1 == Boolean.class && c2 == boolean.class) {
            return true;
        }
        return false;
    }

    @Nullable
    public static Object getResultByReturnType(Class<?> returnType) {
        if (returnType == byte.class || returnType == Byte.class) {
            return (byte) 0;
        }
        if (returnType == boolean.class || returnType == Boolean.class) {
            return false;
        }
        if (returnType == short.class || returnType == Short.class) {
            return 0;
        }
        if (returnType == int.class || returnType == Integer.class) {
            return 0;
        }
        if (returnType == float.class || returnType == Float.class) {
            return 0F;
        }
        if (returnType == long.class || returnType == Long.class) {
            return 0;
        }
        return null;
    }

}
