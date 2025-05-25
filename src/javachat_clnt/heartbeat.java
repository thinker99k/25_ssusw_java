package javachat_clnt;


public class heartbeat implements Runnable{
    private final int interval;
    private Boolean status;
    public heartbeat(){
        this.interval = 900;
        this.status = true;
    }
    public void run(){

        while (true){
            try {
                clnt_main.b.n.send("1 " + this.status);
                Thread.sleep(interval);
            }catch(InterruptedException e){
                this.status = false;
                System.out.println("heartbeat interrupted");
                clnt_main.b.n.send("1 " + this.status);
                break;
            }
        }
    }
}
