package javachat_clnt;

import javax.swing.*;
import java.io.*;
import java.net.*;

class net {
    private bridge b;

    private final String host;
    private final int port;

    private Socket sock;
    private PrintWriter out;
    private BufferedReader in;

    private boolean DEBUG = false;

    net(String host, int port, bridge b) {
        this.host = host;
        this.port = port;
        this.b = b;
    }

    public void clean() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
        if (out != null) {
            out.close();
        }
        if (sock != null && !sock.isClosed()) {
            try {
                sock.close();
            } catch (IOException ignored) {
            }
        }
    }

    public boolean init() {
        try {
            sock = new Socket(this.host, port); // 연결 수립
        } catch (IOException e) {
            System.err.println("서버 " + host + ":" + port + "에 연결 실패");
            clean();
            return false;
        }

        try {
            out = new PrintWriter(sock.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("** out buffer init failed");
            clean();
            return false;
        }

        try {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch (IOException e) {
            System.err.println("** in buffer init failed");
            return false;
        }

        return true;
    }

    private static final int LOGIN_OK = 100;
    private static final int WRONG_NO = 200;
    private static final int TOO_MANY = 300;

    public int login(String name, String pass) {
        out.println(name + " " + pass);

        // in은 별도의 Thread이기에, 패킷 오고가는 1초동안 sleep
        try {
            Thread.sleep(1000); //1초 대기
        } catch (InterruptedException e) {
            ; // 무시
        }

        int ret = 0;

        try {
            ret = Integer.parseInt(in.readLine());
        } catch (IOException e) { // 서버와 연결 끊김
            System.err.println("서버와 연결 종료");
            clean();
            System.exit(1);
        }

        return ret;
    }

    public void send_msg(String msg, boolean heartbeat) {
        if (out != null) {
            if (DEBUG) {
                System.out.println("<- " + msg);
            }

            if (heartbeat) {
                out.println("1 " + msg);
            } else {
                out.println("0 " + msg);
            }
        }
    }

    public void recv_msg() {
        try {
            String line;

            while ((line = in.readLine()) != null) {
                if (DEBUG) {
                    System.out.println("-> " + line);
                }

                final String msg = line;
                SwingUtilities.invokeLater(() -> b.onIncoming(msg));
            }

            // 서버와의 연결이 의도치 않게 끊기면
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        null,
                        "서버 연결이 종료되었습니다.",
                        "연결 끊김",
                        JOptionPane.ERROR_MESSAGE
                );
                SwingUtilities.invokeLater(() -> b.c.setServerStatus(false));

                // 이전 채팅 기록 저장은 하게 해줘야 하지 않을까?? 바로 나가면 안됨
            });

            clean();
        } catch (IOException e) {
            ; // 무시
        }
    }
}