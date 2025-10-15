package H2C_Group.H2C_API.Utils;

import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Enums.TicketStatus;
import H2C_Group.H2C_API.Repositories.TicketRepository;
import H2C_Group.H2C_API.Services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledTasks {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TicketRepository ticketRepository;

    /**
     * Tarea programada para revisar tickets estancados.
     * Se ejecuta todos los días a las 8:00 AM (0 0 8 * * *).
     * Revisa tickets en estado "En Progreso" que no han tenido actualización en 2 días.
     * * Nota: Recuerda poner @EnableScheduling en tu clase principal de Spring Boot.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void checkStaleTickets() {
        System.out.println("🚩 Ejecutando tarea programada: Revisando tickets estancados...");

        // 1. Obtener el ID del estado "En Progreso"
        Long enProgresoId = TicketStatus.EN_PROGRESO.getId();

        // 2. Buscar tickets estancados (asignados y con más de 2 días de antigüedad)
        List<TicketEntity> staleTickets = ticketRepository.findStaleTicketsForTechnicians(enProgresoId);

        if (staleTickets.isEmpty()) {
            System.out.println("✅ No se encontraron tickets estancados. Terminando tarea.");
            return;
        }

        System.out.println("⚠️ Se encontraron " + staleTickets.size() + " tickets estancados. Enviando notificaciones.");

        // 3. Iterar y enviar notificaciones a cada técnico asignado
        for (TicketEntity ticket : staleTickets) {
            if (ticket.getAssignedTechUser() != null) {
                String message = String.format(
                        "🚨 ¡ALERTA DE TICKET ESTANCADO! El ticket #%d (%s) ha estado en progreso sin actividad por 2 días. Por favor, revisa el avance.",
                        ticket.getTicketId(),
                        ticket.getTitle()
                );

                // Envía la notificación (persiste en DB y envía por WS)
                notificationService.createAndSendNotification(
                        ticket.getAssignedTechUser(),
                        ticket,
                        message
                );
            }
        }
        System.out.println("✅ Tarea programada completada.");
    }
}
