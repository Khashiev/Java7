package edu.school21.reflection.app;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.StringJoiner;

public class ClassesWorker {
    Scanner scanner;
    Class<?> aClass;
    Object object;

    public ClassesWorker() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        try {
            printClassInfo();
            createObject();
            updateObject();
            callMethod();
            scanner.close();
        } catch (IllegalArgumentException | IllegalAccessException
                 | InstantiationException | NoSuchFieldException
                 | InvocationTargetException e) {
            System.out.println(e.getMessage());
        }
    }

    private void printClassInfo() {
        Set<Class<?>> classes = getClasses();

        System.out.println("Enter class name:\n-> ");
        String className = scanner.nextLine();
        System.out.println("---------------------");

        for (Class<?> aClass : classes) {
            if (aClass.getSimpleName().equals(className)) {
                this.aClass = aClass;
                printFields();
                printMethods();
                return;
            }
        }

        throw new IllegalStateException("Class not found: " + className);
    }

    private Set<Class<?>> getClasses() {
        Reflections reflections = new Reflections("edu.school21.reflection.classes",
                new SubTypesScanner(false));

        System.out.println("Classes:");
        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);

        for (Class<?> aClass : classes) {
            System.out.println("\t - " + aClass.getSimpleName());
        }
        System.out.println("---------------------");

        return classes;
    }

    private void printFields() {
        System.out.println("fields:");

        Field[] fields = aClass.getDeclaredFields();

        for (Field field : fields) {
            System.out.println("\t\t" + field.getType().getSimpleName()
                    + " " + field.getName());
        }
    }

    private void printMethods() {
        System.out.println("methods:");
        Method[] methods = aClass.getDeclaredMethods();

        for (Method method : methods) {
            if (method.getName().equals("toString")) {
                continue;
            }

            StringJoiner joiner = new StringJoiner(", ", "\t\t"
                    + method.getReturnType().getSimpleName()
                    + " " + method.getName() + '(', ")");

            Class<?>[] parameterTypes = method.getParameterTypes();

            for (Class<?> parameterType : parameterTypes) {
                joiner.add(parameterType.getSimpleName());
            }

            System.out.println(joiner);
        }

        System.out.println("---------------------");
    }

    private void createObject() throws InstantiationException, IllegalAccessException {
        System.out.println("Let’s create an object.");
        object = aClass.newInstance();

        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            System.out.println(field.getName() + ":\n-> ");
            field.setAccessible(true);
            field.set(object, getValue(field.getType()));
        }

        System.out.println("Object created: " + object);
        System.out.println("---------------------");
    }

    private Object getValue(Object object) throws IllegalAccessException {
        try {
            if (object.equals(int.class)) {
                return Integer.parseInt(scanner.nextLine());
            } else if (object.equals(String.class)) {
                return scanner.nextLine();
            } else if (object.equals(Double.class)) {
                return Double.parseDouble(scanner.nextLine());
            } else if (object.equals(Boolean.class)) {
                return Boolean.parseBoolean(scanner.nextLine());
            } else if (object.equals(Long.class)) {
                return Long.parseLong(scanner.nextLine());
            }
        } catch (NumberFormatException ignored) {
        }
        throw new IllegalAccessException();
    }

    private void updateObject()
            throws IllegalAccessException, NoSuchFieldException {
        System.out.println("Enter name of the field for changing:\n-> ");
        Field field = object.getClass().getDeclaredField(scanner.nextLine());
        System.out.println("Enter " + field.getType().getSimpleName() + " value:\n-> ");
        field.setAccessible(true);
        field.set(object, getValue(field.getType()));
        System.out.println("Object updated: " + object);
        System.out.println("---------------------");
    }

    private void callMethod()
            throws IllegalAccessException, InvocationTargetException {
        System.out.println("Enter name of the method for call:\n-> ");
        Method[] methods = object.getClass().getDeclaredMethods();
        String methodName = scanner.nextLine();
        for (Method method : methods) {
            StringJoiner joiner = new StringJoiner(", ", method.getName() + '(', ")");
            Class<?>[] parameterTypes = method.getParameterTypes();

            for (Class<?> parameterType : parameterTypes) {
                joiner.add(parameterType.getSimpleName());
            }

            if (methodName.equals(joiner.toString())) {
                invokeMethod(method, parameterTypes);
                return;
            }
        }
        throw new IllegalAccessException("Method not found: " + methodName);
    }

    private void invokeMethod(Method method, Class<?>[] parameterTypes)
            throws IllegalAccessException, InvocationTargetException {
        ArrayList<Object> arguments = new ArrayList<>();

        for (Class<?> parameterType : parameterTypes) {
            System.out.println("Enter " + parameterType.getSimpleName() + " value:\n-> ");
            arguments.add(getValue(parameterType));
        }

        if (method.getReturnType().equals(void.class)) {
            method.invoke(object, arguments.toArray());
        } else {
            System.out.println("Method returned:\n" + method.invoke(object, arguments.toArray()));
        }
    }
}
