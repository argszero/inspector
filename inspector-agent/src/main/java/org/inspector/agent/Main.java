package org.inspector.agent;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static java.lang.String.format;

/**
 *
 */
public class Main {
    public static void main(String[] args)
            throws InterruptedException, IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException, URISyntaxException {
        String agentJarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getCanonicalPath();
        System.out.println(format("trying to attach to jvm [%s]",args[args.length - 1]));
        VirtualMachine vm = VirtualMachine.attach(args[args.length - 1]);
        StringBuilder agentOption = new StringBuilder();
        for (int i = 0; i < args.length - 1; i++) {
            agentOption.append(args[i]);
            agentOption.append(" ");
        }
        agentOption.deleteCharAt(agentOption.length() - 1);
        vm.loadAgent(agentJarFile, agentOption.toString());
        vm.detach();
    }
}
