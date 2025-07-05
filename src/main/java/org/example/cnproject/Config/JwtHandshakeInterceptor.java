package org.example.cnproject.Config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.example.cnproject.Service.JWTService;
import org.example.cnproject.Service.MyUserDetailsService;
import org.example.cnproject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private MyUserDetailsService  myUserDetailsService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if(request instanceof ServletServerHttpRequest serverHttpRequest){
            HttpServletRequest httpServletRequest = serverHttpRequest.getServletRequest();
            Cookie[] cookies = httpServletRequest.getCookies();
            if(cookies != null){
                for(Cookie cookie : cookies){
                    if("JWT".equals(cookie.getName())){
                        String token = cookie.getValue();
                        String username = jwtService.extractUserName(token);
                        UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
                        if(jwtService.validateToken(token, userDetails)){
                            attributes.put("username", username);
                            return true;
                        }
                    }
                }
            }
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
