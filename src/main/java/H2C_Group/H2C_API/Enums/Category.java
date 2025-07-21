package H2C_Group.H2C_API.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.Optional;

public enum Category {
    SOPORTE_TECNICO(1L, "Soporte Técnico"),
    CONSULTAS(2L, "Consultas"),
    GESTION_DE_USUARIOS(3L, "Gestión de Usuarios"),
    REDES(4L, "Redes"),
    INCIDENTES_CRITICOS(5L, "Incidentes Críticos");

    private final Long id;
    private final String displayName;

    Category(Long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public static Optional<Category> fromIdOptional(Long id) {
        if (id == null) return Optional.empty();
        for (Category cat : Category.values()) {
            if (cat.getId().equals(id)) {
                return Optional.of(cat);
            }
        }
        return Optional.empty();
    }

    public static Optional<Category> fromNameOptional(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty(); // No buscar con nombre nulo o vacío
        }
        for (Category cat : Category.values()) {
            // Compara ignorando mayúsculas/minúsculas para una búsqueda más robusta
            if (cat.getDisplayName().equalsIgnoreCase(name.trim())) {
                return Optional.of(cat);
            }
        }
        return Optional.empty(); // Si no se encuentra ningún Enum con ese nombre
    }

    public Long getId() {
        return id;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static Category fromDisplayName(String displayName) {
        return Arrays.stream(Category.values())
                .filter(category -> category.getDisplayName().equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Categoría '" + displayName + "' no existe en la enumeración."));
    }


    public static Optional<Category> fromId(Long id) {
        return Arrays.stream(Category.values())
                .filter(category -> category.getId().equals(id))
                .findFirst();
    }

}
