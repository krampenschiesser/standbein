package de.ks.reflection;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class PropertyPath<T> {
  public static <T> PropertyPath<T> of(Class<T> clazz) {
    return new PropertyPath<>(clazz);
  }

  private static final Logger log = LogManager.getLogger(PropertyPath.class);

  protected Class<T> root;
  protected List<Method> methodPath = new ArrayList<>(25);
  protected List<String> stringPath = new ArrayList<>(25);
  protected List<String> fieldPath = new ArrayList<>(25);
  protected boolean setter;
  protected boolean getter;
  protected Class<?>[] parameterTypes;
  protected Class<?> returnType;
  protected Field field;

  public PropertyPath(Class<T> root) {
    this.root = root;
  }

  @SuppressWarnings("unchecked")
  public T build() {
    stringPath.clear();
    fieldPath.clear();
    setter = false;
    getter = false;
    parameterTypes = null;
    returnType = null;
    field = null;

    return (T) callBack(root);
  }

  protected Object callBack(Class<?> clazz) {
    if (Modifier.isFinal(clazz.getModifiers())) {
      return null;
    }
    return Enhancer.create(clazz, new MethodInterceptor() {
      @Override
      public Object intercept(Object arg0, Method arg1, Object[] arg2, MethodProxy arg3) throws Throwable {
        String methodName = arg1.getName();
        switch (methodName) {
          case "finalize":
            return null;
          case "toString":
            return null;
          default:
            break;
        }
        stringPath.add(methodName);
        methodPath.add(arg1);

        setter = isSetter(arg1);
        getter = isGetter(arg1);
        parameterTypes = arg1.getParameterTypes();
        returnType = arg1.getReturnType();

        field = discoverField(arg1);
        if (field != null) {
          fieldPath.add(field.getName());
        }

        Class<?> returnType = arg1.getReturnType();
        if (returnType.equals(Void.TYPE)) {
          return null;
        } else if (isSetter()) {
          return null;
        } else {
          return callBack(arg1.getReturnType());
        }
      }
    });
  }

  public static Logger getLog() {
    return log;
  }

  public Class<T> getRoot() {
    return root;
  }

  public Class<?>[] getParameterTypes() {
    return parameterTypes;
  }

  public Class<?> getReturnType() {
    return returnType;
  }

  public boolean isSetter() {
    return setter;
  }

  public boolean isGetter() {
    return getter;
  }

  public boolean isFieldAvailable() {
    return stringPath.size() == fieldPath.size();
  }

  public void setValue(T source, Object value) {
    if (!isSetter()) {
      log.error("Declared path [" + this + "]is no setter");
    }
    Object instance = source;
    Method lastMethod = methodPath.get(methodPath.size() - 1);
    for (Method method : methodPath) {
      if (method.equals(lastMethod)) {
        break;
      }
      try {
        instance = method.invoke(instance);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        log.error("Could not follow path " + this + ": ", e);
        return;
      }
    }
    try {
      lastMethod.invoke(instance, value);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      log.error("Could not invoke setter on path " + this + ": ", e);
    }
  }

  @SuppressWarnings("unchecked")
  public <U> U getValue(T source) {
    if (!isGetter()) {
      log.error("Declared path [" + this + "]is no getter");
      return null;
    }
    Object instance = source;
    for (Method method : methodPath) {
      if (instance == null) {
        return null;
      }
      try {
        instance = method.invoke(instance);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        log.error("Could not follow path " + this + ": ", e);
        return null;
      }
    }
    return (U) instance;
  }

  public void walk(T source) {
    Object instance = source;
    for (Method method : methodPath) {
      if (instance == null) {
        return;
      }
      if (method.getParameterTypes().length > 0) {
        return;
      }
      try {
        instance = method.invoke(instance);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        log.error("Could not follow path " + this + ": ", e);
        return;
      }
    }
  }

  protected boolean isGetter(Method method) {
    if (method.getParameterTypes().length > 0) {
      return false;
    }

    String name = method.getName();
    Class<?> returnType = method.getReturnType();

    if (name.startsWith("is")) {
      if (Boolean.TYPE.equals(returnType) || (Boolean.class.equals(returnType))) {
        return true;
      }
    }
    if (name.startsWith("get") && !Void.TYPE.equals(returnType)) {
      return true;
    }
    return false;
  }

  protected boolean isSetter(Method method) {
    if (method.getParameterTypes().length != 1) {
      return false;
    }

    if (method.getName().startsWith("set")) {
      return true;
    } else {
      return false;
    }
  }

  protected Field discoverField(Method method) {
    String methodName = method.getName();
    if (methodName.startsWith("get") || methodName.startsWith("set")) {
      methodName = methodName.substring(3);
    } else if (methodName.startsWith("is")) {
      methodName = methodName.substring(2);
    }
    methodName = methodName.toLowerCase();
    List<Field> fieldsRecursive = ReflectionUtil.getAllFields(method.getDeclaringClass());
    for (Field field : fieldsRecursive) {
      if (field.getName().toLowerCase().equals(methodName)) {
        return field;
      }
    }
    return null;
  }

  public String getStringFieldPath() {
    StringBuilder builder = new StringBuilder();
    builder.append(root.getSimpleName()).append(".");
    for (String fieldName : fieldPath) {
      builder.append(fieldName).append(".");
    }
    String path = builder.toString();
    return path.substring(0, path.length() - 1);
  }

  public String getPropertyPath() {
    StringBuilder builder = new StringBuilder();
    for (String fieldName : fieldPath) {
      builder.append(fieldName).append(".");
    }
    String path = builder.toString();
    return path.substring(0, path.length() - 1);
  }

  public Method getLastMethod() {
    return methodPath.get(methodPath.size() - 1);
  }

  public String getLastMethodName() {
    return methodPath.get(methodPath.size() - 1).getName();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((methodPath == null) ? 0 : methodPath.hashCode());
    result = (prime * result) + ((root == null) ? 0 : root.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof PropertyPath)) {
      return false;
    }
    PropertyPath<?> other = (PropertyPath<?>) obj;
    if (methodPath == null) {
      if (other.methodPath != null) {
        return false;
      }
    } else if (!methodPath.equals(other.methodPath)) {
      return false;
    }
    if (root == null) {
      if (other.root != null) {
        return false;
      }
    } else if (!root.equals(other.root)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(root.getSimpleName()).append(".");
    for (String methodName : stringPath) {
      builder.append(methodName).append("().");
    }

    String path = builder.toString();
    return path.substring(0, path.length() - 3);
  }

}