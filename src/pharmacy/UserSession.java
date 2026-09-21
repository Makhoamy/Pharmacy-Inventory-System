
package pharmacy;

public class UserSession {
    public static int userId;
    public static String username;
    public static String fullName;
    public static String role;

    public static void clear() {
        userId = 0;
        username = null;
        fullName = null;
        role = null;
    }
}
