package org.inspector.agent;

import java.lang.instrument.Instrumentation;

public interface Agent {
    void main(String args, Instrumentation inst) throws Exception;
}
