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

    @RequestMapping(value = "/ws")
    public String tows(){
        return "/ws";
    }

    //转到公告推送，此处只用注解，不使用注解请参考@MessageMapping("/chat1")
    @MessageMapping("/welcome")
    @SendTo("/topic/getResponse")       //指明发布路径
    public ResponseMessage say(RequestMessage message) {
        System.out.println(message.getName());
        return new ResponseMessage("welcome," + message.getName() + " !");
    }

//  转到相关的HTML界面
    @RequestMapping(value = "/login")
    public String toLogin(){
        return "/login";
    }
    @RequestMapping(value = "/chat")
    public String toChat(){
        return "/chat";
    }
    @RequestMapping(value = "/client")
    public String toClient(){
        return "/client";
    }

//  转到消息推送，对应前端中的stomp.send("/chat1",{},text)，此处不适用注解，使用注解请参考@MessageMapping("/welcome")下的具体代码，发布路径指定为 @SendToUser("/queue/notifications")
    @MessageMapping("/chat1")
//    可以直接在参数中获取Principal，Principal中包含有当前用户的用户名。
    public void handleChat(Principal principal, String msg) {
        List<String> list = new ArrayList<String>();
        list.add("222");
        list.add("333");
        list.add("444");
        list.add("555");
        if (principal.getName().equals("111")) {
            for(String s:list)
                messagingTemplate.convertAndSendToUser(s, "/queue/notifications", principal.getName() + "给您发来了消息：" + msg);
        }else{
            //编程实现消息推送,点对点使用convertAndSendToUser，广播使用convertAndSendTo
            messagingTemplate.convertAndSendToUser("111", "/queue/notifications", principal.getName() + "给您发来了消息：" + msg);
        }
    }




    //============================================================//
    @RequestMapping("getSession")
    @ResponseBody
    public String getSession(HttpSession session){
        System.out.println(session.getId());
        return "index";
    }

    @RequestMapping("index")
    public String index(HttpSession session){
        System.out.println(session.getId());
        return "index";
    }


    @MessageMapping(value="/change-notice")
    //@SendToUser(value="/topic/notice")
    //@SendTo(value="/topic/notice")
    public void greeting(SimpMessageHeaderAccessor headerAccessor, String value){
        System.out.println(headerAccessor.getSessionAttributes());
        Map<String,Object> map = headerAccessor.getSessionAttributes();
        System.out.println("HTTP_SESSION_ID:"+map.get("HTTP_SESSION_ID"));
        System.out.println("session:"+headerAccessor.getSessionId()+"		value:"+value);
        this.messagingTemplate.convertAndSend("/topic/notice", value);
        //return value;
    }





}

