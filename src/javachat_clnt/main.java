package javachat_clnt;
import javax.swing.*;
import static java.lang.Integer.parseInt;

public class main {
    private static net     n;
    private static bridge b;

    static String version = "1.0.2";
    public static void main(String[] args){
        b = new bridge(version);
        n = new net(args[0], parseInt(args[1]), args[2], b);
        b.bind(n);

        SwingUtilities.invokeLater(b::exec);
        n.exec();
    }
}
