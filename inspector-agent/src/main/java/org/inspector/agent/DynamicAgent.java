package org.inspector.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicAgent {
    private final static ConcurrentHashMap<String, byte[]> oldClasses = new ConcurrentHashMap<String, byte[]>();

    public static void agentmain(String args, Instrumentation inst) throws Exception {
        File agentJarFile = new File(DynamicAgent.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        File parentDirectory = agentJarFile.getParentFile();
        List<URL> jars = new ArrayList<URL>();
        File[] children = parentDirectory.listFiles();
        if (children != null) {
            for (File file : children) {
                if (file.getName().endsWith(".jar")) {
                    jars.add(file.toURI().toURL());
                }
            }
        }
        ClassLoader classLoader = new URLClassLoader(jars.toArray(new URL[jars.size()]));
        Class agentClass = classLoader.loadClass("org.inspector.core.InspectorAgent");
        Object obj = agentClass.newInstance();
        if (obj instanceof Agent) {
            ((Agent) obj).main(args, inst, oldClasses);
        }
    }
}
