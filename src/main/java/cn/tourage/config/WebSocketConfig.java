package cn.tourage.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

@Configuration
@EnableWebSocketMessageBroker  //开启使用STOMP协议来传输基于代理的消息
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
    @Override  //注册STOMP协议的节点，并指定映射的URL
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        //这一行代码用来注册STOMP协议节点，同时指定使用SockJS协议。
        stompEndpointRegistry.addEndpoint("/endpointSang").withSockJS();
        stompEndpointRegistry.addEndpoint("/endpointChat").withSockJS();
        stompEndpointRegistry.addEndpoint("/socket").withSockJS()
                .setInterceptors( new ChatHandlerShareInterceptor())
                .setStreamBytesLimit(512 * 1024)
                .setHttpMessageCacheSize(1000)
                .setDisconnectDelay(30 * 1000);
    }

    @Override   //方法用来配置消息代理
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue","/topic");
        registry.setApplicationDestinationPrefixes("/app");
        //registry.setPathMatcher(new AntPathMatcher("."));
    }

    @Override
    public void configureWebSocketTransport(final WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {
            @Override
            public WebSocketHandler decorate(final WebSocketHandler handler) {
                return new WebSocketHandlerDecorator(handler) {
                    @Override
                    public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
                        // 客户端与服务器端建立连接后，此处记录谁上线了
                        String username = session.getPrincipal().getName();
                        //log.info("online: " + username);
                        System.out.println("online: " + username);
                        super.afterConnectionEstablished(session);
                    }

                    @Override
                    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                        // 客户端与服务器端断开连接后，此处记录谁下线了
                        String username = session.getPrincipal().getName();
                       // log.info("offline: " + username);
                        System.out.println("offline: " + username);
                        super.afterConnectionClosed(session, closeStatus);
                    }
                };
            }
        });
        super.configureWebSocketTransport(registration);
    }
}