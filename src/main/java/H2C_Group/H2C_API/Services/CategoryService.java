package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Entities.CategoryEntity;
import H2C_Group.H2C_API.Exceptions.ExceptionCategoryBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionCategoryNotFound;
import H2C_Group.H2C_API.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private ResponseTemplateRepository responseTemplatesRepository;

    public void deleteCategory(Long id) {
        // 1. Verifica si la categoría que quieres borrar existe
        CategoryEntity category = (CategoryEntity) categoryRepository.findById(id)
                .orElseThrow(() -> new ExceptionCategoryNotFound("La categoría con ID " + id + " no existe."));

        // 2. Comprueba si la categoría está en uso en alguna tabla
        boolean isUsed = ticketRepository.existsByCategoryId(id) ||
                userRepository.existsByCategory_CategoryId(id) ||
                solutionRepository.existsByCategory_CategoryId(id) ||
                responseTemplatesRepository.existsByCategory_CategoryId(id);

        // 3. Si está en uso, lanza un error claro
        if (isUsed) {
            throw new ExceptionCategoryBadRequest("No se puede eliminar la categoría '" + category.getCategoryName() +
                    "' porque está en uso por tickets, usuarios, soluciones o plantillas.");
        }

        // 4. Si no está en uso, elimínala
        categoryRepository.deleteById(id);
    }
}
