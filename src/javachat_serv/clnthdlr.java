package javachat_serv;

import java.io.*;
import java.net.Socket;

import java.util.StringTokenizer;

import static javachat_serv.serv_main.auth_serv;
import static javachat_serv.serv_main.clientWriters;

class ClientHandler implements Runnable {
    private String client_name;

    private Socket clnt_sock;
    private BufferedReader in;
    private PrintWriter out;

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

        String line;
        try {
            while ((line = in.readLine()) != null) { // \n전까지 모든 것을 읽음
                /// 오버헤드 매우 심한데 다른 대안이....
                StringTokenizer st = new StringTokenizer(line);

                if (Integer.parseInt(st.nextToken()) == 0) { // 일반 메세지
                    serv_main.broadcast(client_name + " >> " + st.nextToken());
                } else { // heartbeat
                    // TODO : 구현!!
                }

                st = null; // 빠른 가비지 콜렉션을 위해
            }
        } catch (Exception e) {
            // in.readline() 에 대한 예외 처리 안함
        }

        clientWriters.remove(out);
        try {
            clnt_sock.close();
        } catch (IOException e) {
            ; // 소켓 닫을때 에러처리 무시
        }

        clean();

        /** 퇴장 메세지 */
        serv_main.broadcast("** " + client_name + "님이 채팅방을 떠났습니다 **");
    }
}

