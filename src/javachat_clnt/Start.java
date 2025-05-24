package javachat_clnt;

import javax.swing.*;

import static java.lang.Integer.parseInt;

/** 로그인 절차를 포함 */
public class Start {
    private static final String VERSION = "1.1.0";

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java javachat_clnt.Start <host> <port>");
            System.exit(1);
        }

        /* 1) 로그인 다이얼로그 호출 (EDT 밖에서 modal 로 띄움) */
        LoginDialog dlg = new LoginDialog(null);
        dlg.setVisible(true);
        if (!dlg.isSucceeded()) {
            System.out.println("Login cancelled.");
            System.exit(0);
        }

        /* 2) 채팅 GUI 실행 */
        String user = dlg.getUsername();
        bridge b = new bridge(VERSION);
        net    n = new net(args[0], parseInt(args[1]), user, b);
        b.bind(n);

        SwingUtilities.invokeLater(b::exec);  // GUI
        n.exec();                              // 네트워크
    }
}
