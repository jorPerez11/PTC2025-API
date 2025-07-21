package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.CompanyEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Enums.Category;
import H2C_Group.H2C_API.Enums.UserRole;
import H2C_Group.H2C_API.Exceptions.UserExceptions;
import H2C_Group.H2C_API.Models.DTO.CategoryDTO;
import H2C_Group.H2C_API.Models.DTO.UserDTO;
import H2C_Group.H2C_API.Models.DTO.RolDTO;
import H2C_Group.H2C_API.Repositories.CompanyRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;



import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;


    @Autowired
    private Argon2PasswordEncoder argon2PasswordEncoder;



    public List<UserDTO> findAll() {
        List<UserEntity> usuarios = userRepository.findAll();
        return usuarios.stream().map(this::convertToUserDTO).collect(Collectors.toList());
    }


    public UserDTO registerNewUser(UserDTO dto) {

        //Validaciones de entrada
        //Busca en el userRepository si existe algun registro en la DB que repita el email / usuario / telefono a registrar
        userRepository.findByEmail(dto.getEmail()).ifPresent(user -> {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        });

        userRepository.findByUsername(dto.getUsername()).ifPresent(user-> {
            throw new IllegalArgumentException(("El usuario ya esta registrado."));
        });

        userRepository.findByPhone(dto.getPhone()).ifPresent(user -> {
            throw new IllegalArgumentException(("El número ya está registrado"));
        });



        //Obtener el primer id de tbCompanies
        Long firstCompanyId = companyRepository.findFirstCompanyId().orElseThrow(() -> new IllegalArgumentException("La compañia no existe."));

        UserEntity userEntity = new UserEntity();

        //Asignacion de rol a usuario. Por defecto, al crearlo sera "Cliente" (Se debera actualizar si el usuario es un tecnico)
        long userCount = userRepository.count();

        if (userCount == 0) {
            // Si no hay usuarios, este es el primer registro, asigna el rol de Administrador
            userEntity.setRolId(UserRole.ADMINISTRADOR.getId());
        } else {
            // Si ya hay usuarios, asigna el rol de Cliente
            userEntity.setRolId(UserRole.CLIENTE.getId());
        }

        //ASIGNACION DE PRIMER ID DE COMPANIA ENCONTRADA (DESDE companyRepository) A USUARIO
        Long foundCompanyId = companyRepository.findFirstCompanyId()
                .orElseThrow(() -> new IllegalStateException("No se puede registrar el usuario: No hay compañías registradas."));

        CompanyEntity companyToAssign = companyRepository.findById(foundCompanyId)
                .orElseThrow(() -> new IllegalStateException("La primera compañía (ID: " + firstCompanyId + ") no fue encontrada al intentar asignarla."));

        userEntity.setCompany(companyToAssign);


        if (dto.getCategory() != null) {
            userEntity.setCategoryId(dto.getCategory().getId());
        } else {
            userEntity.setCategoryId(null); // Establece explicitamente a null si no se proporciona
        }

        userEntity.setFullName(dto.getName());
        userEntity.setUsername(dto.getUsername());
        userEntity.setEmail(dto.getEmail());
        userEntity.setPhone(dto.getPhone());
        String hashedPassword = argon2PasswordEncoder.encode(dto.getPassword()); //IMPORTANTE: REQUERIDO HASHEAR ANTES DE INSERTAR A LA DB
        userEntity.setPasswordHash(hashedPassword);
        userEntity.setIsActive(dto.getIsActive());

        //Guarda el usuario registrado en la DB
        UserEntity savedUser = userRepository.save(userEntity);

        return convertToUserDTO(savedUser);

    }

    //METODO DE ACTUALIZACION DE CATEGORIA DE USUARIO (TECNICOS)
    public UserDTO updateUser(Long id, UserDTO dto) {

        //Validaciones
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del usuario a actualizar no puede ser nulo o no válido.");
        }

        UserEntity existingUser = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("El usuario con id" + id + " no existe"));

        //Primera operacion: Actualizar categoria (NIVEL DE ACCESO: 3 // [ADMIN] -> [TECNICO] )
        if (dto.getCategory() != null) { //Verifica si existe un valor (id) del atributo "category" enviado por el usuario (ADMIN)
            if (dto.getCategory().getId() == null) {//Verifica si el valor (id) enviado por el usuario (ADMIN) existe en el registro de CategoryDTO
                throw new IllegalArgumentException("Para actualizar la categoría, debe proporcionar un 'id' dentro de la enumeracion en 'Category'");
            }

            //Verifica si existe un id con la categoria indicada
            Category category = Category.fromId(dto.getCategory().getId()).orElseThrow(() -> new IllegalArgumentException("La categoria de id " + dto.getCategory().getId() + "  no existe."));

            //Verifica si el usuario es un Tecnico
            if (existingUser.getRolId().equals(UserRole.TECNICO.getId())) {
                existingUser.setCategoryId(category.getId());
                Optional<Category> optionalCategory = Category.fromId(dto.getCategory().getId()); //Se asignara la categoria segun el id proporcionado por el usuario (ADMIN)
                if (optionalCategory.isEmpty()) { //Si no existe (el registro en la lista esta vacio), se avisara que esa categoria asociada con su id no existe
                    throw new IllegalArgumentException("La categoría con ID " + dto.getCategory().getId() + " no existe.");
                }
                category = optionalCategory.get();
            }else{
                throw new IllegalArgumentException("Solo los técnicos pueden tener una categoría asignada. El usuario " + existingUser.getFullName() + " no es técnico.");
            }

        }else {
            existingUser.setCategoryId(null);

        }


        //Segunda operacion: Actualizar campos para cada usuario (NIVEL DE ACCESO: 1 // CONFIGURACION DE USUARIO [CLIENTE/TECNICO/ADMIN] -> [CLIENTE/TECNICO/ADMIN] )
        //Para actualizar los datos, se valida que existan datos en el registro
        //Si no se llega a actualizar todos los campos, se dejaran con el valor existente en su registro
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            existingUser.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            existingUser.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            existingUser.setPhone(dto.getPhone());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) { // O dto.getPasswordHash()
            String hashedPassword = argon2PasswordEncoder.encode(dto.getPassword());
            existingUser.setPasswordHash(hashedPassword); // O setPasswordHash()
        }
        //Actualizacion de rol [PARCIAL PARA PRUEBA] para usuarios (NIVEL DE ACCESO: 3 // CAMBIO DE ROL DE USUARIO [ADMIN] -> [TECNICO])
        //Vallidacion: Un usuario que sea admin (rol != admin) no puede efectuar el cambio de rol en otro usuario admin
        if (dto.getRol() != null) {
            if (dto.getRol().getId() == null) { // Asegura que el ID del rol se envió
                throw new IllegalArgumentException("Para actualizar el rol, debe proporcionar un 'id' dentro del objeto 'rol'");
            }

            // Busca el UserRole Enum a partir del ID proporcionado en el DTO
            UserRole newRole = UserRole.fromId(dto.getRol().getId())
                    .orElseThrow(() -> new IllegalArgumentException("El rol con ID " + dto.getRol().getId() + " no existe."));

            // Tu validación existente sobre no poder cambiar a ADMINISTRADOR
            if (newRole.equals(UserRole.ADMINISTRADOR)) {
                throw new IllegalArgumentException("No se puede cambiar el rol a ADMINISTRADOR con esta operación.");
            }

            existingUser.setRolId(newRole.getId());
        }

        UserEntity savedUser = userRepository.save(existingUser);
        return convertToUserDTO(savedUser);
    }


    public void deleteUser(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo o no válido");
        }

        boolean exists = userRepository.existsById(id);

        if (!exists) {
            throw new UserExceptions.UserNotFoundException("Usuario con ID " + id + " no encontrado.");
        }

        userRepository.deleteById(id);
    }


    //Manda datos del usuario. Convierte de UserEntity a DTOUser
    private UserDTO convertToUserDTO(UserEntity usuario) {
        UserDTO dto = new UserDTO();
        dto.setId(usuario.getUserId());
        UserRole userRoleEnum = UserRole.fromId(usuario.getRolId()).orElseThrow(() -> new IllegalArgumentException("ID de Rol de usuario inválido en la entidad: " + usuario.getRolId()));
        dto.setRol(new RolDTO(userRoleEnum));

        if (usuario.getCategoryId() != null) {
            Category categoryEnum = Category.fromId(usuario.getCategoryId()).orElseThrow(() -> new IllegalArgumentException("ID de Categoría inválido en la entidad para el usuario: " + usuario.getUserId() + " con categoryId: " + usuario.getCategoryId()));
            // Creacion de CategoryDTO para la respuesta, incluyendo ambos id y displayName
            dto.setCategory(new CategoryDTO(categoryEnum.getId(), categoryEnum.getDisplayName()));
        } else {
            dto.setCategory(null);
        }

        if (usuario.getCompany() != null) { // userEntity.getCompany() devuelve un CompanyEntity
            dto.setCompanyId(usuario.getCompany().getCompanyId()); // userEntity.getCompany().getCompanyId() devuelve el Long ID
        }
        dto.setName(usuario.getFullName());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setPhone(usuario.getPhone());
        dto.setIsActive(usuario.getIsActive());
        return dto;
    }

    public List<UserDTO> getAllUsers() {
        return findAll();
    }

    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return argon2PasswordEncoder.matches(rawPassword, hashedPassword);
    }

    //Encontrar usuario por su username. -- Su uso es importante para la actualizacion de datos de cada usuario, en configuracion
    public Long getUserIdByUsername(String username) {
        return userRepository.findByUsername(username).map(UserEntity::getUserId).orElseThrow(() -> new IllegalArgumentException("El usuario " + username + " no existe"));
    }





}

