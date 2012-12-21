package org.inspector.core;

import org.inspector.agent.Agent;
import org.inspector.core.replace.ClassesFileTransformer;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

/**
 *
 */
public class InspectorAgent implements Agent {
    @Override
    public void main(String args, Instrumentation inst, ConcurrentHashMap<String, byte[]> oldClasses) throws IOException, UnmodifiableClassException, InterruptedException {
        final Map<String, byte[]> newClasses = discoverClassesTobeReplaced(args);
        if (!oldClasses.isEmpty()) {
            // if there is old classes not recovered, recover it now
            recover(inst, oldClasses);
        }

        ClassFileTransformer transformer = new ClassesFileTransformer(newClasses, oldClasses);
        inst.addTransformer(transformer, true);
        replaceLoadedClasses(inst, newClasses);
        long time = Long.parseLong(System.getProperty("time", 60 + ""));
        if (time >= 0) {
            if (Boolean.parseBoolean(System.getProperty("enableCountdown", "true"))) {
                for (long i = time; i >= 0; i--) {
                    System.out.print(format("\rclasses will transform back after %d second.(use -DenableCountdown=false to turn off count down)", i));
                    Thread.sleep(1000);
                }
            } else {
                System.out.print(format("\rclasses will transform back after %d second.(use -DenableCountdown=true to turn on count down)", time));
                Thread.sleep(time * 1000);
            }
            System.out.println("classes will transform back now");
            inst.removeTransformer(transformer);
            recover(inst, oldClasses);
        } else {
            System.out.println(format("classes will never transform back is time is set to %d", time));
        }

    }

    private void recover(Instrumentation inst, ConcurrentHashMap<String, byte[]> oldClasses) throws UnmodifiableClassException {
        System.out.println("recover...");
        ClassFileTransformer transformer = new ClassesFileTransformer(oldClasses, new ConcurrentHashMap<String, byte[]>());
        inst.addTransformer(transformer, true);
        replaceLoadedClasses(inst, oldClasses);
        inst.removeTransformer(transformer);
        oldClasses.clear();
        System.out.println("recovered success");
    }

    private Map<String, byte[]> discoverClassesTobeReplaced(String args) throws IOException {
        final Map<String, byte[]> newClasses = new HashMap<String, byte[]>();
        for (String arg : args.split(" ")) {
            if (arg.startsWith("-D")) {
                String[] split = arg.substring(2, arg.length()).split("=");
                System.setProperty(split[0], split[1]);
            }
            File newClassFile = new File(arg);
            discover(newClasses, newClassFile);
        }
        return newClasses;
    }

    private void discover(Map<String, byte[]> newClasses, File file) throws IOException {
        if (file.isFile()) {
            String className
                    = readClassName(file);
            if (className != null) {
                byte[] bytes = readClassContent(file);
                newClasses.put(className, bytes);
                System.out.println("discover:" + className);
            }
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    discover(newClasses, child);
                }
            }
        }
    }


    private static void replaceLoadedClasses(Instrumentation inst, final Map<String, byte[]> newClasses) throws UnmodifiableClassException {
        Class[] classes = inst.getAllLoadedClasses();
        System.out.println("Loaded classes:" + classes.length);
        for (Class clz : classes) {
            if (newClasses.containsKey(clz.getName().replace('.', '/'))) {
                System.out.println("trans:" + clz.getName());
                inst.retransformClasses(clz);
            }
        }
    }

    public static String readClassName(File file) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            return readClassName(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static String readClassName(InputStream is) throws IOException {
        try {
            DataInputStream dis = new DataInputStream(is);
            dis.readLong(); // skip header and class version
            int cpcnt = (dis.readShort() & 0xffff) - 1;
            int[] classes = new int[cpcnt];
            String[] strings = new String[cpcnt];
            for (int i = 0; i < cpcnt; i++) {
                int t = dis.read();
                if (t == 7) classes[i] = dis.readShort() & 0xffff;
                else if (t == 1) strings[i] = dis.readUTF();
                else if (t == 5 || t == 6) {
                    dis.readLong();
                    i++;
                } else if (t == 8) dis.readShort();
                else dis.readInt();
            }
            dis.readShort(); // skip access flags
            return strings[classes[(dis.readShort() & 0xffff) - 1] - 1];
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] readClassContent(File file) throws IOException {
        // precondition
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            long length = file.length();
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = in.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            if (offset < bytes.length) {
                throw new IOException("Could not completely read file "
                        + file.getName());
            }
            in.close();
            return bytes;
        } finally {
            if (in != null) {
                in.close();
            }
        }

    }
}
