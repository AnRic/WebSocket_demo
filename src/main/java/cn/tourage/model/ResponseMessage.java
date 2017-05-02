package cn.tourage.model;

/**
 * 保存服务器发给浏览器的信息
 */
public class ResponseMessage {

    private String responseMessage;

    public ResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

}
