import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class javachat_serv {
    private static final Set<PrintWriter> clientWriters =
            ConcurrentHashMap.newKeySet();

    public static int get_port() {
        System.out.print("사용할 포트 번호를 입력하세요 : ");
        Scanner port_scanner = new Scanner(System.in);
        return port_scanner.nextInt();
    }

    static ServerSocket listener;
    private static Map<String, Boolean> UserStatus = new ConcurrentHashMap<>();
    private static Map<String, Long> LastHeartBeatMap = new ConcurrentHashMap<>();
    /**
     * 실행방법
     * 1. java javachat_clnt <port>
     * 2. java javachat_serv
     */

    private static void sendUserStatusToClients() {
        for (Map.Entry<String, Boolean> entry : UserStatus.entrySet()) {
            String message = "STATUS:USERMAP:" + entry.getKey() + ":" + entry.getValue();
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }
    public static void main(String[] args) throws Exception {
        System.out.println("=== JAVACHAT SERVER ===");

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

        try {
            listener = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println(port + "번 포트에서 서버 시작 실패");
            return;
        }

        System.out.println(port + "번 포트에서 서버 시작");

        new Thread (new startStatusMonitor()).start();
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

    private static class ClientHandler implements Runnable {
        private String client_name;

        private Socket clnt_sock;
        private BufferedReader in;
        private PrintWriter out;

        // ClientHandler 생성자
        ClientHandler(Socket socket) {
            this.clnt_sock = socket;
        }

        /**
         * 애초에 연결이 성립이 된 상태에서 시작된 쓰레드임
         */
        public void run() {
            try {
                in = new BufferedReader(
                        new InputStreamReader(clnt_sock.getInputStream()));
                out = new PrintWriter(
                        clnt_sock.getOutputStream(), true);
                clientWriters.add(out);
            } catch (Exception e) {
                System.err.println(
                        "클라이언트" + clnt_sock.getRemoteSocketAddress() + " 에 연결 실패");
                return;
            }

            /** 핸드쉐이크 */
            try {
                client_name = in.readLine();
                broadcast(client_name + "님이 채팅방에 들어오셨습니다");
            } catch (IOException e) {
                ; // in.readline()에 대한 예외처리 안함
            }

            LastHeartBeatMap.put(client_name, System.currentTimeMillis());
            UserStatus.put(client_name, true);
            sendUserStatusToClients();

            /** 여기서부터 클라이언트 간 실질적인 메시지 교환 */

            String line;
            try {
                while ((line = in.readLine()) != null) {
                    if (line.equals("STATUS:HEARTBEAT")){
                        LastHeartBeatMap.put(client_name, System.currentTimeMillis());
                    }
                    else{
                        broadcast(client_name + " >> " + line);
                    }
            } }catch (Exception e) {
                // in.readline() 에 대한 예외 처리 안함
            }

            clientWriters.remove(out);
            try {
                clnt_sock.close();
            } catch (IOException e) {
                ; // 소켓 닫을때 에러처리 무시
            }
            System.out.println("연결 종료 : " + clnt_sock.getRemoteSocketAddress());
            UserStatus.put(client_name, false);
            System.out.println(client_name + "님이 오프라인으로 전환했습니다.");
            }

        private void broadcast(String message) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }
    private static class startStatusMonitor implements Runnable{
        public void run(){
            while (true) {
                try {
                    Thread.sleep(1000);
                    sendUserStatusToClients();
                    for (var entry : UserStatus.entrySet()) {
                        System.out.println(entry.getKey() + ":" + entry.getValue());
                    }
                    long now = System.currentTimeMillis();
                    for (Map.Entry<String, Long> entry : LastHeartBeatMap.entrySet()) {
                        String user = entry.getKey();
                        long lastbeat = entry.getValue();
                        if (now - lastbeat > 2000 && UserStatus.getOrDefault(user, true)) {
                            UserStatus.put(user, false);
                            System.out.println(user + ":오프라인 전환");
                        } else if (UserStatus.getOrDefault(user, false)) {
                            UserStatus.put(user,true);
                        }
                    }
                } catch (InterruptedException e) {
                    System.err.println("감시 쓰레드 종료");
                    break;
                }
            }
        }
    }
}
