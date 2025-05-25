package javachat_serv;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

import static javachat_serv.serv_main.auth_serv;
import static javachat_serv.serv_main.clientWriters;

class ClientHandler implements Runnable {
    private String client_name;
    private static int client_number = 0; //새 클라이언트 생성 시 +1, 감시 쓰레드가 이를 감지하고 클라이언트 별 heartbeat 시그널 전송
    private final Socket sock;
    private BufferedReader in;
    private PrintWriter out;
    private long last_heartbeat;
    private boolean online;

    private boolean DEBUG = true;

    // ClientHandler 생성자
    ClientHandler(Socket socket) {
        this.sock = socket;
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

    private Runnable heartbeat_monitor() {
        return () -> {
            int current_client_number = client_number;

            while (true) {
                //새 클라이언트 접속(+) 혹은 다른 클라이언트 접속 종료(-)시
                if (current_client_number != client_number) {
                    current_client_number = client_number;
                    serv_main.broadcast("1 " + client_name + " " + online);
                }

                if (System.currentTimeMillis() - last_heartbeat > 2000) { // 딜레이가 길어질 경우
                    if (online) { // 온라인 -> 오프라인
                        online = false;
                        serv_main.broadcast("1 " + client_name + " " + online);
                        System.out.println(client_name + " on -> OFF");
                    }
                    // 오프라인 -> 오프라인 : 아무것도 x
                } else { // 딜레이가 정상적일 경우
                    if (!online) { // 오프라인 -> 온라인 (두 번째 하트비트 부터)
                        online = true;
                        serv_main.broadcast("1 " + client_name + " " + online);
                        System.out.println(client_name + " off -> ON");
                    }
                    // 온라인 -> 온라인 : 아무것도 x
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException Ignored) {
                    System.err.println("heartbeat interrupted");
                }
            }
        };
    }

    private boolean init() {
        try {
            in = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter(
                    sock.getOutputStream(), true);

            return true;
        } catch (Exception e) {
            System.err.println(
                    "클라이언트" + sock.getRemoteSocketAddress() + " 에 연결 실패");
            return false;
        }
    }

    private static final int LOGIN_OK = 100;
    private static final int WRONG_NO = 200;
    private static final int TOO_MANY = 300;

    private boolean auth() throws IOException {
        String payload, id, pw;

        payload = in.readLine();
        StringTokenizer st = new StringTokenizer(payload);
        id = st.nextToken();
        pw = st.nextToken();

        if (auth_serv.authenticate(id, pw)) {
            client_name = id;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void run() {
        // init에 실패하면 반환
        if (!init()) {
            clean();
            return;
        }

        final int MAX = 5; // 최대 5번의 시도 허용
        int CNT = 1; // 이번 연결에서 사용, MAX번 로그인 실패시 무조건 300 반환

        while (true) {
            try {
                if (CNT >= MAX) {
                    out.println(TOO_MANY);
                    clean(); // 연결 끊어버림
                    return;
                }

                if (auth()) {
                    out.println(LOGIN_OK);
                    break;
                } else {
                    out.println(WRONG_NO);
                    CNT++;
                }
            } catch (IOException e) { // 연결 끊긴 경우
                clean();
                return;
            }
        }

        /** 여기서부터는 인증 통과됨, 실질적인 메시지 교환 */

        clientWriters.add(out);
        client_number++;

        // 입장 메세지
        serv_main.broadcast(
                "** " + client_name + "님이 채팅방에 들어오셨습니다 **");
        System.out.println("** " + client_name + " handler ON");

        online = true;


        Thread monitor = new Thread(heartbeat_monitor());
        monitor.start();

        String line;
        System.out.println("going loop!!");
        try {
            while ((line = in.readLine()) != null) { // \n전까지 모든 것을 읽음
                if (DEBUG){
                    System.out.println("<- : " + line);
                }

                /// 오버헤드 매우 심한데 다른 대안이....
                StringTokenizer st = new StringTokenizer(line);

                if (Integer.parseInt(st.nextToken()) == 0) { // 일반 메세지
                    serv_main.broadcast(client_name + " >> " + st.nextToken());
                } else { // heartbeat
                    last_heartbeat = System.currentTimeMillis();
                }

                st = null; // 빠른 가비지 콜렉션을 위해
            }
        } catch (Exception Ignored) {
            // in.readline() 에 대한 예외 처리 안함
        }

        clientWriters.remove(out);
        try {
            sock.close();
            client_number--; //접속 종료 시
        } catch (IOException e) {
            ; // 소켓 닫을때 에러처리 무시
        }

        clean();

        /** 퇴장 메세지 */
        serv_main.broadcast("** " + client_name + "님이 채팅방을 떠났습니다 **");
        System.out.println("** " + client_name + " handler OFF");
    }
}

