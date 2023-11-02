package com.vuong.app.util;

import javax.servlet.http.HttpServletRequest;

public class ServletHelper {
    public static String extractIp(HttpServletRequest request) {
        String clientIp;
        String clientXForwardedForIp = request
                .getHeader("x-forwarded-for");
        if (clientXForwardedForIp != null) {
            clientIp = parseXForwardedHeader(clientXForwardedForIp);
        } else {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }

    private static String parseXForwardedHeader(String header) {
        return header.split(" *, *")[0];
    }

    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("user-agent");
    }
}
