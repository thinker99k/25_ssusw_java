package javachat_clnt;
import javax.swing.*;
import static java.lang.Integer.parseInt;

public class clnt_main {
    static bridge b;
    static String version = "1.0.2";
    public static void main(String[] args){
        b = new bridge(args[0], parseInt(args[1]), args[2], args[3]);

        SwingUtilities.invokeLater(b::exec);
    }
}
