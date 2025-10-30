package com.xladmt.makify.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 클라이언트 IP를 추적하는 유틸리티 클래스
 * 프록시, 로드 밸런서 등을 고려한 IP 추출
 */
@Slf4j
@Component
public class IpTracer {

    /**
     * HttpServletRequest에서 클라이언트 IP 추출
     * 프록시를 통한 요청도 고려
     * 
     * 우선순위:
     * 1. X-Forwarded-For (프록시 환경)
     * 2. X-Real-IP (Nginx)
     * 3. Proxy-Client-IP
     * 4. WL-Proxy-Client-IP
     * 5. getRemoteAddr() (직접 연결)
     */
    public String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN";
        }

        // 1. X-Forwarded-For 헤더 확인 (프록시 환경에서 가장 일반적)
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp != null && !clientIp.isEmpty() && !"unknown".equalsIgnoreCase(clientIp)) {
            // X-Forwarded-For는 여러 개의 IP가 쉼표로 구분되어 있을 수 있음
            // 첫 번째 IP가 클라이언트의 실제 IP
            String[] ips = clientIp.split(",");
            return ips[0].trim();
        }

        // 2. X-Real-IP 헤더 확인 (Nginx)
        clientIp = request.getHeader("X-Real-IP");
        if (clientIp != null && !clientIp.isEmpty() && !"unknown".equalsIgnoreCase(clientIp)) {
            return clientIp;
        }

        // 3. Proxy-Client-IP 헤더 확인
        clientIp = request.getHeader("Proxy-Client-IP");
        if (clientIp != null && !clientIp.isEmpty() && !"unknown".equalsIgnoreCase(clientIp)) {
            return clientIp;
        }

        // 4. WL-Proxy-Client-IP 헤더 확인
        clientIp = request.getHeader("WL-Proxy-Client-IP");
        if (clientIp != null && !clientIp.isEmpty() && !"unknown".equalsIgnoreCase(clientIp)) {
            return clientIp;
        }

        // 5. CF-Connecting-IP 헤더 확인 (Cloudflare)
        clientIp = request.getHeader("CF-Connecting-IP");
        if (clientIp != null && !clientIp.isEmpty() && !"unknown".equalsIgnoreCase(clientIp)) {
            return clientIp;
        }

        // 6. 직접 연결 (프록시 없음)
        clientIp = request.getRemoteAddr();
        
        return clientIp != null ? clientIp : "UNKNOWN";
    }

    /**
     * IP 주소가 로컬 주소인지 확인
     */
    public boolean isLocalAddress(String ip) {
        return ip != null && (
            ip.equals("127.0.0.1") ||
            ip.equals("0:0:0:0:0:0:0:1") ||
            ip.startsWith("192.168.") ||
            ip.startsWith("10.") ||
            ip.startsWith("172.") ||
            ip.equals("localhost")
        );
    }

    /**
     * IP 주소 형식 검증
     */
    public boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // IPv4 패턴
        String ipv4Pattern = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
                             "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
        
        // IPv6 기본 패턴 (간단한 검증)
        String ipv6Pattern = "^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|" +
                             "([0-9a-fA-F]{1,4}:){1,7}:|" +
                             "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +
                             "::1|::)$";

        return ip.matches(ipv4Pattern) || ip.matches(ipv6Pattern);
    }
}
