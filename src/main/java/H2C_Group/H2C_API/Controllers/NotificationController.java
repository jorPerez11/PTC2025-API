package H2C_Group.H2C_API.Controllers;

import org.apache.logging.log4j.message.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {
    @MessageMapping("/notification")
    public String sendMessage(String message) {
        System.out.println("message:" + message);
        return message;
    }
}
