package cn;

import java.io.IOException;  
import java.net.InetSocketAddress;  
import java.nio.ByteBuffer;  
import java.nio.channels.SelectionKey;  
import java.nio.channels.Selector;  
import java.nio.channels.SocketChannel;  
import java.util.Iterator;  
import java.util.concurrent.atomic.AtomicBoolean;  
  
public class RTSPClient extends Thread implements IEvent {  
  
    private static final String VERSION = " RTSP/1.0\n";  
    private static final String RTSP_OK = "RTSP/1.0 200 OK";  
  
    /** *//** Զ�̵�ַ */  
    private final InetSocketAddress remoteAddress;  
  
    /** *//** * ���ص�ַ */  
    private final InetSocketAddress localAddress;  
  
    /** *//** * ����ͨ�� */  
    private SocketChannel socketChannel;  
  
    /** *//** ���ͻ����� */  
    private final ByteBuffer sendBuf;  
  
    /** *//** ���ջ����� */  
    private final ByteBuffer receiveBuf;  
  
    private static final int BUFFER_SIZE = 8192;  
  
    /** *//** �˿�ѡ���� */  
    private Selector selector;  
  
    private String address;  
  
    private Status sysStatus;  
  
    private String sessionid;  
  
    /** *//** �߳��Ƿ�����ı�־ */  
    private AtomicBoolean shutdown;  
      
    private int seq=1;  
      
    private boolean isSended;  
      
    private String trackInfo;  
      
  
    private enum Status {  
        init, options, describe, setup, play, pause, teardown  
    }  
  
    public RTSPClient(InetSocketAddress remoteAddress,  
            InetSocketAddress localAddress, String address) {  
        this.remoteAddress = remoteAddress;  
        this.localAddress = localAddress;
        this.address = address;  
  
        // ��ʼ��������  
        sendBuf = ByteBuffer.allocateDirect(BUFFER_SIZE);  
        receiveBuf = ByteBuffer.allocateDirect(BUFFER_SIZE);  
        if (selector == null) {  
            // �����µ�Selector  
            try {  
                selector = Selector.open();  
            } catch (final IOException e) {  
                e.printStackTrace();  
            }  
        }  
  
        startup();  
        sysStatus = Status.init;  
        shutdown=new AtomicBoolean(false);  
        isSended=false;  
    }  
  
    public void startup() {  
        try {  
            // ��ͨ��  
            socketChannel = SocketChannel.open();  
            // �󶨵����ض˿�  
            socketChannel.socket().setSoTimeout(30000);  
            socketChannel.configureBlocking(false);  
            socketChannel.socket().bind(localAddress);  
            if (socketChannel.connect(remoteAddress)) {  
                System.out.println("��ʼ��������:" + remoteAddress);  
            }  
            socketChannel.register(selector, SelectionKey.OP_CONNECT  
                    | SelectionKey.OP_READ | SelectionKey.OP_WRITE, this);  
            System.out.println("�˿ڴ򿪳ɹ�");  
  
        } catch (final IOException e1) {  
            e1.printStackTrace();  
        }  
    }  
  
    public void send(byte[] out) {  
        if (out == null || out.length < 1) {  
            return;  
        }  
        synchronized (sendBuf) {  
            sendBuf.clear();  
            sendBuf.put(out);  
            sendBuf.flip();  
        }  
  
        // ���ͳ�ȥ  
        try {  
            write();  
            isSended=true;  
        } catch (final IOException e) {  
            e.printStackTrace();  
        }  
    }  
  
    public void write() throws IOException {  
        if (isConnected()) {  
            try {  
                socketChannel.write(sendBuf);  
            } catch (final IOException e) {  
            }  
        } else {  
            System.out.println("ͨ��Ϊ�ջ���û��������");  
        }  
    }  
  
    public byte[] recieve() {  
        if (isConnected()) {  
            try {  
                int len = 0;  
                int readBytes = 0;  
  
                synchronized (receiveBuf) {  
                    receiveBuf.clear();  
                    try {  
                        while ((len = socketChannel.read(receiveBuf)) > 0) {  
                            readBytes += len;  
                        }  
                    } finally {  
                        receiveBuf.flip();  
                    }  
                    if (readBytes > 0) {  
                        final byte[] tmp = new byte[readBytes];  
                        receiveBuf.get(tmp);  
                        return tmp;  
                    } else {  
                        System.out.println("���յ�����Ϊ��,������������");  
                        return null;  
                    }  
                }  
            } catch (final IOException e) {  
                System.out.println("������Ϣ����:");  
            }  
        } else {  
            System.out.println("�˿�û������");  
        }  
        return null;  
    }  
  
