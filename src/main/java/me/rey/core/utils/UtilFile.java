package me.rey.core.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import me.rey.core.Warriors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.Map;
import java.util.Objects;

public class UtilFile {

    public static boolean isArchive(final File file) {
        long signature = 0;
        try (final RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            signature = raf.readLong();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return signature == 5785721462170058752L;
    }

    public static File getFirstFileStartsWith(final String start, final File parent, final boolean ignoreCase) {
        Validate.notNull(start, "start cannot be null");
        Validate.notNull(parent, "parent file cannot be null");
        Validate.isTrue(parent.isDirectory(), "parent must be a directory");
        for (final File file : Objects.requireNonNull(parent.listFiles())) {
            if (ignoreCase) {
                if (file.getName().toLowerCase().startsWith(start.toLowerCase())) {
                    return file;
                }
            } else {
                if (file.getName().startsWith(start)) {
                    return file;
                }
            }
        }
        return null;
    }

    public static File getFolder(final String name, final File parent) {
        final File file = new File(parent, name);
        if (!file.exists()) {
            Text.log("Unable to find '" + file.getName() + "', creating a new one!");
            if (!file.mkdirs()) {
                Text.log("There was an unknown error whilst trying to create '" + file.getName() + "'. If the file has been created in " + file.getParentFile().getPath() + ", you can ignore this. If it hasn't and restarting your server does not fix this, please contact the plugin developer via DM.");
            } else {
                Text.log("Created '" + file.getName() + "' successfully!");
            }
        }
        return file;
    }

    public static File getYamlFile(final String name, final File parent, final Map<String, Object> defaults) {
        if (name.toLowerCase().endsWith(".yml")) {
            final File file = new File(parent, name);
            if (!file.exists()) {
                try {
                    Text.log("Unable to find '" + file.getName() + "', creating a new one!");
                    if (!file.createNewFile()) {
                        Text.log("There was an unknown error whilst trying to create '" + file.getName() + "'. If the file has been created in " + file.getParentFile().getPath() + ", you can ignore this. If it hasn't and restarting your server does not fix this, please contact the plugin developer via DM.");
                    } else {
                        Text.log("Created '" + file.getName() + "' successfully!");
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }

                final YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
                for (final Map.Entry<String, Object> entry : defaults.entrySet()) {
                    yml.set(entry.getKey(), entry.getValue());
                }

                try {
                    yml.save(file);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            } else {
                final YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
                for (final Map.Entry<String, Object> entry : defaults.entrySet()) {
                    if (yml.get(entry.getKey()) == null) {
                        yml.set(entry.getKey(), entry.getValue());
                    }
                }

                try {
                    yml.save(file);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            return file;
        } else {
            throw new IllegalStateException("file must be a yml");
        }
    }

    public static File getJsonFile(final String name, final File parent, final InputStream resource) {
        if (name.toLowerCase().endsWith(".json")) {
            final File file = new File(parent, name);
            if (!file.exists()) {
                try {
                    Text.log("Unable to find '" + file.getName() + "', creating a new one!");
                    if (!file.createNewFile()) {
                        Text.log("There was an unknown error whilst trying to create '" + file.getName() + "'. If the file has been created in " + file.getParentFile().getPath() + ", you can ignore this. If it hasn't and restarting your server does not fix this, please contact the plugin developer via DM.");
                    } else {
                        Text.log("Created '" + file.getName() + "' successfully!");
                    }

                    final OutputStream outputStream = new FileOutputStream(file);
                    IOUtils.copy(resource, outputStream);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            return file;
        } else {
            throw new IllegalStateException("file must be a json");
        }
    }

    public static File getJsonFile(final String name, final File parent) {
        return getJsonFile(name, parent, (JsonElement) null);
    }

    public static File getJsonFile(final String name, final File parent, final JsonElement json) {
        if (name.toLowerCase().endsWith(".json")) {
            final File file = new File(parent, name);
            if (!file.exists()) {
                try {
                    Text.log("Unable to find '" + file.getName() + "', creating a new one!");
                    if (!file.createNewFile()) {
                        Text.log("There was an unknown error whilst trying to create '" + file.getName() + "'. If the file has been created in " + file.getParentFile().getPath() + ", you can ignore this. If it hasn't and restarting your server does not fix this, please contact the plugin developer via DM.");
                    } else {
                        Text.log("Created '" + file.getName() + "' successfully!");
                    }

                    if (json != null) {
                        final FileWriter writer = new FileWriter(file);
                        writer.write(new Gson().toJson(json));
                        writer.close();
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            return file;
        } else {
            throw new IllegalStateException("file must be a json");
        }
    }

    public static File getIndiscriminatoryFile(final String name, final File parent) {
        final File file = new File(parent, name);
        if (!file.exists()) {
            try {
                Text.log("Unable to find '" + file.getName() + "', creating a new one!");
                if (!file.createNewFile()) {
                    Text.log("There was an unknown error whilst trying to create '" + file.getName() + "'. If the file has been created in " + file.getParentFile().getPath() + ", you can ignore this. If it hasn't and restarting your server does not fix this, please contact the plugin developer via DM.");
                } else {
                    Text.log("Created '" + file.getName() + "' successfully!");
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static void deleteRecursively(final File file) {
        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files != null) {
                for (final File childFile : files) {
                    if (childFile.isDirectory()) {
                        deleteRecursively(childFile);
                    } else {
                        if (!childFile.delete()) {
                            Text.log("Could not delete file: " + childFile.getName());
                        }
                    }
                }
            }
        }

        if (!file.delete()) {
            Text.log("Could not delete file: " + file.getName());
        }
    }
}
