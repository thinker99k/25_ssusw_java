package javachat_serv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

class AuthServer {
    private final Map<String,String> credentials = new HashMap<>();

    AuthServer(String filename) throws IOException{
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;               // skip blank lines
                String[] parts = line.split("\\s+", 2);     // split on first whitespace
                if (parts.length == 2) {
                    String id = parts[0];
                    String pw = parts[1];
                    credentials.put(id, pw);
                } else {
                    System.err.println("Malformed line: " + line);
                }
            }
        }
    }

    public boolean authenticate(String id, String pw) {
        String stored = credentials.get(id);
        return stored != null && stored.equals(pw);
    }
}
