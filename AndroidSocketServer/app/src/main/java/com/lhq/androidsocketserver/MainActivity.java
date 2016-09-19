package com.lhq.androidsocketserver;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.zxing.WriterException;
import com.lhq.androidsocketserver.utils.EncodingHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {


    TextView info, infoip, msg;
    String message = "";


    ImageView code;
    private String uploadPath = Environment.getExternalStorageDirectory() + File.separator + "/serverpath/";

    static final int SocketServerPORT = 8080;
    public static Map<Long, FileLog> datas = new HashMap<Long, FileLog>();// 存放断点数据，最好改为数据库存放
    private ExecutorService executorService;// 线程池
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        info = (TextView) findViewById(R.id.info);
        infoip = (TextView) findViewById(R.id.infoip);
        msg = (TextView) findViewById(R.id.msg);
        code = (ImageView) findViewById(R.id.iv_code_er);

        File file = new File(uploadPath);
        if (!file.exists()) {
            file.mkdirs();
        }
//        infoip.setText();
        createCode(getIpAddress()+","+SocketServerPORT);
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * 50);

        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    private class SocketServerThread extends Thread {

        int count = 0;

        @Override
        public void run() {

            try {
                final ServerSocket serverSocket;
                serverSocket = new ServerSocket(SocketServerPORT);
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        info.setText("I'm waiting here: "
                                + serverSocket.getLocalPort());
                    }
                });


                while (true) {
                    Socket socket = serverSocket.accept();
                    count++;
                    executorService.execute(new SocketServerReplyThread(socket,count));
                    message += "#" + count + " from " + socket.getInetAddress()
                            + ":" + socket.getPort() + "\n";

                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            msg.setText(message);
                        }
                    });

//                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
//                            socket, count);
//                    socketServerReplyThread.run();

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private class SocketServerReplyThread implements Runnable {

        private Socket hostThreadSocket;
        int cnt;

        SocketServerReplyThread(Socket socket, int c) {
            hostThreadSocket = socket;
            cnt = c;
        }

        @Override
        public void run() {
            try {

                PushbackInputStream inStream = new PushbackInputStream(
                        hostThreadSocket.getInputStream());

                String head = StreamTool.readLine(inStream);
                System.out.println(head);
                if (head != null) {
                    // 下面从协议数据中读取各种参数值
                    String[] items = head.split(";");
                    String filelength = items[0].substring(items[0].indexOf("=") + 1);
                    String filename = items[1].substring(items[1].indexOf("=") + 1);
                    String sourceid = items[2].substring(items[2].indexOf("=") + 1);
                    Long id = System.currentTimeMillis();
                    FileLog log = null;
                    if (null != sourceid && !"".equals(sourceid)) {
                        id = Long.valueOf(sourceid);
                        log = find(id);//查找上传的文件是否存在上传记录
                    }
                    File file = null;
                    int position = 0;
                    if (log == null) {//如果上传的文件不存在上传记录,为文件添加跟踪记录
                        String path = new SimpleDateFormat("yyyy/MM/dd/HH/mm").format(new Date());
                        File dir = new File(uploadPath + path);
                        if (!dir.exists()) dir.mkdirs();
                        file = new File(dir, filename);
                        if (file.exists()) {//如果上传的文件发生重名，然后进行改名
                            filename = filename.substring(0, filename.indexOf(".") - 1) + dir.listFiles().length + filename.substring(filename.indexOf("."));
                            file = new File(dir, filename);
                        }
                        save(id, file);
                    } else {// 如果上传的文件存在上传记录,读取上次的断点位置
                        file = new File(log.getPath());//从上传记录中得到文件的路径
                        if (file.exists()) {
                            File logFile = new File(file.getParentFile(), file.getName() + ".log");
                            if (logFile.exists()) {
                                Properties properties = new Properties();
                                properties.load(new FileInputStream(logFile));
                                position = Integer.valueOf(properties.getProperty("length"));//读取断点位置
                            }
                        }
                    }

                    OutputStream outStream = hostThreadSocket.getOutputStream();

                    String response = "sourceid=" + id + ";position=" + position + "\r\n";
                    Log.i("lhq", "response==========" + response);
                    //服务器收到客户端的请求信息后，给客户端返回响应信息：sourceid=1274773833264;position=0
                    //sourceid由服务生成，唯一标识上传的文件，position指示客户端从文件的什么位置开始上传
                    outStream.write(response.getBytes());

                    RandomAccessFile fileOutStream = new RandomAccessFile(file, "rwd");
                    if (position == 0) fileOutStream.setLength(Integer.valueOf(filelength));//设置文件长度
                    fileOutStream.seek(position);//移动文件指定的位置开始写入数据
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    int length = position;
                    while ((len = inStream.read(buffer)) != -1) {//从输入流中读取数据写入到文件中
                        fileOutStream.write(buffer, 0, len);
                        length += len;
                        Properties properties = new Properties();
                        properties.put("length", String.valueOf(length));
                        FileOutputStream logFile = new FileOutputStream(new File(file.getParentFile(), file.getName() + ".log"));
                        properties.store(logFile, null);//实时记录文件的最后保存位置
                        logFile.close();
                    }
                    if (length == fileOutStream.length()) delete(id);
                    fileOutStream.close();
                    inStream.close();
                    outStream.close();
                    file = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public FileLog find(Long sourceid) {
        return datas.get(sourceid);
    }

    // 保存上传记录
    public void save(Long id, File saveFile) {
        // 日后可以改成通过数据库存放
        datas.put(id, new FileLog(id, saveFile.getAbsolutePath()));
    }

    // 当文件上传完毕，删除记录
    public void delete(long sourceid) {
        if (datas.containsKey(sourceid))
            datas.remove(sourceid);
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }
    /**
     * 生成二维码
     *
     * */
    public void createCode(String content) {
        try {
            // 根据字符串生成二维码图片并显示在界面上，第二个参数为图片的大小（350*350）
            Bitmap qrCodeBitmap = EncodingHandler.createQRCode(content, 600);
            code.setImageBitmap(qrCodeBitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if (serverSocket != null) {
//            try {
//                serverSocket.close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
    }
}