    public boolean isConnected() {  
        return socketChannel != null && socketChannel.isConnected();  
    }  
  
    private void select() {  
        int n = 0;  
        try {  
            if (selector == null) {  
                return;  
            }  
            n = selector.select(1000);  
  
        } catch (final Exception e) {  
            e.printStackTrace();  
        }  
  
        // ���select���ش���0�������¼�  
        if (n > 0) {  
            for (final Iterator<SelectionKey> i = selector.selectedKeys()  
                    .iterator(); i.hasNext();) {  
                // �õ���һ��Key  
                final SelectionKey sk = i.next();  
                i.remove();  
                // ������Ƿ���Ч  
                if (!sk.isValid()) {  
                    continue;  
                }  
  
                // �����¼�  
                final IEvent handler = (IEvent) sk.attachment();  
                try {  
                    if (sk.isConnectable()) {  
                        handler.connect(sk);  
                    } else if (sk.isReadable()) {  
                        handler.read(sk);  
                    } else {  
                        // System.err.println("Ooops");  
                    }  
                } catch (final Exception e) {  
                    handler.error(e);  
                    sk.cancel();  
                }  
            }  
        }  
    }  
  
    public void shutdown() {  
        if (isConnected()) {  
            try {  
                socketChannel.close();  
                System.out.println("�˿ڹرճɹ�");  
            } catch (final IOException e) {  
                System.out.println("�˿ڹرմ���:");  
            } finally {  
                socketChannel = null;  
            }  
        } else {  
            System.out.println("ͨ��Ϊ�ջ���û������");  
        }  
    }  
  
    @Override  
    public void run() {  
        // ������ѭ������  
        while (!shutdown.get()) {  
            try {  
                if (isConnected()&&(!isSended)) {  
                    switch (sysStatus) {  
                    case init:  
                        doOption();  
                        break;  
                    case options:  
                        doDescribe();  
                        break;  
                    case describe:  
                        doSetup();  
                        break;  
                    case setup:  
                        if(sessionid==null&&sessionid.length()>0){  
                            System.out.println("setup��û����������");  
                        }else{  
                            doPlay();  
                        }  
                        break;  
                    case play:  
                        doPause();  
                        break;  
                          
                    case pause:  
                        doTeardown();  
                        break;  
                    default:  
                        break;  
                    }  
                }  
                // do select  
                select();  
                try {  
                    Thread.sleep(5000);  
                } catch (final Exception e) {  
                }  
            } catch (final Exception e) {  
                e.printStackTrace();  
            }  
        }  
          
        shutdown();  
    }  
  
    public void connect(SelectionKey key) throws IOException {  
        if (isConnected()) {  
            return;  
        }  
        // ���SocketChannel������  
        socketChannel.finishConnect();  
        while (!socketChannel.isConnected()) {  
            try {  
                Thread.sleep(300);  
            } catch (final InterruptedException e) {  
                e.printStackTrace();  
            }  
            socketChannel.finishConnect();  
        }  
  
    }  
  
    public void error(Exception e) {  
        e.printStackTrace();  
    }  
  
    public void read(SelectionKey key) throws IOException {  
        // ������Ϣ  
        final byte[] msg = recieve();  
        if (msg != null) {  
            handle(msg);  
        } else {  
            key.cancel();  
        }  
    }  
  
    private void handle(byte[] msg) {  
        String tmp = new String(msg);  
        System.out.println("�������ݣ�");  
        System.out.println(tmp);  
        if (tmp.startsWith(RTSP_OK)) {  
            switch (sysStatus) {  
            case init:  
                sysStatus = Status.options;  
                break;  
            case options:  
                sysStatus = Status.describe;  
                trackInfo=tmp.substring(tmp.indexOf("trackID"));  
                break;  
            case describe:  
                sessionid = tmp.substring(tmp.indexOf("Session: ") + 9, tmp  
                        .indexOf("Date:"));  
                if(sessionid!=null&&sessionid.length()>0){  
                    sysStatus = Status.setup;  
                }  
                break;  
            case setup:  
                sysStatus = Status.play;  
                break;  
            case play:  
                sysStatus = Status.pause;  
                break;  
            case pause:  
                sysStatus = Status.teardown;  
                shutdown.set(true);  
                break;  
            case teardown:  
                sysStatus = Status.init;  
                break;  
            default:  
                break;  
            }  
            isSended=false;  
        } else {  
            System.out.println("���ش���" + tmp);  
        }  
  
    }  
  
