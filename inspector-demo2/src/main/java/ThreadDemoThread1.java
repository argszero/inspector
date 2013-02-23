/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 13-2-22
 * Time: 上午10:29
 * To change this template use File | Settings | File Templates.
 */
public class ThreadDemoThread1 extends Thread {
    private boolean shutdown = false;

    @Override
    public void run() {
        while (!shutdown) {
            System.out.println("I am demo");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }
}
