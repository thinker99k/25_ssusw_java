import java.io.*;
import java.util.Scanner;
import java.net.*;

public class javachat_clnt {
    public static int get_port() {
        System.out.print("서버의 포트 번호를 입력하세요 : ");
        Scanner port_scanner = new Scanner(System.in);
        return port_scanner.nextInt();
    }

    public static String get_name() {
        System.out.print("사용할 닉네임을 입력하세요 : ");
        Scanner name_scanner = new Scanner(System.in);
        return name_scanner.next();
    }

    /**
     * 실행방법
     * 1. java javachat_clnt <host> <port> <name>
     * 2. java javachat_clnt <host> <port>
     * 3. java javachat_clnt <host>
     */

    public static void main(String[] args) {
        System.out.println("=== JAVACHAT CLIENT ===");
        String host;
        int port;
        String name;
        BufferedReader con = new BufferedReader(new InputStreamReader(System.in));

        Socket serv_sock;
        BufferedReader in;
        PrintWriter out;

        switch (args.length) {
            case 3: {
                host = args[0];
                port = Integer.parseInt(args[1]);
                name = args[2];
                break;
            }
            case 2: {
                host = args[0];
                port = Integer.parseInt(args[1]);
                /** 이름 모름 */
                name = get_name();
                break;
            }
            case 1: {
                host = args[0];
                /** 포트, 이름 모름 */
                port = get_port();
                name = get_name();
                break;
            }
            default:
                System.err.println("사용법 : java javachat_clnt <host> [port] [name]");
                System.exit(1);
                return;
        }

        try {
            serv_sock = new Socket(host, port); // 연결 수립

            // 서버와 연결이 수립되었으면 in, out 정의
            try {
                in = new BufferedReader(
                        new InputStreamReader(serv_sock.getInputStream()));
                out = new PrintWriter(
                        serv_sock.getOutputStream(), true);
            } catch (Exception e) {
                throw new Exception(e);
            }

            System.out.println("서버 " + host + ":" + port + "에 연결 성공");
        } catch (Exception e) {
            System.err.println("서버 " + host + ":" + port + "에 연결 실패");
            return;
        }

        /** 핸드쉐이크 */
        out.println(name);

        /** 여기서부터 실질적인 메시지 교환 */

        // 새로운 쓰레드 : 서버로부터 들어오는 내용 println 전담
        new Thread(() -> {
            try {
                String in_line;
                while ((in_line = in.readLine()) != null) {
                    System.out.println(in_line);
                }
            } catch (IOException e) {
                System.err.println("서버 " + host + ":" + port + "의 연결이 끊겼습니다");
            } finally {
                try {
                    serv_sock.close();
                } catch (IOException e) {
                    ; // 소켓 닫을때 에러처리 무시
                }
                return;
            }
        }).start();
        //heartbeat관련 쓰레드
        new Thread(() -> {
            try {
                while (!serv_sock.isClosed()){
                    out.println("STATUS:HEARTBEAT");
                    Thread.sleep(900);
                }
            } catch (InterruptedException e){

            }
        }).start();

        // 메인 쓰레드 : 사용자의 입력을 받아 서버로 전달
        String out_line;
        try {
            while ((out_line = con.readLine()) != null)
                out.println(out_line);
        } catch (IOException e) {
            // console.readline() 에 대한 에러처리 안함
        }
    }
}
