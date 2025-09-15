package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Entities.Views.ClientTicketView;
import H2C_Group.H2C_API.Repositories.ClientTicketViewRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ClientViewService {

    @Autowired
    private ClientTicketViewRepository clientTicketViewRepository;

    public Page<ClientTicketView> getAllClients(Pageable pageable, String status, String period, String search) {
        Specification<ClientTicketView> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por estado del ticket
            if (status != null && !status.isEmpty() && !status.equals("all")) {
                predicates.add(criteriaBuilder.equal(root.get("estado_solicitud"), status));
            }

            // Filtro por período de registro (fecha_registro)
            if (period != null && !period.isEmpty() && !period.equals("all")) {
                Date now = new Date();
                Date startDate = null;
                switch (period) {
                    case "today":
                        // ** Lógica para 'hoy' **
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(now);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        startDate = calendar.getTime();
                        break;
                    case "week":
                        startDate = new Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000);
                        break;
                    case "month":
                        startDate = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);
                        break;
                }
                if (startDate != null) {
                    predicates.add(criteriaBuilder.between(root.get("fecha_registro"), startDate, now));
                }
            }

            // Filtro por término de búsqueda (nombre, email, id)
            if (search != null && !search.isEmpty()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nombre_cliente")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("userId").as(String.class)), likePattern)
                );
                predicates.add(searchPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return clientTicketViewRepository.findAll(spec, pageable);
    }
}