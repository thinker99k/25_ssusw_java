package javachat_clnt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class gui extends JFrame { //클래스 자체가 하나의 윈도우가 되도록 JFrame 상속

    private static final int SIDE_WIDTH = 100; // 사용자 목록과 placeholder 폭 고정

    private JTextArea chatArea;//채팅창 영역
    private JTextField inputField;// 메세지 입력창 영역
    private JButton sendButton; //전송 영역
    private JList<String> userList; //사용자 목록 나열 리스트
    private DefaultListModel<String> listModel;//userlist 의 데이터 모델, 사용자에 변화가 있을 시 갱신된다.
    private JPanel placeholderPanel; // 우측 하단 빈공간 영역

    gui() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // x 버튼 누르면 프로세스 종료
        setSize(1000, 800); // 윈도우 창 사이즈 고정
        setResizable(false);                // 사이즈를 바꿀 수 없도록 설정
        setLocationRelativeTo(null);        // 화면 정중앙에 프레임을 띄움
        initComponents();                   //내부 컴포넌트 생성 및 배치하는 메서드 호출
    }

    private void initComponents() {

        // 1) 전송 버튼 생성 및 크기 계산
        sendButton = new JButton("전송");
        Dimension btnDim = sendButton.getPreferredSize();//버튼의 기본 크기를 가져옴
        sendButton.setPreferredSize(new Dimension(SIDE_WIDTH, btnDim.height)); // 폭 고정

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

    public void updateStatus(String name, Boolean status) {
        String updatedStatus = name + " - " + (status ? "Online" : "Offline");
        boolean found = false;

        for (int i = 0; i < listModel.size(); i++) {
            String userEntry = listModel.getElementAt(i).trim();
            String[] parts = userEntry.split(" - ", 2);

            if (parts.length > 0 && parts[0].trim().equals(name)) {
                if (!userEntry.equals(updatedStatus)) {
                    listModel.set(i, updatedStatus);
                    System.out.println("업데이트됨: " + updatedStatus);
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
}
