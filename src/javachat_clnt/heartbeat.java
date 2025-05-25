package javachat_clnt;

import static javachat_clnt.clnt_main.b;

public class heartbeat implements Runnable {
    private final int interval;
    private Boolean status;

    public heartbeat() {
        this.interval = 900;
        this.status = true;
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException Ignored) {
                System.err.println("heartbeat interrupted");
            }

            // 창이 active할 때만 보냄
            if(b.c.isActive()){
                // 항상 "1 true"만 보냄
                clnt_main.b.n.send_msg(String.valueOf(this.status), true);
            }
        }
    }
}
