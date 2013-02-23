package ssh.tail;

import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 13-2-22
 * Time: 下午1:02
 * To change this template use File | Settings | File Templates.
 */
public class SshLink {
    private static class MyUserInfo implements UserInfo {
        private String passwd;

        MyUserInfo(String passwd) {
            this.passwd = passwd;
        }

        public String getPassword() {
            return passwd;
        }

        public boolean promptYesNo(String str) {
            return true;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
            return true;
        }

        public void showMessage(String message) {
        }
    }


    public static void main(String[] args) {

        final String hostName = System.getProperty("hostName");
        System.out.println("hostName:" + hostName);
        final String userName = System.getProperty("userName");
        System.out.println("userName:" + userName);
        final String password = System.getProperty("password");
        System.out.println("password:" + password);
        StringBuffer sb = new StringBuffer();
        for (String arg : args) {
            sb.append(arg);
            sb.append(" ");
        }
        String command = sb.toString().trim();
        System.out.println("command:" + command);

//        final String channelTye = "exec";
        final String channelTye = "shell";

        JSch.setLogger(new Logger() {

            public boolean isEnabled(int level) {
                return true;
            }

            public void log(int level, String message) {
                System.out.println(message);
            }
        });


        final JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;
        boolean exec = channelTye.equals("exec");
        try {
            session = jsch.getSession(userName, hostName, 22);
            session.setUserInfo(new MyUserInfo(password));
            session.setDaemonThread(false);
            session.connect();
            channel = session.openChannel(channelTye);
            OutputStream os = channel.getOutputStream();
            BufferedReader lineReader = new BufferedReader(new
                    InputStreamReader(channel.getInputStream()));
            if (exec) {
                ((ChannelExec) channel).setErrStream(System.err);
                ((ChannelExec) channel).setCommand(command);
            }
            channel.connect();
            if (!exec) {
                os.write("PS1=\"MY_PROMPT>\"".getBytes());
                os.write("\n".getBytes());
                os.write("TERM=ansi".getBytes());
                os.write("\n".getBytes());
                os.write(command.getBytes());
                os.write("\n".getBytes());
                os.flush();
            }
            Thread.sleep(2000);
            while (lineReader.ready()) {
                System.out.println(lineReader.readLine());
                if (!lineReader.ready()) {
                    Thread.sleep(2000);
                }
            }
            if (channel.isClosed()) {
                System.out.println("exit-status: " + channel.getExitStatus());
            }
            channel.disconnect();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }


    }

}
