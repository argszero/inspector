package org.inspector.core.replace;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 *
 */
public class Agent {
    public static void agentmain(String args, Instrumentation inst) throws IOException, UnmodifiableClassException, InterruptedException {
        final Map<String, byte[]> newClasses = new HashMap<String, byte[]>();
        final Map<String, byte[]> oldClasses = new HashMap<String, byte[]>();
        for (String arg : args.split(" ")) {
            File newClassFile = new File(arg);
            String className = readClassName(newClassFile);
            byte[] bytes = readClassContent(newClassFile);
            newClasses.put(className, bytes);
            System.out.println("discover:" + className);

        }

        ClassFileTransformer transformer = new ClassesFileTransformer(newClasses, oldClasses);
        inst.addTransformer(transformer, true);
        replaceLoadedClasses(inst, newClasses, oldClasses);
        long time = Long.parseLong(System.getProperty("time", 60 + ""));
        if (time >= 0) {
            System.out.println(format("classes will transform back after %d second.", time));

            Thread.sleep(time * 1000);

            System.out.println("classes will transform back now");
            inst.removeTransformer(transformer);

            transformer = new ClassesFileTransformer(oldClasses, newClasses);
            inst.addTransformer(transformer, true);
            replaceLoadedClasses(inst, oldClasses, newClasses);
            inst.removeTransformer(transformer);
            System.out.println("classes already transform backed");
        } else {
            System.out.println(format("classes will never transform back is time is set to %d", time));
        }

    }


    private static void replaceLoadedClasses(Instrumentation inst, final Map<String, byte[]> newClasses, final Map<String, byte[]> oldClasses) throws UnmodifiableClassException {
        Class[] classes = inst.getAllLoadedClasses();
        System.out.println("Loaded Classes:" + classes.length);
        for (Class clz : classes) {
            if (newClasses.containsKey(clz.getName())) {
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
        return strings[classes[(dis.readShort() & 0xffff) - 1] - 1].replace('/', '.');
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
