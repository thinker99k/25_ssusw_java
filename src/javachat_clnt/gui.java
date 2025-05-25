package javachat_clnt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

class gui_chat extends JFrame { //클래스 자체가 하나의 윈도우가 되도록 JFrame 상속
    private static final int SIDE_WIDTH = 200; // 사용자 목록과 placeholder 폭 고정

    private JTextArea chatArea;//채팅창 영역
    private JTextField inputField;// 메세지 입력창 영역
    private JButton sendButton; //전송 영역
    private JList<String> userList; //사용자 목록 나열 리스트
    private DefaultListModel<String> listModel;//userlist 의 데이터 모델, 사용자에 변화가 있을 시 갱신된다.
    private JPanel placeholderPanel; // 우측 하단 빈공간 영역
    private JButton clearButton;
    private JLabel serverStatusLabel;


    gui_chat() {
        setTitle("JAVACHAT CLIENT v" + clnt_main.version);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // x 버튼 누르면 프로세스 종료
        setSize(1000, 800); // 윈도우 창 사이즈 고정
        setResizable(false);                // 사이즈를 바꿀 수 없도록 설정
        setLocationRelativeTo(null);        // 화면 정중앙에 프레임을 띄움
    }

    public void initComponents() {
        // 1) 전송 버튼 생성 및 크기 계산
        sendButton = new JButton("전송");
        Dimension btnDim = sendButton.getPreferredSize();//버튼의 기본 크기를 가져옴
        sendButton.setPreferredSize(new Dimension(100, btnDim.height)); // 폭 고정

        // 2) 메시지 입력창 생성 및 버튼과 동일한 높이로 고정
        inputField = new JTextField();
        inputField.setPreferredSize(new Dimension(0, btnDim.height)); //높이는 버튼과 동일, 폭은 자동 확장

        // 3) 입력 패널: 입력창 + 전송 버튼
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);    //중앙에 메세지 입력창
        inputPanel.add(sendButton, BorderLayout.EAST);      // 오른쪽에 버튼 배치
        inputPanel.setPreferredSize(new Dimension(0, btnDim.height)); // 패널의 전체 높이는 버튼의 높이에 맞춰서

        // 4) 채팅 내용 영역 생성
        chatArea = new JTextArea();
        chatArea.setEditable(false); //읽기 전용으로 설정
        chatArea.setLineWrap(true);// 자동 줄 바꿈 활성화
        JScrollPane chatScroll = new JScrollPane(chatArea);//채팅 영역에 스크롤바 붙임

        // 5) 왼쪽 패널 구성: 채팅 내용 위, 입력창 아래
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(chatScroll, BorderLayout.CENTER); //채팅창을 가운데에
        leftPanel.add(inputPanel, BorderLayout.SOUTH);  //입력 패널을 아래에 추가

        // 6) 사용자 목록 생성
        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        JScrollPane userScroll = new JScrollPane(userList);

        // 7) placeholder 패널 생성 (추가 기능을 위한 빈 공간)
        placeholderPanel = new JPanel();
        placeholderPanel.setPreferredSize(new Dimension(SIDE_WIDTH, 150)); // 높이 조정 가능

        // 7a) clear 기능
        clearButton = new JButton("Clear Chat");
        clearButton.addActionListener(e -> {
            chatArea.setText("");          // wipe the chat area
        });
        placeholderPanel.add(clearButton, BorderLayout.SOUTH);

        // 7b) 서버상태
        serverStatusLabel = new JLabel();
        serverStatusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        placeholderPanel.add(serverStatusLabel, BorderLayout.SOUTH);

        // 8) 오른쪽 패널 구성: 사용자 목록 위, placeholder 아래
        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.setPreferredSize(new Dimension(SIDE_WIDTH, 0)); // 폭 고정, 높이는 자동
        eastPanel.add(userScroll, BorderLayout.CENTER);   //사용자 목록 가운데에
        eastPanel.add(placeholderPanel, BorderLayout.SOUTH); // 빈공간을 아래에

        // 9) 최종 레이아웃: 왼쪽(leftPanel)과 오른쪽(eastPanel)
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(leftPanel, BorderLayout.CENTER); //가운데에 버튼, 입력창, 채팅창
        cp.add(eastPanel, BorderLayout.EAST); // 동쪽에 사용자 목록, 빈공간
    }

    // 메시지 전송 이벤트 등록
    public void addSendListener(ActionListener listener) {
        sendButton.addActionListener(listener);
        inputField.addActionListener(listener);
    }

    // 입력 필드의 텍스트 읽기
    public String getInputText() {
        return inputField.getText();
    }

    // 입력 필드 비우기
    public void clearInput() {
        inputField.setText("");
    }

    // 채팅 영역에 메시지 추가
    public void appendChat(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void setUserStatus(String name, String status) {
        if(Objects.equals(status, "kill")){
            for (int i = 0; i < listModel.size(); i++) {
                String userEntry = listModel.getElementAt(i).trim();
                String[] parts = userEntry.split(" - ", 2);

                if (parts.length > 0 && parts[0].trim().equals(name)) {
                    listModel.remove(i);
                    System.out.println("삭제됨: " + name);
                    return;
                }
            }
        }

        String updatedStatus = name + " - " +
                (status.equals("true") ? "Online" : "Offline");

        boolean found = false;
        for (int i = 0; i < listModel.size(); i++) {
            String userEntry = listModel.getElementAt(i).trim();
            String[] parts = userEntry.split(" - ", 2);

            if (parts.length > 0 && parts[0].trim().equals(name)) {
                if (!userEntry.equals(updatedStatus)) {
                    listModel.set(i, updatedStatus);
                    System.out.println(name + " -> " + updatedStatus);
                }
                found = true;
                break;
            }
        }

        if (!found) {
            listModel.addElement(updatedStatus);
            System.out.println("추가됨: " + updatedStatus);
        }
    }

    public void setServerStatus(boolean online) {
        if (online) {
            serverStatusLabel.setText("Server : ONLINE");
            serverStatusLabel.setForeground(Color.BLUE);  // dark green
        } else {
            serverStatusLabel.setText("Server : OFFLINE");
            serverStatusLabel.setForeground(Color.RED);
        }
    }
}

