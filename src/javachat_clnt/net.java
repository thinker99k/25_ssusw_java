package javachat_clnt;

import javax.swing.*;
import java.io.*;
import java.net.*;

class net {
    private bridge b;

    private final String host;
    private final int port;
    private final String name;
    private final String pass;

    private Socket sock;
    private PrintWriter out;
    private BufferedReader in;

    net(String host, int port, String name, String pass, bridge b) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.pass = pass;
        this.b = b;
    }

    private void clean() {
        try {
            in.close();
            out.close();
            sock.close();
        } catch (IOException e) {
            ; // 예외 무시
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

    public int login() {
        // TODO : gui 구현!!!
        // 일단 cli상에서는 작동되게 해놨음

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

    public void exec() {
        int response;

        while (true) {
            response = login();

            if (response == TOO_MANY) {
                System.err.println("로그인 불가능, 프로그램을 다시 시작해주세요!");
                clean();
                System.exit(0);
            } else {
                if (response == LOGIN_OK) {
                    break;
                } else if (response == WRONG_NO){
                    // 경고창 띄우기
                    System.err.println("인증 정보가 틀립니다!");
                }
                else {
                    ;
                }
            }
        }

        // 메세지 읽기 쓰레드 시작
        new Thread(this::recv).start(); // 부득이하게 이름이 겹쳐서 네임스페이스 지정..
    }

    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    public void recv() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                final String msg = line;
                SwingUtilities.invokeLater(() -> b.onIncoming(msg));
            }
        } catch (IOException e) {
            ; // 무시
        }
    }
}