package H2C_Group.H2C_API.Models.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessageDTO {
    private Long notificationId;
    private Long ticketId;
    private String message;
    private String type;
    private LocalDateTime date;

    public NotificationMessageDTO(Long notificationId, Long ticketId, String message, String type) {
        this.notificationId = notificationId;
        this.ticketId = ticketId;
        this.message = message;
        this.type = type;
        this.date = LocalDateTime.now();
    }
}
