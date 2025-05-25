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

    net(String host, int port, bridge b) {
        this.host = host;
        this.port = port;
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
        // 1) GUI 로그인 다이얼로그 띄우기
        if (SwingUtilities.isEventDispatchThread()) {
            // 이미 EDT라면 그냥 modal dialog 띄우기만 해도 블록됩니다.
            b.l.setVisible(true);
        } else {
            try {
                SwingUtilities.invokeAndWait(() -> b.l.setVisible(true));
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

        // 2) 사용자가 Cancel 또는 X 누르면 종료
        if (!b.l.isSucceeded()) {
            System.exit(0);
        }

        // 3) 입력된 ID/PW 가져오기
        String id = b.l.getUsername();
        String pw = b.l.getPassword();

        // 4) 서버로 전송
        out.println(id + " " + pw);

        // 5) 서버 응답 대기
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        int ret;
        try {
            ret = Integer.parseInt(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        // 6) 로그인 결과에 따라 메시지 박스 띄우기
        switch (ret) {
            case TOO_MANY:
                JOptionPane.showMessageDialog(
                        null,
                        "로그인 불가능, 프로그램을 다시 시작해주세요!",
                        "로그인 오류",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(0);
                break;
            case WRONG_NO:
                JOptionPane.showMessageDialog(
                        null,
                        "인증 정보가 틀립니다!",
                        "로그인 오류",
                        JOptionPane.ERROR_MESSAGE
                );
                break; // exec() 루프에서 재시도
            case LOGIN_OK:
                // 성공
                break;
            default:
                break;
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