package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Enums.Category;
import H2C_Group.H2C_API.Models.DTO.CategoryDTO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    public List<CategoryDTO> getAllCategories(){
        return Arrays.stream(Category.values())
                .map(category -> new CategoryDTO(category.getId(), category.getDisplayName()))
                .collect(Collectors.toList());
    }
}
