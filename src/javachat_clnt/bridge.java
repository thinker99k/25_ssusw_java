package javachat_clnt;

class bridge{
    public final gui_chat c;
    public final gui_login l;
    public final net n;

    bridge(String host, int port, String name, String pass) {
        c = new gui_chat();
        l = new gui_login(c);
        n = new net(host, port, name, pass, this);
    }

    public void exec(){
        // 소켓 통신 establish 실패
        if(!n.init()){
            System.exit(1);
        } /** UI 테스트시 주석처리 하고 실행 */

        n.init();
        n.exec();

        c.initComponents();

        c.addSendListener(e -> {
            String txt = c.getInputText().trim();
            if (!txt.isEmpty()) {
                n.send("0 " + txt);
                c.clearInput();
            }
        });

        c.setVisible(true);

        //l.setVisible(true);
    }

    public void onIncoming(String line) {
        c.appendChat(line);
    }

    @Override
    public void onStatusUpdate(String name, Boolean status){
        g.updateStatus(name, status);
    }
}