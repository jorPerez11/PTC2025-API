package H2C_Group.H2C_API.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

public enum TicketField {
    IMAGEN(1L, "Imagen"),
    VIDEO(2L, "Video"),
    PDF(3L, "PDF"),
    OTRO(4L, "Otro");

    private final Long id;
    private final String displayName;

    TicketField(Long id, String displayName) {
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

    @JsonCreator
    public static TicketField fromDisplayName(String displayName) {
        return Arrays.stream(TicketField.values())
                .filter(type -> type.getDisplayName().equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El tipo '" + displayName + "' no existe en la enumeración"));
    }


    public static Optional<TicketField> fromId(Long id) {
        return  Arrays.stream(TicketField.values())
                .filter(type -> type.getId().equals(id))
                .findFirst();
    }

}
