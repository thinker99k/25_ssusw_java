package javachat_clnt;

class bridge{
    public final gui_chat c;
    public final gui_login l;
    public final net n;
    public final heartbeat h;

    bridge(String host, int port, String name, String pass) {
        c = new gui_chat();
        l = new gui_login(c);
        n = new net(host, port, name, pass, this);
        h = new heartbeat();
    }

    public void exec(){
        // 소켓 통신 establish 실패
        if(!n.init()){
            System.exit(1);
        } /** UI 테스트시 주석처리 하고 실행 */

        n.init(); // server에서 accept
        n.exec(); // 이 이후로 메세지 전달

        c.initComponents();

        c.addSendListener(e -> {
            String txt = c.getInputText().trim();
            if (!txt.isEmpty()) {
                n.send("0 " + txt);
                c.clearInput();
            }
        });

        c.setVisible(true);

        Thread hb_thread = new Thread(h);
        hb_thread.start();

        //l.setVisible(true);
    }

    public void onIncoming(String line) {
        String[] tokens = line.split(" ");
        switch (tokens[0]){
            case "1":// heartbeat 응답
                String name = tokens[1];

                Boolean status = Boolean.valueOf(tokens[2]);
                c.updateStatus(name, status);

                //TODO 토큰에 맞춰 유저 상태 리스트 업데이트
                /** DEBUG */
                System.out.println(name + " : " + status);

                break;
            default:
                // nickname >> 으로 시작하는 경우
                c.appendChat(line);
        }
    }
}