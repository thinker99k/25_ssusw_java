package javachat_clnt;

class bridge implements lsnr {
    gui g;
    private net n;

    bridge(String ver) {
        g= new gui();
        g.setTitle("JAVACHAT CLIENT v" + ver);

        g.addSendListener(e -> {
            String txt = g.getInputText().trim();
            if (!txt.isEmpty() && n != null) {
                n.send(txt);
                g.clearInput();
            }
        });
    }

    void bind(net n) {
        this.n = n;
    }

    void exec() {
        g.setVisible(true);
    }

    @Override
    public void onIncoming(String line) {
        g.appendChat(line);
    }
}