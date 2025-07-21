package H2C_Group.H2C_API.Enums;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

public enum TicketPriority {
    BAJA(1L, "Baja"),
    MEDIA(2L, "Media"),
    ALTA(3L, "Alta"),
    CRITICA(4L, "Crítica");

    private final Long id;
    private final String displayName;

    TicketPriority(Long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    public static Optional<TicketPriority> fromIdOptional(Long id) {
        if (id == null) return Optional.empty();
        for (TicketPriority priority : TicketPriority.values()) {
            if (priority.getId().equals(id)) {
                return Optional.of(priority);
            }
        }
        return Optional.empty();
    }

    @JsonCreator
    public static TicketPriority fromDisplayName(String displayName) {
        return Arrays.stream(TicketPriority.values())
                .filter(priority -> priority.getDisplayName().equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Prioridad '" + displayName + "' no existe en la enumeración"));
    }


    public static Optional<TicketPriority> fromId(Long id) {
        return Arrays.stream(TicketPriority.values())
                .filter(priority -> priority.getId().equals(id))
                .findFirst();
    }



}
