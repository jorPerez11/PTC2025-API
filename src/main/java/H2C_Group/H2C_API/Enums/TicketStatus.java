package H2C_Group.H2C_API.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

public enum TicketStatus {
    EN_ESPERA(1L, "En espera"),
    EN_PROGRESO(2L, "En progreso"),
    COMPLETADO(3L, "Completado");

    private final Long id;
    private final String displayName;

    TicketStatus(Long id, String displayName) {
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

    public static Optional<TicketStatus> fromIdOptional(Long id) {
        if (id == null) return Optional.empty();
        for (TicketStatus status : TicketStatus.values()) {
            if (status.getId().equals(id)) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }


    @JsonCreator
    public static TicketStatus fromDisplayName(String displayName) {
        return Arrays.stream(TicketStatus.values())
                .filter(status -> status.getDisplayName().equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado '" + displayName + "' no existe en la enumeración"));
    }

    public static Optional<TicketStatus> fromId(Long id) {
        return Arrays.stream(TicketStatus.values())
                .filter(priority -> priority.getId().equals(id))
                .findFirst();
    }

}
