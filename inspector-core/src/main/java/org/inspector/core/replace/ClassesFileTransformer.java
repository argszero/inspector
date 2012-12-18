package org.inspector.core.replace;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;

public class ClassesFileTransformer implements ClassFileTransformer {
    Map<String, byte[]> newClasses;
    Map<String, byte[]> oldClasses;

    public ClassesFileTransformer(Map<String, byte[]> newClasses, Map<String, byte[]> oldClasses) {
        this.newClasses = newClasses;
        this.oldClasses = oldClasses;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (newClasses.containsKey(className)) {
            System.out.println("replace :" + className);
            byte[] newClass = newClasses.get(className);
            oldClasses.put(className, classfileBuffer);
            return newClass;
        } else {
            return classfileBuffer;
        }
    }
}
