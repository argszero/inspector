package org.inspector.agent;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 *
 */
public class Main {
    public static void main(String[] args)
            throws InterruptedException, IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException, URISyntaxException {
        String agentJarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getCanonicalPath();
        List<String> listArgs = new ArrayList<String>();
        String processId = null;
        for (int i = args.length - 1; i >= 0; i--) {
            System.out.println("arg["+i+"]:>"+args[i].trim()+"<");
            if (Pattern.matches("\\d+", args[i].trim())) {
                processId = args[i].trim();
            } else {
                listArgs.add(args[i]);
            }
        }
        System.out.println(format("trying to attach to jvm [%s]", processId));
        VirtualMachine vm = VirtualMachine.attach(processId);
        StringBuilder agentOption = new StringBuilder();
        for (String arg : listArgs) {
            agentOption.append(arg);
            agentOption.append(" ");
        }
        if (agentOption.length() > 0) {
            agentOption.deleteCharAt(agentOption.length() - 1);
        }
        vm.loadAgent(agentJarFile, agentOption.toString());
        vm.detach();
    }
}
