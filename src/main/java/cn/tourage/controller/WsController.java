package cn.tourage.controller;

import cn.tourage.model.RequestMessage;
import cn.tourage.model.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 控制器
 */
@Controller
public class WsController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;   //实现向浏览器发送信息的功能

    //转到ws.html界面,ws是一个测试界面，既可以发布消息也可以接受推送
    @RequestMapping(value = "/ws")
    public String tows() {
        return "/ws";
    }

    /**
     * 进行公告推送，此处只用注解，不使用注解请参考点对点中的@MessageMapping("/chat1")对应具体方法
     *
     * @MessageMapping("/welcome") 对应ws.html中的stompClient.send("/app/welcome")
     * 多出来的“/app"是WebSocKetConfig中定义的,如不定义，则HTML中对应改为stompClient.send("/welcome")
     * @SendTo("/topic/getResponse") 指定订阅路径，对应HTML中的stompClient.subscribe('/topic/getResponse', ……)
     * 意味将信息推送给所有订阅了"/topic/getResponse"的用户
     */
    @MessageMapping("/welcome")
    @SendTo("/topic/getResponse")       //指明发布路径
    public ResponseMessage say(RequestMessage message) {
        System.out.println(message.getName());
        return new ResponseMessage("welcome," + message.getName() + " !");
    }

    //  转到相关的HTML界面
    @RequestMapping(value = "/login")
    public String toLogin() {
        return "/login";
    }

    @RequestMapping(value = "/chat")
    public String toChat() {
        return "/chat";
    }

    @RequestMapping(value = "/client")
    public String toClient() {
        return "/client";
    }

    /**
     * 转到消息推送，对应前端chat.html中的stomp.send("/chat1",{},text)
     * 此处未使用注解，而是使用代码编程来完成推送，目的是为了方便对批量（可理解为分组）推送进行扩展
     * 编程实现消息推送,点对点使用convertAndSendToUser，广播使用convertAndSendTo
     * 使用注解可参考全局推送里面的具体实现，注：点对点用SendToUser()来代替SendTo()
     */
    @MessageMapping("/chat1")
//    可以直接在参数中获取Principal，Principal中包含有当前用户的用户名。
    public void handleChat(Principal principal, String msg) {
        List<String> list = new ArrayList<String>();
        list.add("222");
        list.add("333");
        list.add("444");
        if (principal.getName().equals("111")) {
            for (String s : list)
            /**
             * 第一个参数：用户名；第二个参数：订阅路径；第三个参数：要推送的消息文本
             * 注：全局推送对应方法有两个参数，第一个参数：订阅路径；第二个参数：要推送的消息文本
             */
                messagingTemplate.convertAndSendToUser(s, "/queue/notifications", principal.getName() + "给您发来了消息：" + msg);
        } else {
            messagingTemplate.convertAndSendToUser("111", "/queue/notifications", principal.getName() + "给您发来了消息：" + msg);
        }
    }


    //============================================================//
    @RequestMapping("getSession")
    @ResponseBody
    public String getSession(HttpSession session) {
        System.out.println(session.getId());
        return "index";
    }

    @RequestMapping("index")
    public String index(HttpSession session) {
        System.out.println(session.getId());
        return "index";
    }


    @MessageMapping(value = "/change-notice")
    //@SendToUser(value="/topic/notice")
    //@SendTo(value="/topic/notice")
    public void greeting(SimpMessageHeaderAccessor headerAccessor, String value) {
        System.out.println(headerAccessor.getSessionAttributes());
        Map<String, Object> map = headerAccessor.getSessionAttributes();
        System.out.println("HTTP_SESSION_ID:" + map.get("HTTP_SESSION_ID"));
        System.out.println("session:" + headerAccessor.getSessionId() + "		value:" + value);
        this.messagingTemplate.convertAndSend("/topic/notice", value);
        //return value;
    }


}

