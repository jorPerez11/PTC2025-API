package H2C_Group.H2C_API.Enums;


//Enum: lista cerrada y predefinida de opciones
//Permite definir un conjunto fijo de valores (para tbRol) con nombres constantes

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue; //<-- Jackson es el JSON que utiliza Spring

import java.util.Arrays;
import java.util.Optional;

public enum UserRole {
    //Definicion de la lista dentro del Enum (roles de tbRol)
    CLIENTE(1L, "Cliente"), //1L representa el tipo de dato Long y el orden del rol dentro de la tabla tbRol
    TECNICO(2L, "Técnico"),
    ADMINISTRADOR(3L, "Administrador");

    private final Long id; // Nuevo campo para el ID numérico del rol

    private final String displayName; //Declaracion de variable para almacenar los items del Enum (SOLO PARA LEGIBILIDAD DE CODIGO, NO SE ALMACENA)

    //Declaracion de objeto UserRole, con parametros de item de lista Enum (this.displayName) con sus displayName (= displayname)
    UserRole(Long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    //Getter para el ID
    public Long getId() {
        return id;
    }

    @JsonValue //Conversion del Enum a String JSON cuando la API envia datos (DTO -> Entity -> API)
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator //Conversion de String JSON a Enum cuando se reciben datos de la API
    public static UserRole fromDisplayName(String displayName) {
        for (UserRole role : UserRole.values()) {
            if (role.getDisplayName().equals(displayName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Rol " +  displayName + " no existe en enumeracion");
    }

    public static Optional<UserRole> fromId(Long id) {
        return Arrays.stream(UserRole.values()).
                filter(role -> role.getId().equals(id)).
                findFirst();
    }
}
