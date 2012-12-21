package org.inspector.agent;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.ConcurrentHashMap;

public interface Agent {
    void main(String args, Instrumentation inst, ConcurrentHashMap<String, byte[]> oldClasses) throws Exception;
}
