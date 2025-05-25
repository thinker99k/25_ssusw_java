package javachat_clnt;
import javax.swing.*;
import static java.lang.Integer.parseInt;

public class clnt_main {
    public static bridge b;
    public static String version = "1.1.2(rc2)";
    public static void main(String[] args){
        b = new bridge(args[0], parseInt(args[1]));

        SwingUtilities.invokeLater(b::exec);
    }
}