class gui_login extends JDialog {
    private final JTextField idField = new JTextField();
    private final JPasswordField pwField = new JPasswordField();

    public gui_login(Frame owner) {
        super(owner, "Login", true);
        setResizable(false);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {//x 누르면 오류 메세지 없이 바로 종료
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // 좌측 배너 (텍스트가 ID/PW 입력창 높이에 맞춰 자동 정렬 + 하단 그림)
        BannerPanel banner = new BannerPanel(idField, pwField);

        // 우측 폼 + 버전 표시
        JPanel formAndVersion = new JPanel(new BorderLayout());
        formAndVersion.add(createFormPanel(), BorderLayout.CENTER);

        JLabel ver = new JLabel("v" + clnt_main.version);
        ver.setFont(new Font("SANS_SERIF", Font.PLAIN, 12));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.add(ver);
        formAndVersion.add(bottom, BorderLayout.SOUTH);

        // 좌·우 분할
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                banner,
                formAndVersion
        );
        split.setDividerSize(0);
        split.setEnabled(false);
        split.setDividerLocation(250);
        getContentPane().add(split);

        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel createFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;

        // 1) ID 레이블 (아래 여백 2px)
        gbc.gridy = 0;
        gbc.insets = new Insets(100, 15, 2, 15);
        p.add(new JLabel("ID"), gbc);

        // 2) ID 입력창 (위 여백 2px)
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 15, 8, 15);
        idField.setPreferredSize(new Dimension(200, 25));
        p.add(idField, gbc);

        // 3) PW 레이블
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(8, 15, 2, 15);
        p.add(new JLabel("PW"), gbc);

        // 4) PW 입력창
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 15, 8, 15);
        pwField.setPreferredSize(new Dimension(200, 25));
        p.add(pwField, gbc);

        // 5) Login 버튼 (폭 100px, 우측 정렬)
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(8, 15, 8, 15);
        JButton btn = new JButton("Login");
        btn.setPreferredSize(new Dimension(100, btn.getPreferredSize().height));
        btn.addActionListener(e -> verifyInput());
        getRootPane().setDefaultButton(btn);
        p.add(btn, gbc);

        // 6) 아래 빈 공간으로 위쪽에 몰기
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        p.add(Box.createVerticalGlue(), gbc);

        return p;
    }

    private void verifyInput() {
        String id = idField.getText().trim();
        String pw = new String(pwField.getPassword());
        if (!id.isEmpty() && !pw.isEmpty()) {
            dispose();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "ID 또는 PW가 올바르지 않습니다.",
                    "로그인 오류",
                    JOptionPane.ERROR_MESSAGE
            );
            pwField.setText("");
        }
    }

    public String getUsername() {
        return idField.getText();
    }

    public String getPassword() {
        return new String(pwField.getPassword());
    }

    /**
     * 왼쪽 배너: idField/pwField 위치에 맞춰 JAVACHAT/CLIENT 그리기
     */
    private static class BannerPanel extends JPanel {
        private final JTextField idField;
        private final JPasswordField pwField;
        private final Font font1 = new Font(Font.SANS_SERIF, Font.BOLD, 40);
        private final Font font2 = new Font(Font.SANS_SERIF, Font.PLAIN, 20);

        BannerPanel(JTextField idField, JPasswordField pwField) {
            this.idField = idField;
            this.pwField = pwField;
            setBackground(new Color(0, 95, 219));
            setPreferredSize(new Dimension(250, 400));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();

            // JAVACHAT
            g2.setFont(font1);
            String s1 = "JAVACHAT";
            int w1 = fm.stringWidth(s1);
            Point pId = SwingUtilities.convertPoint(
                    idField.getParent(), idField.getX(), idField.getY(), this
            );
            int y1 = pId.y + idField.getHeight() / 2 + fm.getAscent() / 2;
            int x1 = getWidth() - 160 - w1;
            g2.drawString(s1, x1, y1);

            // CLIENT
            g2.setFont(font2);
            String s2 = "CLIENT";
            int w2 = fm.stringWidth(s2);
            Point pPw = SwingUtilities.convertPoint(
                    pwField.getParent(), pwField.getX(), pwField.getY(), this
            );
            int y2 = pPw.y + pwField.getHeight() / 2 + fm.getAscent() / 2;
            int x2 = getWidth() - 50 - w2;
            g2.drawString(s2, x2, y2);
        }
    }
}
