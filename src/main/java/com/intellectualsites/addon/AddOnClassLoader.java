package com.intellectualsites.addon;

import lombok.Getter;
import lombok.NonNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AddOnClassLoader extends URLClassLoader {

    private final AddOnManager addOnManager;
    private final Map<String, Class> classes;

    @Getter
    private final AddOn addOn;

    AddOnClassLoader(@NonNull final AddOnManager addOnManager, @NonNull final File file,
                     @NonNull final String mainFile, @NonNull final String name) throws AddOnLoaderException, MalformedURLException {
        super (new URL[] { file.toURI().toURL() }, addOnManager.getClass().getClassLoader());
        this.addOnManager = addOnManager;
        this.classes = new HashMap<>();

        Class mainClass;
        try {
            mainClass = Class.forName(mainFile, true, this);
        } catch (final ClassNotFoundException e) {
            throw new AddOnLoaderException("Could not find main class for addOn " + name);
        }
        Class<? extends AddOn> addOnMain;
        try {
            addOnMain = mainClass.asSubclass(AddOn.class);
        } catch (final Exception e) {
            throw new AddOnLoaderException(mainFile + " does not implement AddOn");
        }
        try {
            this.addOn = addOnMain.newInstance();
        } catch (final Exception e) {
            throw new AddOnLoaderException("Failed to load main class for " + name, e);
        }
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    Class<?> findClass(final String name, final boolean global) throws ClassNotFoundException {
        if (classes.containsKey(name)) {
            return classes.get(name);
        } else {
            Class<?> clazz = null;
            if (global) {
                clazz = addOnManager.findClass(name);
            }
            if (clazz == null) {
                clazz = super.findClass(name);
                if (clazz != null) {
                    addOnManager.setClass(name, clazz);
                }
            }
            return clazz;
        }
    }

    protected Collection<Class> getClasses() {
        return this.classes.values();
    }

    private static class AddOnLoaderException extends RuntimeException {

        private AddOnLoaderException(String error) {
            super (error);
        }

        private AddOnLoaderException(String error, Throwable cause) {
            super (error, cause);
        }

    }

}
