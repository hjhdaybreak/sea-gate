package com.sea.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpUtil {
    public static String getLocalIpAddress() {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return address.getHostAddress();
    }
}
