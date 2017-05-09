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

    //注册STOMP协议的节点，并指定映射的URL
    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        /**
         * 注册STOMP协议节点，同时指定使用SockJS协议，在前端界面中会使用该处的节点进行SocKet连接
         * 根据需要注册即可，这里因为只是测试，注册了3个节点
         * 另外，在第三个节点中，可以看到比前两个节点多了一些内容，制定了一个拦截器及其他信息，该拦截器在demo中主要用在点对点模块
         * 这个根据需要来扩展，拦截器是定义的一个class文件，因本人也是初次接触这部分内容，不太了解。
         */
        stompEndpointRegistry.addEndpoint("/endpointSang").withSockJS();
        stompEndpointRegistry.addEndpoint("/endpointChat").withSockJS();
        stompEndpointRegistry.addEndpoint("/socket").withSockJS()
                .setInterceptors( new ChatHandlerShareInterceptor())
                .setStreamBytesLimit(512 * 1024)
                .setHttpMessageCacheSize(1000)
                .setDisconnectDelay(30 * 1000);
    }

    //配置消息代理
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        /**
         * 配置消息代理，前端通过这个代理来进行消息订阅,
         * 消息代理可以有一个，也可以有多个，用 “，” 号分隔
         * 这里配置了两个，"/topic"用作全局推送，"/queue"用做点对点使用
         */
        registry.enableSimpleBroker("/queue","/topic");
        /**
         * 配置接收前端信息的消息代理，前端通过这个代理来向后台传递消息
         * 简单来说，前端通过这个消息代理访问对应controller中的MessageMapping(value)
         */
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