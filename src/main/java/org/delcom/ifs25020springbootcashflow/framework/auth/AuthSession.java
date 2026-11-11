package org.delcom.ifs25020springbootcashflow.framework.auth;

import org.delcom.ifs25020springbootcashflow.domain.entity.User;
import org.delcom.ifs25020springbootcashflow.domain.repository.IUserRepository;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import jakarta.servlet.http.Cookie;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Sesi login: VaadinSession + HTTP session + cookie (cashflow_uid) agar sesi bertahan setelah refresh.
 * LOGGED_OUT_KEY mencegah restore dari cookie lama setelah logout.
 */
public final class AuthSession {
    public static final String USER_ID_KEY = "cashflow.userId";
    public static final String LOGGED_OUT_KEY = "cashflow.loggedOut";
    public static final String COOKIE_NAME = "cashflow_uid";
    private static final int COOKIE_MAX_AGE = 8 * 60 * 60;
    private static final byte[] SECRET = new byte[32];

    static {
        new SecureRandom().nextBytes(SECRET);
    }

    private AuthSession() {
    }

    public static void login(User user) {
        VaadinSession vs = VaadinSession.getCurrent();
        vs.setAttribute(LOGGED_OUT_KEY, null);
        vs.setAttribute(USER_ID_KEY, user.getId());
        WrappedSession http = vs.getSession();
        if (http != null) {
            http.removeAttribute(LOGGED_OUT_KEY);
            http.setAttribute(USER_ID_KEY, user.getId());
        }
        VaadinServletResponse response = VaadinServletResponse.getCurrent();
        if (response != null) {
            response.addCookie(cookie(sign(user.getId()), COOKIE_MAX_AGE));
        }
    }

    public static void logout() {
        VaadinSession vs = VaadinSession.getCurrent();
        vs.setAttribute(USER_ID_KEY, null);
        vs.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        WrappedSession http = vs.getSession();
        if (http != null) {
            http.removeAttribute(USER_ID_KEY);
            http.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        }
        VaadinServletResponse response = VaadinServletResponse.getCurrent();
        if (response != null) {
            response.addCookie(cookie("", 0));
        }
    }

    /** User yang sedang login, atau null. Memulihkan sesi setelah refresh. */
    public static User currentUser(IUserRepository users) {
        VaadinSession vs = VaadinSession.getCurrent();
        if (vs == null) return null;
        WrappedSession http = vs.getSession();
        if (vs.getAttribute(LOGGED_OUT_KEY) != null
                || (http != null && http.getAttribute(LOGGED_OUT_KEY) != null)) {
            return null;
        }
        Integer id = (Integer) vs.getAttribute(USER_ID_KEY);
        if (id == null && http != null) {
            id = (Integer) http.getAttribute(USER_ID_KEY);
        }
        if (id == null) {
            id = idFromCookie();
        }
        if (id == null) return null;
        Optional<User> user = users.findById(id);
        if (user.isEmpty()) return null;
        vs.setAttribute(USER_ID_KEY, id);
        if (http != null) http.setAttribute(USER_ID_KEY, id);
        return user.get();
    }

    private static Integer idFromCookie() {
        VaadinServletRequest request = VaadinServletRequest.getCurrent();
        if (request == null || request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (COOKIE_NAME.equals(c.getName())) {
                return verify(c.getValue());
            }
        }
        return null;
    }

    private static Cookie cookie(String value, int maxAge) {
        Cookie c = new Cookie(COOKIE_NAME, value);
        c.setPath("/");
        c.setHttpOnly(true);
        c.setMaxAge(maxAge);
        return c;
    }

    /** Cookie ditandatangani HMAC agar id user tidak bisa dipalsukan. */
    static String sign(int id) {
        return id + "." + hmac(Integer.toString(id));
    }

    static Integer verify(String value) {
        if (value == null) return null;
        int dot = value.indexOf('.');
        if (dot <= 0) return null;
        try {
            String idPart = value.substring(0, dot);
            String expected = hmac(idPart);
            boolean ok = MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    value.substring(dot + 1).getBytes(StandardCharsets.UTF_8));
            return ok ? Integer.valueOf(idPart) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
