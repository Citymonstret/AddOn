package com.intellectualsites.addon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sun.misc.JarFilter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AddOnManager {

    private Map<String, Class> globalClassMap = new ConcurrentHashMap<>();
    private Map<String, AddOnClassLoader> classLoaders = new ConcurrentHashMap<>();

    @Getter
    private final File addOnFolder;

    public int load() throws AddOnManagerException {
        if (!addOnFolder.exists()) {
            if (!addOnFolder.mkdir()) {
                throw new AddOnManagerException("Couldn't create AddOn folder");
            }
        }
        int loaded = 0;
        final File[] files = addOnFolder.listFiles(new JarFilter());
        for (final File file : files) {
            Properties properties;
            try {
                properties = getAddOnProperties(file);
            } catch (final Exception e) {
                e.printStackTrace();
                continue;
            }
            if (!properties.containsKey("main")) {
                new AddOnManagerException("\"addon.properties\" for " + file.getName() + " has no \"main\" key")
                        .printStackTrace();
                continue;
            }
            if (!properties.containsKey("name")) {
                new AddOnManagerException("\"addon.properties\" for " + file.getName() + " has no \"name\" key")
                        .printStackTrace();
                continue;
            }
            AddOnClassLoader loader;
            try {
                loader = new AddOnClassLoader(this, file, properties.get("main").toString(),
                        properties.get("name").toString());
            } catch (final Exception e) {
                new AddOnManagerException("Failed to load " + file.getName(), e)
                        .printStackTrace();
                continue;
            }
            this.classLoaders.put(properties.get("name").toString(), loader);
            loaded++;
        }
        return loaded;
    }

    private Properties getAddOnProperties(final File file) throws AddOnManagerException {
        JarFile jar;
        try {
            jar = new JarFile(file);
        } catch (final IOException e) {
            throw new AddOnManagerException("Failed to create jar object from " + file.getName(), e);
        }
        final JarEntry desc = jar.getJarEntry("addon.properties");
        if (desc == null) {
            throw new AddOnManagerException("There is no \"addon.properties\" for addon: " + file.getName());
        }
        final Properties properties = new Properties();
        try (InputStream stream = jar.getInputStream(desc)) {
            properties.load(stream);
        } catch (final Exception e) {
            throw new AddOnManagerException("Failed to load \"addon.properties\" in " + file.getName());
        }
        return properties;
    }

    void setClass(String name, Class<?> clazz) {
        globalClassMap.put(name, clazz);
    }

    Class<?> findClass(String name) {
        if (globalClassMap.containsKey(name)) {
            return globalClassMap.get(name);
        }
        Class clazz;
        for (final Map.Entry<String, AddOnClassLoader> loader : classLoaders.entrySet()) {
            try {
                if ((clazz = loader.getValue().findClass(name, false)) != null) {
                    return clazz;
                }
            } catch (final Exception e) {
                new AddOnManagerException("Failed to find class " + name, e)
                        .printStackTrace();
            }
        }
        return null;
    }

    private static class AddOnManagerException extends RuntimeException {

        private AddOnManagerException(String error) {
            super (error);
        }

        private AddOnManagerException(String error, Throwable cause) {
            super (error, cause);
        }

    }

    public void enableAddOns() {
        classLoaders.values().stream().filter(loader -> loader.getAddOn() != null)
                .forEach(loader -> loader.getAddOn().enable());
    }

    public void disableAddons() {
        classLoaders.values().stream().filter(loader -> loader.getAddOn() != null)
                .forEach(loader -> loader.getAddOn().disable());
    }
}
