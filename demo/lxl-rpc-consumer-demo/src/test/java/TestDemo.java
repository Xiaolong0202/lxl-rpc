/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/29  21:37
 **/
public class TestDemo {

    public static void main(String[] args) {
        //可以使用Runtime.getRuntime().addShutdownHook钩子函数处理正在进行的请求
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("程序别关闭-------------");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("程序别关闭");
        }));

        while (true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("程序正在运行");
        }
    }
}
