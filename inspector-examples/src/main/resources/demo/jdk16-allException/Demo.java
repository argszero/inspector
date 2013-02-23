/**
 * Created with IntelliJ IDEA.
 * User: shaoaq
 * Date: 13-2-22
 * Time: 下午12:23
 * To change this template use File | Settings | File Templates.
 */
public class Demo {
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            new NullPointerException("aa");
            Thread.sleep(3000);
        }
    }
}
