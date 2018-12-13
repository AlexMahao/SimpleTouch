package com.spearbothy.touch.core;

import com.android.dx.Code;
import com.android.dx.Comparison;
import com.android.dx.DexMaker;
import com.android.dx.FieldId;
import com.android.dx.Label;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.STATIC;

/**
 * @author mahao 2018/11/13 下午1:48
 */

public final class ViewProxyBuilder<T> {
    private static final String FIELD_NAME_HANDLER = "$__handler";
    private static final String FIELD_NAME_METHODS = "$__methodArray";

    /**
     * A cache of all proxy classes ever generated. At the time of writing,
     * Android's runtime doesn't support class unloading so there's little
     * value in using weak references.
     */
    private static final Map<Class<?>, Class<?>> generatedProxyClasses
            = Collections.synchronizedMap(new HashMap<Class<?>, Class<?>>());
    private List<String> proxyMethods = new ArrayList<>();
    private final Class<T> baseClass;
    private ClassLoader parentClassLoader = ViewProxyBuilder.class.getClassLoader();
    private InvocationHandler handler;
    private File dexCache;
    private Class<?>[] constructorArgTypes = new Class[0];
    private Object[] constructorArgValues = new Object[0];
    private Set<Class<?>> interfaces = new HashSet<Class<?>>();

    private ViewProxyBuilder(Class<T> clazz) {
        baseClass = clazz;
    }

    public static <T> ViewProxyBuilder<T> forClass(Class<T> clazz) {
        return new ViewProxyBuilder<T>(clazz);
    }

    /**
     * Specifies the parent ClassLoader to use when creating the proxy.
     * <p>
     * <p>If null, {@code ProxyBuilder.class.getClassLoader()} will be used.
     */
    public ViewProxyBuilder<T> parentClassLoader(ClassLoader parent) {
        parentClassLoader = parent;
        return this;
    }

    public ViewProxyBuilder<T> handler(InvocationHandler handler) {
        this.handler = handler;
        return this;
    }

    public ViewProxyBuilder<T> addProxyMethod(String method) {
        proxyMethods.add(method);
        return this;
    }

    public ViewProxyBuilder<T> addProxyMethod(List<String> methods) {
        proxyMethods.addAll(methods);
        return this;
    }

    /**
     * Sets the directory where executable code is stored. See {@link
     * DexMaker#generateAndLoad DexMaker.generateAndLoad()} for guidance on
     * choosing a secure location for the dex cache.
     */
    public ViewProxyBuilder<T> dexCache(File dexCache) {
        this.dexCache = dexCache;
        return this;
    }

    public ViewProxyBuilder<T> implementing(Class<?>... interfaces) {
        for (Class<?> i : interfaces) {
            if (!i.isInterface()) {
                throw new IllegalArgumentException("Not an interface: " + i.getName());
            }
            this.interfaces.add(i);
        }
        return this;
    }

    public ViewProxyBuilder<T> constructorArgValues(Object... constructorArgValues) {
        this.constructorArgValues = constructorArgValues;
        return this;
    }

    public ViewProxyBuilder<T> constructorArgTypes(Class<?>... constructorArgTypes) {
        this.constructorArgTypes = constructorArgTypes;
        return this;
    }

    /**
     * Create a new instance of the class to proxy.
     *
     * @throws UnsupportedOperationException if the class we are trying to create a proxy for is
     *                                       not accessible.
     * @throws IOException                   if an exception occurred writing to the {@code dexCache} directory.
     * @throws UndeclaredThrowableException  if the constructor for the base class to proxy throws
     *                                       a declared exception during construction.
     * @throws IllegalArgumentException      if the handler is null, if the constructor argument types
     *                                       do not match the constructor argument values, or if no such constructor exists.
     */
    public T build() throws IOException {
        check(handler != null, "handler == null");
        check(constructorArgTypes.length == constructorArgValues.length,
                "constructorArgValues.length != constructorArgTypes.length");
        Class<? extends T> proxyClass = buildProxyClass();
        Constructor<? extends T> constructor;
        try {
            constructor = proxyClass.getConstructor(constructorArgTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No constructor for " + baseClass.getName()
                    + " with parameter types " + Arrays.toString(constructorArgTypes));
        }
        T result;
        try {
            result = constructor.newInstance(constructorArgValues);
        } catch (InstantiationException e) {
            // Should not be thrown, generated class is not abstract.
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            // Should not be thrown, the generated constructor is accessible.
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            // Thrown when the base class constructor throws an exception.
            throw launderCause(e);
        }
        setInvocationHandler(result, handler);
        return result;
    }

