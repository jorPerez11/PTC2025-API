package H2C_Group.H2C_API.Models.DTO;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class ResponseTemplateDTO {
    private Long templateId;

    @NotNull(message = "La categoria es obligatoria.")
    private CategoryDTO category;

    @NotBlank(message = "El titulo es obligatorio.")
    private String title;

    @NotBlank(message = "El contenido de la plantilla es obligatorio.")
    private String templateContent;

    private String keyWords;

}
