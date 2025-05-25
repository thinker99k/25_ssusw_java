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
        while (true) {
            // 1) 다이얼로그 생성
            gui_login dlg = new gui_login(null);

            // 2) EDT 여부에 따라 띄우기
            if (SwingUtilities.isEventDispatchThread()) {
                dlg.setVisible(true);
            } else {
                try {
                    SwingUtilities.invokeAndWait(() -> dlg.setVisible(true));
                } catch (Exception e) {
                    e.printStackTrace();
                    return -1;
                }
            }

            // 3) Cancel/X 누르면 종료
            if (!dlg.isSucceeded()) {
                System.exit(0);
            }

            // 4) 서버로 ID/PW 전송
            String id = dlg.getUsername();
            String pw = dlg.getPassword();
            out.println(id + " " + pw);

            // 5) 서버 응답 대기
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}

            // 6) 결과 읽기
            int ret;
            try {
                ret = Integer.parseInt(in.readLine());
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }

            // 7) 결과 처리
            if (ret == LOGIN_OK) {
                return ret;  // 성공하면 메서드 종료
            } else if (ret == TOO_MANY) {
                JOptionPane.showMessageDialog(
                        null,
                        "로그인 불가능, 프로그램을 다시 시작해주세요!",
                        "로그인 오류",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(0);
            } else {  // WRONG_NO
                JOptionPane.showMessageDialog(
                        null,
                        "인증 정보가 틀립니다! 다시 시도해 주세요.",
                        "로그인 오류",
                        JOptionPane.ERROR_MESSAGE
                );
                // 루프가 돌면서 동일 dlg를 다시 생성·표시
            }
        }
    }

    /**
     * 한 번만 login() 호출하도록 변경.
     */
    public void exec() {
        int response = login();  // 여기선 무조건 LOGIN_OK 반환
        // 메시지 수신 스레드 시작
        new Thread(this::recv).start();
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