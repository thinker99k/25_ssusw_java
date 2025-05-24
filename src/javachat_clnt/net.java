package javachat_clnt;

import javax.swing.*;
import java.io.*;
import java.net.*;

class net {
    private final String host;
    private final int port;
    private final String name;
    private final lsnr ui;

    private Socket sock;
    private PrintWriter out;
    private BufferedReader in;

    net(String host, int port, String name, lsnr ui) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.ui = ui;
    }

    public void exec() {
        try {
            sock = new Socket(this.host, port); // 연결 수립
        } catch (IOException e) {
            System.err.println("서버 " + host + ":" + port + "에 연결 실패");
            return;
        }

        try {
            out = new PrintWriter(sock.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("** out buffer init failed");
            return;
        }

        try {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch (IOException e) {
            System.err.println("** in buffer init failed");
            return;
        }

        /** 읽기 쓰레드 시작 */
        new Thread(this::recv).start();
        new Thread(this::heartbeat).start();
    }
    public void heartbeat(){
        try {
            while (!sock.isClosed()){
                out.println("STATUS:HEARTBEAT"); //heartbeat to server
                Thread.sleep(900);
            }
        } catch (InterruptedException e){

        }
    }
    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    public void recv() {
        try {
            send(name);

            String line;
            while ((line = in.readLine()) != null) {
                final String msg = line;
                if (line.startsWith("STATUS:USERMAP:")){
                    String[] parts = line.split(":");
                    String username = parts[2];
                    boolean isonline = Boolean.parseBoolean(parts[3]);
                    SwingUtilities.invokeLater(() -> ui.onStatusUpdate(username, isonline));
                }
                else{
                    SwingUtilities.invokeLater(() -> ui.onIncoming(msg));
                }
            }
        } catch (IOException e){
            ; // 무시
        }
    }
}