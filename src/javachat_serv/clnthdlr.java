package javachat_serv;

import java.io.*;
import java.net.Socket;

import java.util.StringTokenizer;

import static javachat_serv.serv_main.auth_serv;
import static javachat_serv.serv_main.clientWriters;

class ClientHandler implements Runnable {
    private String client_name;
    private static int client_number = 0; //새 클라이언트 생성 시 +1, 감시 쓰레드가 이를 감지하고 클라이언트 별 heartbeat 시그널 전송
    private Socket clnt_sock;
    private BufferedReader in;
    private PrintWriter out;
    private long heartbeat;
    private Boolean online; // 추후 사용

    // ClientHandler 생성자
    ClientHandler(Socket socket) {
        this.clnt_sock = socket;
    }

    private void clean() {
        try {
            in.close();
            out.close();
            clnt_sock.close();
        } catch (IOException e) {
            ; // 예외 무시
        }
    }

    private Runnable heartbeat_monitor(){
        return () -> {
            try {
                serv_main.broadcast("1 " + client_name + " " + online);
                int current_client_number = client_number;
                while (!Thread.currentThread().isInterrupted()) {
                    long now = System.currentTimeMillis();
                    if (now - heartbeat > 2000) {
                        if (online == true){
                            online = false;
                            serv_main.broadcast("1 " + client_name + " " + online);
                        }
                    } else {
                        if (online == false) {
                            online = true;
                            serv_main.broadcast("1 " + client_name + " " + online);
                        }
                    }
                    if (current_client_number != client_number){//새 클라이언트 접속 혹은 다른 클라이언트 접속 종료시
                        current_client_number = client_number;
                        serv_main.broadcast("1 " + client_name + " " + online);
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                // 감시 쓰레드 종료
            }
        };
    }

    private boolean init() {
        try {
            in = new BufferedReader(
                    new InputStreamReader(clnt_sock.getInputStream()));
            out = new PrintWriter(
                    clnt_sock.getOutputStream(), true);
            clientWriters.add(out);

            return true;
        } catch (Exception e) {
            System.err.println(
                    "클라이언트" + clnt_sock.getRemoteSocketAddress() + " 에 연결 실패");
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
        int CNT = 0; // 이번 연결에서 사용, MAX번 로그인 실패시 무조건 300 반환

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

        // 입장 메세지
        serv_main.broadcast(
                "** " + client_name + "님이 채팅방에 들어오셨습니다 **");
        heartbeat = System.currentTimeMillis();
        online = true;
        client_number++;
        Thread monitor= new Thread(heartbeat_monitor());
        monitor.start();
        String line;
        try {
            while ((line = in.readLine()) != null) { // \n전까지 모든 것을 읽음
                /// 오버헤드 매우 심한데 다른 대안이....
                StringTokenizer st = new StringTokenizer(line);

                if (Integer.parseInt(st.nextToken()) == 0) { // 일반 메세지
                    serv_main.broadcast(client_name + " >> " + st.nextToken());
                } else { // heartbeat
                    heartbeat = System.currentTimeMillis();
                }

                st = null; // 빠른 가비지 콜렉션을 위해
            }
        } catch (Exception e) {
            // in.readline() 에 대한 예외 처리 안함
        }

        clientWriters.remove(out);
        try {
            clnt_sock.close();
            client_number--; //접속 종료 시
        } catch (IOException e) {
            ; // 소켓 닫을때 에러처리 무시
        }

        clean();

        /** 퇴장 메세지 */
        serv_main.broadcast("** " + client_name + "님이 채팅방을 떠났습니다 **");
    }
}

