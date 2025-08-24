package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Enums.Category;
import H2C_Group.H2C_API.Enums.TicketPriority;
import H2C_Group.H2C_API.Models.DTO.CategoryDTO;
import H2C_Group.H2C_API.Models.DTO.TicketPriorityDTO;
import org.apache.hc.core5.reactor.Command;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PriorityService {
    public List<TicketPriorityDTO> getAllPriorities(){
        return Arrays.stream(TicketPriority.values())
                .map(priority -> new TicketPriorityDTO(priority.getId(), priority.getDisplayName()))
                .collect(Collectors.toList());
    }

}