    // TODO: test coverage for this

    /**
     * Generate a proxy class. Note that new instances of this class will not automatically have an
     * an invocation handler, even if {@link #handler(InvocationHandler)} was called. The handler
     * must be set on each instance after it is created, using
     * {@link #setInvocationHandler(Object, InvocationHandler)}.
     */
    public Class<? extends T> buildProxyClass() throws IOException {
        // try the cache to see if we've generated this one before
        @SuppressWarnings("unchecked") // we only populate the map with matching types
                Class<? extends T> proxyClass = (Class) generatedProxyClasses.get(baseClass);
        if (proxyClass != null
                && proxyClass.getClassLoader().getParent() == parentClassLoader
                && interfaces.equals(asSet(proxyClass.getInterfaces()))) {
            return proxyClass; // cache hit!
        }

        // the cache missed; generate the class
        DexMaker dexMaker = new DexMaker();
        String generatedName = getMethodNameForProxyOf(baseClass);
        TypeId<? extends T> generatedType = TypeId.get("L" + generatedName + ";");
        TypeId<T> superType = TypeId.get(baseClass);
        generateConstructorsAndFields(dexMaker, generatedType, superType, baseClass);

        Method[] allMethods = getMethodsToProxyRecursive();
        Method[] methodsToProxy = filtrate(allMethods);
        generateCodeForAllMethods(dexMaker, generatedType, methodsToProxy, superType);
        dexMaker.declare(generatedType, generatedName + ".generated", PUBLIC, superType,
                getInterfacesAsTypeIds());
        ClassLoader classLoader = dexMaker.generateAndLoad(parentClassLoader, dexCache);
        try {
            proxyClass = loadClass(classLoader, generatedName);
        } catch (IllegalAccessError e) {
            // Thrown when the base class is not accessible.
            throw new UnsupportedOperationException(
                    "cannot proxy inaccessible class " + baseClass, e);
        } catch (ClassNotFoundException e) {
            // Should not be thrown, we're sure to have generated this class.
            throw new AssertionError(e);
        }
        setMethodsStaticField(proxyClass, methodsToProxy);
        generatedProxyClasses.put(baseClass, proxyClass);
        return proxyClass;
    }

    private Method[] filtrate(Method[] allMethods) {
        ArrayList<Method> methods = new ArrayList<>();
        for (int i = 0; i < allMethods.length; i++) {
            Method method = allMethods[i];
            if (proxyMethods.contains(method.getName())) {
                methods.add(method);
            }
        }
        return methods.toArray(new Method[methods.size()]);
    }

    // The type cast is safe: the generated type will extend the base class type.
    @SuppressWarnings("unchecked")
    private Class<? extends T> loadClass(ClassLoader classLoader, String generatedName)
            throws ClassNotFoundException {
        return (Class<? extends T>) classLoader.loadClass(generatedName);
    }

