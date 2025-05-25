package javachat_clnt;

import javax.swing.*;

class bridge {
    public gui_chat c;
    public gui_login l;
    public final net n;
    public final heartbeat h;

    public String name;
    public String pass;

    bridge(String host, int port) {
        c = new gui_chat();
        l = new gui_login(c);
        n = new net(host, port, this);
        h = new heartbeat();
    }

    public void exec() {
        // 소켓 통신 establish 실패
        if (!n.init()) {
            System.exit(1);
        } /** UI 테스트시 주석처리 하고 실행 */

        // 로그인
        tryLogin(); // 성공하면 아래로 진행, 실패하면 exit(0);

        // 메인 창 시작
        c.initComponents();
        c.setServerStatus(true);
        c.setVisible(true);

        // 메세지 센더 시작
        c.addSendListener(e -> {
            String txt = c.getInputText().trim();
            if (!txt.isEmpty()) {
                n.send_msg(txt, false);
                c.clearInput();
            }
        });

        // 메세지 리시버 시작
        Thread t_recv = new Thread(n::recv_msg);
        t_recv.start();

        // 하트비트 시작
        Thread heart = new Thread(h);
        heart.start();
    }

    public void tryLogin() {
        // n.login 반환값에 따라 작동
        int code;

        while (true) {
            l.setVisible(true); // 로그인 창 보이게

            name = l.getUsername();
            pass = l.getPassword();

            code = n.login(name, pass);

            if (code == 100) {
                break;
            } else if (code == 200) {
                JOptionPane.showMessageDialog(
                        null, "ID 또는 PW가 틀립니다.", "로그인 오류",
                        JOptionPane.WARNING_MESSAGE);
                // 루프 반복 → 다시 로그인
            } else { // 서버에서 300코드 보냄
                JOptionPane.showMessageDialog(
                        null, "로그인 시도 횟수 초과", "로그인 오류",
                        JOptionPane.ERROR_MESSAGE);
                n.clean();
                System.exit(0);
            }
        }
    }

    public void onIncoming(String line) {
        String[] tokens = line.split(" ");
        switch (tokens[0]) {
            case "1": { // heartbeat 응답
                System.out.println(line);
                c.setUserStatus(tokens[1], tokens[2]);

                break;
            }
            default:
                // nickname >> 으로 시작하는 경우
                c.appendChat(line);
        }
    }
}