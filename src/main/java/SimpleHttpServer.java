import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @Auther lidaxia
 * @Date 2020-01-06 22:19
 */
public class SimpleHttpServer {

    static      ExecutorService execute = Executors.newFixedThreadPool(10);
    static String basePath;
    static ServerSocket serverSocket;
    static int port = 8080;

    public static void setPort(int port) {
        if(port>0){
            SimpleHttpServer.port = port;
        }
    }

    public static void setBasePath(String basePath) {
        if(basePath != null && new File(basePath).exists() && new File(basePath).isDirectory()){
            SimpleHttpServer.basePath = basePath;
        }
    }

    public static void main(String[] args) throws Exception {
        start();
    }

    /**
     * 启动SimpleHttpServer
     */
    public static void start() throws Exception{
        System.out.println("开启服务");
        serverSocket = new ServerSocket(port);
        Socket socket = null;
        while ((socket = serverSocket.accept()) != null){
            execute.execute(new HttpRequestHandler(socket));
            System.out.println("========");
        }
        serverSocket.close();
    }


    static class HttpRequestHandler implements Runnable {

        private Socket socket;

        public HttpRequestHandler() {
        }

        public HttpRequestHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            String line = null;
            BufferedReader reader = null;
            BufferedReader br = null;
            PrintWriter out = null;
            InputStream in = null;

            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String header = reader.readLine();
                /**
                 *
                 */
                String filePath = basePath + header.split(" ")[1];
                out = new PrintWriter(socket.getOutputStream());

                if(filePath.endsWith("jpg") || filePath.endsWith("ico")){
                    in = new FileInputStream(filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int i = 0;
                    while ((i = in.read()) != -1){
                        baos.write(i);
                    }
                    byte[] array = baos.toByteArray();
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: Molly");
                    out.println("Content-Type: image/jpeg");
                    out.println("Content-Length:"+array.length);
                    out.println("");
                    socket.getOutputStream().write(array,0,array.length);
                }else {
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: Molly");
                    out.println("Content-Type: text/html;charset=UTF-8");
                    out.println("");
                    while ((line = br.readLine())!=null){
                        out.println(line);
                    }
                }
                out.flush();

            }catch (Exception e){
                out.println("HTTP/1.1 500");
                out.println("");
                out.flush();
            }finally {
                close(br,in,reader,out,reader);
            }


        }
        private static void close(Closeable... closeables){
            if(closeables != null){
                for (Closeable closeable : closeables){
                    try {
                        closeable.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