    private static RuntimeException launderCause(InvocationTargetException e) {
        Throwable cause = e.getCause();
        // Errors should be thrown as they are.
        if (cause instanceof Error) {
            throw (Error) cause;
        }
        // RuntimeException can be thrown as-is.
        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }
        // Declared exceptions will have to be wrapped.
        throw new UndeclaredThrowableException(cause);
    }

    private static void setMethodsStaticField(Class<?> proxyClass, Method[] methodsToProxy) {
        try {
            Field methodArrayField = proxyClass.getDeclaredField(FIELD_NAME_METHODS);
            methodArrayField.setAccessible(true);
            methodArrayField.set(null, methodsToProxy);
        } catch (NoSuchFieldException e) {
            // Should not be thrown, generated proxy class has been generated with this field.
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            // Should not be thrown, we just set the field to accessible.
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the proxy's {@link InvocationHandler}.
     *
     * @throws IllegalArgumentException if the object supplied is not a proxy created by this class.
     */
    public static InvocationHandler getInvocationHandler(Object instance) {
        try {
            Field field = instance.getClass().getDeclaredField(FIELD_NAME_HANDLER);
            field.setAccessible(true);
            return (InvocationHandler) field.get(instance);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Not a valid proxy instance", e);
        } catch (IllegalAccessException e) {
            // Should not be thrown, we just set the field to accessible.
            throw new AssertionError(e);
        }
    }

    /**
     * Sets the proxy's {@link InvocationHandler}.
     * <p>
     * If you create a proxy with {@link #build()}, the proxy will already have a handler set,
     * provided that you configured one with {@link #handler(InvocationHandler)}.
     * <p>
     * If you generate a proxy class with {@link #buildProxyClass()}, instances of the proxy class
     * will not automatically have a handler set, and it is necessary to use this method with each
     * instance.
     *
     * @throws IllegalArgumentException if the object supplied is not a proxy created by this class.
     */
    public static void setInvocationHandler(Object instance, InvocationHandler handler) {
        try {
            Field handlerField = instance.getClass().getDeclaredField(FIELD_NAME_HANDLER);
            handlerField.setAccessible(true);
            handlerField.set(instance, handler);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Not a valid proxy instance", e);
        } catch (IllegalAccessException e) {
            // Should not be thrown, we just set the field to accessible.
            throw new AssertionError(e);
        }
    }

    // TODO: test coverage for isProxyClass

    /**
     * Returns true if {@code c} is a proxy class created by this builder.
     */
    public static boolean isProxyClass(Class<?> c) {
        // TODO: use a marker interface instead?
        try {
            c.getDeclaredField(FIELD_NAME_HANDLER);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private static <T, G extends T> void generateCodeForAllMethods(DexMaker dexMaker,
                                                                   TypeId<G> generatedType, Method[] methodsToProxy, TypeId<T> superclassType) {
        TypeId<InvocationHandler> handlerType = TypeId.get(InvocationHandler.class);
        TypeId<Method[]> methodArrayType = TypeId.get(Method[].class);
        FieldId<G, InvocationHandler> handlerField =
                generatedType.getField(handlerType, FIELD_NAME_HANDLER);
        FieldId<G, Method[]> allMethods =
                generatedType.getField(methodArrayType, FIELD_NAME_METHODS);
        TypeId<Method> methodType = TypeId.get(Method.class);
        TypeId<Object[]> objectArrayType = TypeId.get(Object[].class);
        MethodId<InvocationHandler, Object> methodInvoke = handlerType.getMethod(TypeId.OBJECT,
                "invoke", TypeId.OBJECT, methodType, objectArrayType);
        for (int m = 0; m < methodsToProxy.length; ++m) {
            /*
             * If the 5th method on the superclass Example that can be overridden were to look like
             * this:
             *
             *     public int doSomething(Bar param0, int param1) {
             *         ...
             *     }
             *
             * Then the following code will generate a method on the proxy that looks something
             * like this:
             *
             *     public int doSomething(Bar param0, int param1) {
             *         int methodIndex = 4;
             *         Method[] allMethods = Example_Proxy.$__methodArray;
             *         Method thisMethod = allMethods[methodIndex];
             *         int argsLength = 2;
             *         Object[] args = new Object[argsLength];
             *         InvocationHandler localHandler = this.$__handler;
             *         // for-loop begins
             *         int p = 0;
             *         Bar parameter0 = param0;
             *         args[p] = parameter0;
             *         p = 1;
             *         int parameter1 = param1;
             *         Integer boxed1 = Integer.valueOf(parameter1);
             *         args[p] = boxed1;
             *         // for-loop ends
             *         Object result = localHandler.invoke(this, thisMethod, args);
             *         Integer castResult = (Integer) result;
             *         int unboxedResult = castResult.intValue();
             *         return unboxedResult;
             *     }
             *
             * Or, in more idiomatic Java:
             *
             *     public int doSomething(Bar param0, int param1) {
             *         if ($__handler == null) {
             *             return super.doSomething(param0, param1);
             *         }
             *         return __handler.invoke(this, __methodArray[4],
             *                 new Object[] { param0, Integer.valueOf(param1) });
             *     }
             */
            Method method = methodsToProxy[m];
            String name = method.getName();
            Class<?>[] argClasses = method.getParameterTypes();
            TypeId<?>[] argTypes = new TypeId<?>[argClasses.length];
            for (int i = 0; i < argTypes.length; ++i) {
                argTypes[i] = TypeId.get(argClasses[i]);
            }
            Class<?> returnType = method.getReturnType();
            TypeId<?> resultType = TypeId.get(returnType);
            MethodId<T, ?> superMethod = superclassType.getMethod(resultType, name, argTypes);
            MethodId<?, ?> methodId = generatedType.getMethod(resultType, name, argTypes);
            Code code = dexMaker.declare(methodId, PUBLIC);
            Local<G> localThis = code.getThis(generatedType);
            Local<InvocationHandler> localHandler = code.newLocal(handlerType);
            Local<Object> invokeResult = code.newLocal(TypeId.OBJECT);
            Local<Integer> intValue = code.newLocal(TypeId.INT);
            Local<Object[]> args = code.newLocal(objectArrayType);
            Local<Integer> argsLength = code.newLocal(TypeId.INT);
            Local<Object> temp = code.newLocal(TypeId.OBJECT);
            Local<?> resultHolder = code.newLocal(resultType);
            Local<Method[]> methodArray = code.newLocal(methodArrayType);
            Local<Method> thisMethod = code.newLocal(methodType);
            Local<Integer> methodIndex = code.newLocal(TypeId.INT);
            Class<?> aBoxedClass = PRIMITIVE_TO_BOXED.get(returnType);
            Local<?> aBoxedResult = null;
            if (aBoxedClass != null) {
                aBoxedResult = code.newLocal(TypeId.get(aBoxedClass));
            }
            Local<?>[] superArgs2 = new Local<?>[argClasses.length];
            Local<?> superResult2 = code.newLocal(resultType);
            Local<InvocationHandler> nullHandler = code.newLocal(handlerType);

            code.loadConstant(methodIndex, m);
            code.sget(allMethods, methodArray);
            code.aget(thisMethod, methodArray, methodIndex);
            code.loadConstant(argsLength, argTypes.length);
            code.newArray(args, argsLength);
            code.iget(handlerField, localHandler, localThis);

            // if (proxy == null)
            code.loadConstant(nullHandler, null);
            Label handlerNullCase = new Label();
            code.compare(Comparison.EQ, handlerNullCase, nullHandler, localHandler);

            // This code is what we execute when we have a valid proxy: delegate to invocation
            // handler.
            for (int p = 0; p < argTypes.length; ++p) {
                code.loadConstant(intValue, p);
                Local<?> parameter = code.getParameter(p, argTypes[p]);
                Local<?> unboxedIfNecessary = boxIfRequired(code, parameter, temp);
                code.aput(args, intValue, unboxedIfNecessary);
            }
            code.invokeInterface(methodInvoke, invokeResult, localHandler,
                    localThis, thisMethod, args);
            generateCodeForReturnStatement(code, returnType, invokeResult, resultHolder,
                    aBoxedResult);

            // This code is executed if proxy is null: call the original super method.
            // This is required to handle the case of construction of an object which leaks the
            // "this" pointer.
            code.mark(handlerNullCase);
            for (int i = 0; i < superArgs2.length; ++i) {
                superArgs2[i] = code.getParameter(i, argTypes[i]);
            }
            if (void.class.equals(returnType)) {
                code.invokeSuper(superMethod, null, localThis, superArgs2);
                code.returnVoid();
            } else {
                invokeSuper(superMethod, code, localThis, superArgs2, superResult2);
                code.returnValue(superResult2);
            }

            /*
             * And to allow calling the original super method, the following is also generated:
             *
             *     public String super$doSomething$java_lang_String(Bar param0, int param1) {
             *          int result = super.doSomething(param0, param1);
             *          return result;
             *     }
             */
            // TODO: don't include a super_ method if the target is abstract!
            MethodId<G, ?> callsSuperMethod = generatedType.getMethod(
                    resultType, superMethodName(method), argTypes);
            Code superCode = dexMaker.declare(callsSuperMethod, PUBLIC);
            Local<G> superThis = superCode.getThis(generatedType);
            Local<?>[] superArgs = new Local<?>[argClasses.length];
            for (int i = 0; i < superArgs.length; ++i) {
                superArgs[i] = superCode.getParameter(i, argTypes[i]);
            }
            if (void.class.equals(returnType)) {
                superCode.invokeSuper(superMethod, null, superThis, superArgs);
                superCode.returnVoid();
            } else {
                Local<?> superResult = superCode.newLocal(resultType);
                invokeSuper(superMethod, superCode, superThis, superArgs, superResult);
                superCode.returnValue(superResult);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void invokeSuper(MethodId superMethod, Code superCode,
                                    Local superThis, Local[] superArgs, Local superResult) {
        superCode.invokeSuper(superMethod, superResult, superThis, superArgs);
    }

    private static Local<?> boxIfRequired(Code code, Local<?> parameter, Local<Object> temp) {
        MethodId<?, ?> unboxMethod = PRIMITIVE_TYPE_TO_UNBOX_METHOD.get(parameter.getType());
        if (unboxMethod == null) {
            return parameter;
        }
        code.invokeStatic(unboxMethod, temp, parameter);
        return temp;
    }

    public static Object callSuper(Object proxy, Method method, Object... args) throws Throwable {
        try {
            return proxy.getClass()
                    .getMethod(superMethodName(method), method.getParameterTypes())
                    .invoke(proxy, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * The super method must include the return type, otherwise its ambiguous
     * for methods with covariant return types.
     */
    private static String superMethodName(Method method) {
        String returnType = method.getReturnType().getName();
        return "super$" + method.getName() + "$"
                + returnType.replace('.', '_').replace('[', '_').replace(';', '_');
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private static <T, G extends T> void generateConstructorsAndFields(DexMaker dexMaker,
                                                                       TypeId<G> generatedType, TypeId<T> superType, Class<T> superClass) {
        TypeId<InvocationHandler> handlerType = TypeId.get(InvocationHandler.class);
        TypeId<Method[]> methodArrayType = TypeId.get(Method[].class);
        FieldId<G, InvocationHandler> handlerField = generatedType.getField(
                handlerType, FIELD_NAME_HANDLER);
        dexMaker.declare(handlerField, PRIVATE, null);
        FieldId<G, Method[]> allMethods = generatedType.getField(
                methodArrayType, FIELD_NAME_METHODS);
        dexMaker.declare(allMethods, PRIVATE | STATIC, null);
        for (Constructor<T> constructor : getConstructorsToOverwrite(superClass)) {
            if (constructor.getModifiers() == Modifier.FINAL) {
                continue;
            }
            TypeId<?>[] types = classArrayToTypeArray(constructor.getParameterTypes());
            MethodId<?, ?> method = generatedType.getConstructor(types);
            Code constructorCode = dexMaker.declare(method, PUBLIC);
            Local<G> thisRef = constructorCode.getThis(generatedType);
            Local<?>[] params = new Local[types.length];
            for (int i = 0; i < params.length; ++i) {
                params[i] = constructorCode.getParameter(i, types[i]);
            }
            MethodId<T, ?> superConstructor = superType.getConstructor(types);
            constructorCode.invokeDirect(superConstructor, null, thisRef, params);
            constructorCode.returnVoid();
        }
    }

    // The type parameter on Constructor is the class in which the constructor is declared.
    // The getDeclaredConstructors() method gets constructors declared only in the given class,
    // hence this cast is safe.
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T>[] getConstructorsToOverwrite(Class<T> clazz) {
        return (Constructor<T>[]) clazz.getDeclaredConstructors();
    }

    private TypeId<?>[] getInterfacesAsTypeIds() {
        TypeId<?>[] result = new TypeId<?>[interfaces.size()];
        int i = 0;
        for (Class<?> implemented : interfaces) {
            result[i++] = TypeId.get(implemented);
        }
        return result;
    }

    /**
     * Gets all {@link Method} objects we can proxy in the hierarchy of the
     * supplied class.
     */
    private Method[] getMethodsToProxyRecursive() {
        Set<MethodSetEntry> methodsToProxy = new HashSet<MethodSetEntry>();
        Set<MethodSetEntry> seenFinalMethods = new HashSet<MethodSetEntry>();
        for (Class<?> c = baseClass; c != null; c = c.getSuperclass()) {
            getMethodsToProxy(methodsToProxy, seenFinalMethods, c);
        }
        for (Class<?> c = baseClass; c != null; c = c.getSuperclass()) {
            for (Class<?> i : c.getInterfaces()) {
                getMethodsToProxy(methodsToProxy, seenFinalMethods, i);
            }
        }
        for (Class<?> c : interfaces) {
            getMethodsToProxy(methodsToProxy, seenFinalMethods, c);
        }

        Method[] results = new Method[methodsToProxy.size()];
        int i = 0;
        for (MethodSetEntry entry : methodsToProxy) {
            results[i++] = entry.originalMethod;
        }
        return results;
    }

    private void getMethodsToProxy(Set<MethodSetEntry> sink, Set<MethodSetEntry> seenFinalMethods,
                                   Class<?> c) {
        for (Method method : c.getDeclaredMethods()) {
            if ((method.getModifiers() & Modifier.FINAL) != 0) {
                // Skip final methods, we can't override them. We
                // also need to remember them, in case the same
                // method exists in a parent class.
                MethodSetEntry entry = new MethodSetEntry(method);
                seenFinalMethods.add(entry);
                // We may have seen this method already, from an interface
                // implemented by a child class. We need to remove it here.
                sink.remove(entry);
                continue;
            }
            if ((method.getModifiers() & STATIC) != 0) {
                // Skip static methods, overriding them has no effect.
                continue;
            }
            if (method.getName().equals("finalize") && method.getParameterTypes().length == 0) {
                // Skip finalize method, it's likely important that it execute as normal.
                continue;
            }
            MethodSetEntry entry = new MethodSetEntry(method);
            if (seenFinalMethods.contains(entry)) {
                // This method is final in a child class.
                // We can't override it.
                continue;
            }
            sink.add(entry);
        }
    }

    private static <T> String getMethodNameForProxyOf(Class<T> clazz) {
        return clazz.getSimpleName() + "_Proxy";
    }

    private static TypeId<?>[] classArrayToTypeArray(Class<?>[] input) {
        TypeId<?>[] result = new TypeId[input.length];
        for (int i = 0; i < input.length; ++i) {
            result[i] = TypeId.get(input[i]);
        }
        return result;
    }

    /**
     * Calculates the correct return statement code for a method.
     * <p>
     * A void method will not return anything.  A method that returns a primitive will need to
     * unbox the boxed result.  Otherwise we will cast the result.
     */
    // This one is tricky to fix, I gave up.
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void generateCodeForReturnStatement(Code code, Class methodReturnType,
                                                       Local localForResultOfInvoke, Local localOfMethodReturnType, Local aBoxedResult) {
        if (PRIMITIVE_TO_UNBOX_METHOD.containsKey(methodReturnType)) {
            code.cast(aBoxedResult, localForResultOfInvoke);
            MethodId unboxingMethodFor = getUnboxMethodForPrimitive(methodReturnType);
            code.invokeVirtual(unboxingMethodFor, localOfMethodReturnType, aBoxedResult);
            code.returnValue(localOfMethodReturnType);
        } else if (void.class.equals(methodReturnType)) {
            code.returnVoid();
        } else {
            code.cast(localOfMethodReturnType, localForResultOfInvoke);
            code.returnValue(localOfMethodReturnType);
        }
    }

    private static <T> Set<T> asSet(T... array) {
        return new CopyOnWriteArraySet<T>(Arrays.asList(array));
    }

    private static MethodId<?, ?> getUnboxMethodForPrimitive(Class<?> methodReturnType) {
        return PRIMITIVE_TO_UNBOX_METHOD.get(methodReturnType);
    }

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_BOXED;

    static {
        PRIMITIVE_TO_BOXED = new HashMap<Class<?>, Class<?>>();
        PRIMITIVE_TO_BOXED.put(boolean.class, Boolean.class);
        PRIMITIVE_TO_BOXED.put(int.class, Integer.class);
        PRIMITIVE_TO_BOXED.put(byte.class, Byte.class);
        PRIMITIVE_TO_BOXED.put(long.class, Long.class);
        PRIMITIVE_TO_BOXED.put(short.class, Short.class);
        PRIMITIVE_TO_BOXED.put(float.class, Float.class);
        PRIMITIVE_TO_BOXED.put(double.class, Double.class);
        PRIMITIVE_TO_BOXED.put(char.class, Character.class);
    }

    private static final Map<TypeId<?>, MethodId<?, ?>> PRIMITIVE_TYPE_TO_UNBOX_METHOD;

    static {
        PRIMITIVE_TYPE_TO_UNBOX_METHOD = new HashMap<TypeId<?>, MethodId<?, ?>>();
        for (Map.Entry<Class<?>, Class<?>> entry : PRIMITIVE_TO_BOXED.entrySet()) {
            TypeId<?> primitiveType = TypeId.get(entry.getKey());
            TypeId<?> boxedType = TypeId.get(entry.getValue());
            MethodId<?, ?> valueOfMethod = boxedType.getMethod(boxedType, "valueOf", primitiveType);
            PRIMITIVE_TYPE_TO_UNBOX_METHOD.put(primitiveType, valueOfMethod);
        }
    }

    /**
     * Map from primitive type to method used to unbox a boxed version of the primitive.
     * <p>
     * This is required for methods whose return type is primitive, since the
     * {@link InvocationHandler} will return us a boxed result, and we'll need to convert it back to
     * primitive value.
     */
    private static final Map<Class<?>, MethodId<?, ?>> PRIMITIVE_TO_UNBOX_METHOD;

    static {
        Map<Class<?>, MethodId<?, ?>> map = new HashMap<Class<?>, MethodId<?, ?>>();
        map.put(boolean.class, TypeId.get(Boolean.class).getMethod(TypeId.BOOLEAN, "booleanValue"));
        map.put(int.class, TypeId.get(Integer.class).getMethod(TypeId.INT, "intValue"));
        map.put(byte.class, TypeId.get(Byte.class).getMethod(TypeId.BYTE, "byteValue"));
        map.put(long.class, TypeId.get(Long.class).getMethod(TypeId.LONG, "longValue"));
        map.put(short.class, TypeId.get(Short.class).getMethod(TypeId.SHORT, "shortValue"));
        map.put(float.class, TypeId.get(Float.class).getMethod(TypeId.FLOAT, "floatValue"));
        map.put(double.class, TypeId.get(Double.class).getMethod(TypeId.DOUBLE, "doubleValue"));
        map.put(char.class, TypeId.get(Character.class).getMethod(TypeId.CHAR, "charValue"));
        PRIMITIVE_TO_UNBOX_METHOD = map;
    }

    /**
     * Wrapper class to let us disambiguate {@link Method} objects.
     * <p>
     * The purpose of this class is to override the {@link #equals(Object)} and {@link #hashCode()}
     * methods so we can use a {@link Set} to remove duplicate methods that are overrides of one
     * another. For these purposes, we consider two methods to be equal if they have the same
     * name, return type, and parameter types.
     */
    private static class MethodSetEntry {
        private final String name;
        private final Class<?>[] paramTypes;
        private final Class<?> returnType;
        private final Method originalMethod;

        public MethodSetEntry(Method method) {
            originalMethod = method;
            name = method.getName();
            paramTypes = method.getParameterTypes();
            returnType = method.getReturnType();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof MethodSetEntry) {
                MethodSetEntry other = (MethodSetEntry) o;
                return name.equals(other.name)
                        && returnType.equals(other.returnType)
                        && Arrays.equals(paramTypes, other.paramTypes);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result += 31 * result + name.hashCode();
            result += 31 * result + returnType.hashCode();
            result += 31 * result + Arrays.hashCode(paramTypes);
            return result;
        }
    }
}
