package com.axiserp.inventory.application.shared;

public class RequestContext {
    private static final InheritableThreadLocal<String> IP_ADDRESS = new InheritableThreadLocal<>();
    private static final InheritableThreadLocal<String> USER_AGENT = new InheritableThreadLocal<>();

    public static String getIpAddress() { return IP_ADDRESS.get(); }
    public static void setIpAddress(String ip) { IP_ADDRESS.set(ip); }
    public static String getUserAgent() { return USER_AGENT.get(); }
    public static void setUserAgent(String ua) { USER_AGENT.set(ua); }
    public static void clear() { IP_ADDRESS.remove(); USER_AGENT.remove(); }
}
