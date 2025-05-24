package javachat_clnt;

import java.util.HashMap;
import java.util.Map;

/** 하드코딩 자격 DB – 필요하면 여기에 계정을 추가 */
public final class CredentialDB {
    private static final Map<String, String> USERS = new HashMap<>();
    static {
        USERS.put("sumin",   "sumin1234");
        USERS.put("minsu",     "minsu1234");
        USERS.put("jingi", "jingi1234");
    }

    private CredentialDB() { }

    /** 아이디/비밀번호가 유효하면 true */
    public static boolean verify(String id, String pw) {
        return pw.equals(USERS.get(id));
    }
}
