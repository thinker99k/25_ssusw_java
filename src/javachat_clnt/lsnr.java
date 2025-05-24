package javachat_clnt;

import java.util.concurrent.ConcurrentHashMap;

public interface lsnr {
    void onIncoming(String line);
    void onStatusUpdate(String name, Boolean status);
}