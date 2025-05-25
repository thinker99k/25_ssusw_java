package javachat_serv;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class serv_main {
    static ServerSocket listener;
    static AuthServer auth_serv;
    static final String version = "1.1.1";

    public static final Set<PrintWriter> clientWriters =
            ConcurrentHashMap.newKeySet();

    public static final Set<String> clientNames =
            new HashSet<>();

    // 처음 한번 실행
    public static void whoAlive() {
        for (String my_name : clientNames) {
            broadcast("1 " + my_name + " " + true);
        }
    }

    public static void iamAlive(String alive){
        broadcast("1 " + alive + " " + true);
    }

    public static void iamIdle(String idle) {
        broadcast("1 " + idle + " " + false); //
    }

    public static void iamDead(String dead) {
        broadcast("1 " + dead + " " + "kill");
    }

    public static void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }

    public static int get_port() {
        System.out.print("사용할 포트 번호를 입력하세요 : ");
        Scanner port_scanner = new Scanner(System.in);
        return port_scanner.nextInt();
    }

    /**
     * 실행방법
     * 1. java javachat_clnt <port>
     * 2. java javachat_serv
     */

    public static void main(String[] args) throws Exception {
        System.out.println("=== JAVACHAT SERVER v" + version + " ===");

        int port;

        switch (args.length) {
            case 1: {
                port = Integer.parseInt(args[0]);
                break;
            }
            case 0: {
                /** 포트 모름 */
                port = get_port();
                break;
            }
            default: {
                System.err.println("사용법 : java javachat_serv [port]");
                System.exit(1);
                return;
            }
        }

        /** 인증서버 시작 */
        try {
            auth_serv = new AuthServer("./src/javachat_serv/db.txt");
        } catch (IOException e) {
            System.err.println("인증서버 시작 실패 ");
        }

        /* // 인증서버 테스트용
        System.out.println("khlee / 1234 (t) → " + auth_serv.authenticate("khlee","1234"));
        System.out.println("trudy / 1234 (f) → " + auth_serv.authenticate("alice","1234"));
        */

        try {
            listener = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println(port + "번 포트에서 서버 시작 실패");
            return;
        }

        System.out.println(port + "번 포트에서 서버 시작");

        /** SS가 accept해서 받은 socket으로 쓰레드 생성 말고는 여기서 더 해줄 거 없음 */
        while (true) {
            Socket sock;
            sock = listener.accept();
            System.out.println(
                    "새로운 연결 : " + sock.getRemoteSocketAddress());

            try {
                new Thread(new ClientHandler(sock)).start();
            } catch (Exception e) { // 쓰레드에 대한 예외 처리
                System.err.println(
                        "클라이언트 핸들러 쓰레드 오류");
            }
        }
        /** 프로그램 꺼질 때 알아서 리스너 닫힘 */
    }
}