package javachat_clnt;

import javax.swing.*;
import java.awt.*;

/** 로그인용 다이얼로그 */
public class LoginDialog extends JDialog {
    private final JTextField     idField  = new JTextField(12);
    private final JPasswordField pwField  = new JPasswordField(12);
    private boolean succeeded;
    private String  username;

    public LoginDialog(Frame owner) {
        super(owner, "Login", true);
        setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridLayout(2,2,4,4));
        form.add(new JLabel("ID:",   SwingConstants.RIGHT)); form.add(idField);
        form.add(new JLabel("PW:",   SwingConstants.RIGHT)); form.add(pwField);
        add(form, BorderLayout.CENTER);

        JButton btn = new JButton("Login");
        add(btn, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btn);

        btn.addActionListener(e -> tryLogin());

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void tryLogin() {
        String id = idField.getText().trim();
        String pw = new String(pwField.getPassword());
        if (CredentialDB.verify(id, pw)) {
            succeeded = true;
            username  = id;
            dispose();
        } else {
            JOptionPane.showMessageDialog(
                    this, "잘못된 자격입니다.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            pwField.setText("");
        }
    }

    public boolean isSucceeded() { return succeeded; }
    public String  getUsername() { return username;  }
}

