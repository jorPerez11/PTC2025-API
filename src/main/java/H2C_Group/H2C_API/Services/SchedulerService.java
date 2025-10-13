package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchedulerService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private NotificationService notificationService;

    // Ejecuta la tarea cada 12 horas (43,200,000 milisegundos)
    // Se recomienda usar la sintaxis cron para producción.
    @Scheduled(fixedRate = 43200000)
    public void sendStaleTicketReminders() {
        List<TicketEntity> staleTickets = ticketRepository.findStalePendingTickets();

        if (!staleTickets.isEmpty()) {
            System.out.println("🔔 Ejecutando recordatorio para " + staleTickets.size() + " tickets estancados.");

            for (TicketEntity ticket : staleTickets) {
                // 1. Obtener la entidad del técnico
                UserEntity assignedTech = ticket.getAssignedTechUser();

                if (assignedTech != null) {
                    Long technicianId = assignedTech.getUserId(); // <-- Obtenemos el ID del técnico

                    String message = "🚨 RECORDATORIO: El ticket #" + ticket.getTicketId() + " (" + ticket.getTitle() + ") lleva mucho tiempo en espera.";

                    notificationService.sendNotification(
                            technicianId,
                            ticket,
                            message,
                            "REMINDER"
                    );
                }
            }
        }
    }
}