    private void doTeardown() {  
        StringBuilder sb = new StringBuilder();  
        sb.append("TEARDOWN ");  
        sb.append(this.address);  
        sb.append("/");
        sb.append(VERSION);
        sb.append("Cseq: ");
        sb.append(seq++);
        sb.append("\n");  
        sb.append("User-Agent: RealMedia Player HelixDNAClient/10.0.0.11279 (win32)\n");  
        sb.append("Session: ");  
        sb.append(sessionid);  
        sb.append("\n");  
        send(sb.toString().getBytes());
        System.out.println(sb.toString());
    }  
  
    private void doPlay() {  
        StringBuilder sb = new StringBuilder();  
        sb.append("PLAY ");  
        sb.append(this.address);  
        sb.append(VERSION);  
        sb.append("Session: ");  
        sb.append(sessionid);  
        sb.append("Cseq: ");  
        sb.append(seq++);  
        sb.append("\n");  
        sb.append("\n");  
        System.out.println(sb.toString());  
        send(sb.toString().getBytes());  
  
    }  
  
    private void doSetup() {  
        StringBuilder sb = new StringBuilder();  
        sb.append("SETUP ");  
        sb.append(this.address);  
        sb.append("/");  
        sb.append(trackInfo);  
        sb.append(VERSION);  
        sb.append("Cseq: ");  
        sb.append(seq++);  
        sb.append("\n");  
        sb.append("Transport: RTP/AVP;UNICAST;client_port=16264-16265;mode=play\n");  
        sb.append("\n");  
        System.out.println(sb.toString());
        send(sb.toString().getBytes());
    }  
  
    private void doOption() {  
        StringBuilder sb = new StringBuilder();  
        sb.append("OPTIONS ");  
        sb.append(this.address.substring(0, address.lastIndexOf("/")));  
        sb.append(VERSION);  
        sb.append("Cseq: ");  
        sb.append(seq++);  
        sb.append("\n");  
        sb.append("\n");  
        System.out.println(sb.toString());  
        send(sb.toString().getBytes());  
    }  
  
    private void doDescribe() {  
        StringBuilder sb = new StringBuilder();  
        sb.append("DESCRIBE ");  
        sb.append(this.address);  
        sb.append(VERSION);  
        sb.append("Cseq: ");  
        sb.append(seq++);  
        sb.append("\n");  
        sb.append("\n");  
        System.out.println(sb.toString());  
        send(sb.toString().getBytes());  
    }  
      
    private void doPause() {  
        StringBuilder sb = new StringBuilder();  
        sb.append("PAUSE ");  
        sb.append(this.address);  
        sb.append("/");  
        sb.append(VERSION);  
        sb.append("Cseq: ");  
        sb.append(seq++);  
        sb.append("\n");  
        sb.append("Session: ");  
        sb.append(sessionid);  
        sb.append("\n");  
        send(sb.toString().getBytes());  
        System.out.println(sb.toString());  
    }  
      
    public static void main(String[] args) {  
        try {  
            // RTSPClient(InetSocketAddress remoteAddress,  
            // InetSocketAddress localAddress, String address)  
            RTSPClient client = new RTSPClient(  
                    //new InetSocketAddress("101.101.109.197", 6554), 
                    new InetSocketAddress("218.204.223.237", 554), 
                    //new InetSocketAddress("217.146.95.166", 554),  
                    //new InetSocketAddress("211.139.194.251", 554),  
                    //new InetSocketAddress("121.14.129.221", 554),  
                    new InetSocketAddress("10.20.175.155", 0),  
                    //"rtsp://101.101.109.197:6554/1.sdp");  
                    "rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp");  
        			//"rtsp://211.139.194.251:554/live/2/13E6330A31193128/5iLd2iNl5nQ2s8r8.sdp");  
            		//"rtsp://218.204.223.237:554/live/1/6D1E43167B3A7BDA/oby9efo80duh9bjf.sdp");  
        			//"rtsp://217.146.95.166:554/live/ch12bqvga.3gp");  
            client.start();  
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
    }  
} 