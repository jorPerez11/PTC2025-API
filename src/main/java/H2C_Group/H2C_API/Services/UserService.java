package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.CategoryEntity;
import H2C_Group.H2C_API.Entities.CompanyEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Enums.Category;
import H2C_Group.H2C_API.Enums.UserRole;
import H2C_Group.H2C_API.Exceptions.ExceptionCategoryNotFound;
import H2C_Group.H2C_API.Exceptions.ExceptionUserNotFound;
import H2C_Group.H2C_API.Exceptions.ExceptionCategoryBadRequest;
import H2C_Group.H2C_API.Models.DTO.CategoryDTO;
import H2C_Group.H2C_API.Models.DTO.UserDTO;
import H2C_Group.H2C_API.Models.DTO.RolDTO;
import H2C_Group.H2C_API.Repositories.CategoryRepository;
import H2C_Group.H2C_API.Repositories.CompanyRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EntityManager entityManager;

    //Implementacion del metodo que requiere UserDetailsService si lo quito deja de funcionar :(. Spring Security lo usa para encontrar a un usuario por su nombre de usuario
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario con el nombre: " + username + " no encontrado"));

        // 1. Obtener el UserRole Enum usando el ID
        UserRole userRoleEnum = UserRole.fromId(userEntity.getRolId())
                .orElseThrow(() -> new IllegalStateException("ID de rol desconocido: " + userEntity.getRolId()));

        // 2. Construir la autoridad de Spring Security
        // El formato debe ser "ROLE_<NOMBRE_DEL_ROL>" (ej: ROLE_TECNICO, ROLE_CLIENTE, ROLE_ADMINISTRADOR)
        String roleName = "ROLE_" + userRoleEnum.name();

        // 3. Crear la lista de autoridades
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(roleName)
        );

        // 4. Devolver el objeto UserDetails con las autoridades
        return new User(
                userEntity.getUsername(),
                userEntity.getPasswordHash(),
                authorities
        );
    }

    public List<UserDTO> getTech(){
        List<UserEntity> tech = userRepository.findByRolId(2L);
        return tech.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    //Metodo para generar una contraseña segura y aleatoria
    private String generatedRandomPassword(){
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:,.<>?";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12); //Longitud de 12 caracteres

        for (int i = 0; i<12; i++){
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Transactional
    public UserDTO changePassword(String username, String currentPassword, String newPassword){
        //1.Encuentra el usuario por su nombre de usuario
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        //2.Verifica si la contraseña actual (si no es temporal)
        if (!userEntity.isPasswordExpired()){
            if (!passwordEncoder.matches(currentPassword, userEntity.getPasswordHash())){
                throw new IllegalArgumentException("La contraseña actual es incorrecta");
            }
        }

        //3.Cifra la nueva contraseña
        String newHashedPassword = passwordEncoder.encode(newPassword);

        //4.Actualiza la contraseña en la entidad del usuario
        userEntity.setPasswordHash(newHashedPassword);
        userEntity.setPasswordExpired(false);

        //6.Guarda los cambios en la base de datos
        userRepository.save(userEntity);
        return convertToUserDTO(userEntity);
    }


    public Page<UserDTO> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<UserEntity> usuarios = userRepository.findAll(pageable);
        return usuarios.map(this::convertToUserDTO);
    }


    public UserDTO registerNewUser(UserDTO dto) {
        //Limpiar el caché antes de las validaciones esto es para que tenga que consultar la base y no guarde informacion innecesaria
        entityManager.clear();
        // Asegúrate de que el correo electrónico no sea nulo antes de guardar
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico no puede ser nulo.");
        }

        //Validaciones de entrada
        //Busca en el userRepository si existe algún registro en la DB que repita el email / usuario / telefono a registrar
        userRepository.findByEmailIgnoreCase(dto.getEmail()).ifPresent(user -> {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        });

        userRepository.findByUsername(dto.getUsername()).ifPresent(user-> {
            throw new IllegalArgumentException(("El usuario ya esta registrado."));
        });

        userRepository.findByPhone(dto.getPhone()).ifPresent(user -> {
            throw new IllegalArgumentException(("El número ya está registrado"));
        });

        //Genera la contraseña aleatoria
        String randomPassword = generatedRandomPassword();

        //Obtener el primer id de tbCompanies
        Long firstCompanyId = companyRepository.findFirstCompanyId().orElseThrow(() -> new IllegalArgumentException("La compañia no existe."));

        UserEntity userEntity = new UserEntity();

        // Usar el rol del DTO si está presente
        if (dto.getRol() != null && dto.getRol().getId() != null) {
            // Verificar que el rol sea válido
            UserRole role = UserRole.fromId(dto.getRol().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Rol con ID " + dto.getRol().getId() + " no válido"));
            userEntity.setRolId(role.getId());
        }else{
            long userCount = userRepository.count();
            if (userCount == 0) {
                userEntity.setRolId(UserRole.ADMINISTRADOR.getId());
            } else {
                userEntity.setRolId(UserRole.CLIENTE.getId());
            }
            // Si ya hay usuarios, asigna el rol de Cliente
            userEntity.setRolId(UserRole.CLIENTE.getId());
        }


        if (!isValidDomain(dto.getEmail())){
            throw new IllegalArgumentException("Dominio de correo no permistido");
        }

        //ASIGNACION DE PRIMER ID DE COMPANIA ENCONTRADA (DESDE companyRepository) A USUARIO
        Long foundCompanyId = companyRepository.findFirstCompanyId().orElseThrow(() -> new IllegalStateException("No se puede registrar el usuario: No hay compañías registradas."));

        CompanyEntity companyToAssign = companyRepository.findById(foundCompanyId).orElseThrow(() -> new IllegalStateException("La primera compañía (ID: " + firstCompanyId + ") no fue encontrada al intentar asignarla."));

        userEntity.setCompany(companyToAssign);


        if (dto.getCategory() != null && dto.getCategory().getId() != null) {
            Long categoryId = dto.getCategory().getId();

            // 🔑 CLAVE: Forzar la búsqueda y lanzar excepción si no encuentra (revisar Logs/Output)
            CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("ERROR: Categoría con ID " + categoryId + " no encontrada en la DB."));

            // 5. Establecer la entidad
            userEntity.setCategory(categoryEntity);
            // userEntity.setCategoryId(categoryId); // Esta línea es redundante con el setCategory(entity) si el mapeo es correcto.
        } else {
            // Si no se proporciona categoría
            userEntity.setCategoryId(null);
            userEntity.setCategory(null);
        }

        userEntity.setFullName(dto.getName());
        userEntity.setUsername(dto.getUsername());
        userEntity.setEmail(dto.getEmail());
        userEntity.setPhone(dto.getPhone());
        String hashedPassword = passwordEncoder.encode(randomPassword); //IMPORTANTE: REQUERIDO HASHEAR ANTES DE INSERTAR A LA DB
        userEntity.setPasswordHash(hashedPassword);
        userEntity.setIsActive(dto.getIsActive());
        userEntity.setProfilePictureUrl(dto.getProfilePictureUrl());

        //Marca la contraseña como expirada para forzar el cambio en el primer inicio de sesion
        userEntity.setPasswordExpired(true);

        //Guarda el usuario registrado en la DB
        UserEntity savedUser = userRepository.save(userEntity);

        //Envia la contraseña temporal por correo electronico
        String subject = "Credenciales de Acceso a Help Desk H2C";
        String body = "Hola " + dto.getName() + " tu cuenta ha sido creada exitosamente. Tu nombre de usuario es: " + dto.getUsername() + " , tu contraseña temporal es: " + randomPassword + " Por favor no compartas con nadie esta información, Saludos del equipo de H2C";
        emailService.sendEmail(dto.getEmail(), subject, body);

        return convertToUserDTO(savedUser);

    }



    @Transactional
    public UserDTO UpdateUser(Long id, Map<String, String> updates) throws ExceptionUserNotFound {
        // 1. Encontrar el usuario por su ID
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ExceptionUserNotFound("Usuario no encontrado con ID: " + id));

        // 2. Iterar sobre el Map de actualizaciones y aplicar los cambios
        updates.forEach((key, value) -> {
            // Asegúrate de que el valor no sea null o vacío antes de actualizar
            if (value != null && !value.trim().isEmpty()) {
                switch (key) {
                    case "fullName":
                        user.setFullName(value);
                        break;
                    case "username":
                        user.setUsername(value);
                        break;
                    case "email":
                        user.setEmail(value);
                        break;
                    case "phone":
                        user.setPhone(value);
                        break;
                    case "password":
                        // Hash de la nueva contraseña recibida
                        String hashedPassword = passwordEncoder.encode(value);
                        user.setPasswordHash(hashedPassword);
                        break;
                    case "profilePictureUrl":
                        user.setProfilePictureUrl(value);
                        break;
                    // Agrega más casos para otros campos si es necesario
                }
            }
        });

        // 3. Guardar el usuario actualizado en la base de datos
        UserEntity updatedUser = userRepository.save(user);

        // 4. Convertir la entidad a DTO y devolverla
        return convertToUserDTO(updatedUser);
    }
  
    //METODO PARA REGISTRAR TECNICOS
 public UserDTO registerNewUserTech(UserDTO dto) {
    //Limpiar el cache antes de las validaciones esto es para que tenga que consultar la base y no guarde informacion innecesaria
    entityManager.clear();

    //Validaciones de entrada
    //Busca en el userRepository si existe algun registro en la DB que repita el email / usuario / telefono a registrar
    userRepository.findByEmailIgnoreCase(dto.getEmail()).ifPresent(user -> {
        throw new IllegalArgumentException("El correo electrónico ya está registrado.");
    });

    userRepository.findByUsername(dto.getUsername()).ifPresent(user-> {
        throw new IllegalArgumentException(("El usuario ya esta registrado."));
    });

    userRepository.findByPhone(dto.getPhone()).ifPresent(user -> {
        throw new IllegalArgumentException(("El número ya está registrado"));
    });

    //Genera la contraseña aleatoria
    String randomPassword = generatedRandomPassword();

    //Obtener el primer id de tbCompanies
    Long firstCompanyId = companyRepository.findFirstCompanyId().orElseThrow(() -> new IllegalArgumentException("La compañia no existe."));

    UserEntity userEntity = new UserEntity();

    //Asignacion de rol a usuario. Por defecto, al crearlo sera "Cliente" (Se debera actualizar si el usuario es un tecnico)
    long userCount = userRepository.count();


    userEntity.setRolId(UserRole.TECNICO.getId());


    if (!isValidDomain(dto.getEmail())){
        throw new IllegalArgumentException("Dominio de correo no permistido");
    }

    //ASIGNACION DE PRIMER ID DE COMPANIA ENCONTRADA (DESDE companyRepository) A USUARIO
    Long foundCompanyId = companyRepository.findFirstCompanyId().orElseThrow(() -> new IllegalStateException("No se puede registrar el usuario: No hay compañías registradas."));

    CompanyEntity companyToAssign = companyRepository.findById(foundCompanyId).orElseThrow(() -> new IllegalStateException("La primera compañía (ID: " + firstCompanyId + ") no fue encontrada al intentar asignarla."));

    userEntity.setCompany(companyToAssign);


    if (dto.getCategory() != null && dto.getCategory().getId() != null) {
        Long categoryId = dto.getCategory().getId();

        // 🔑 CLAVE: Forzar la búsqueda y lanzar excepción si no encuentra (revisar Logs/Output)
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("ERROR: Categoría con ID " + categoryId + " no encontrada en la DB."));

        // 5. Establecer la entidad
        userEntity.setCategory(categoryEntity);
        // userEntity.setCategoryId(categoryId); // Esta línea es redundante con el setCategory(entity) si el mapeo es correcto.
    } else {
        // Si no se proporciona categoría
        userEntity.setCategoryId(null);
        userEntity.setCategory(null);
    }

    userEntity.setFullName(dto.getName());
    userEntity.setUsername(dto.getUsername());
    userEntity.setEmail(dto.getEmail());
    userEntity.setPhone(dto.getPhone());
    String hashedPassword = passwordEncoder.encode(randomPassword); //IMPORTANTE: REQUERIDO HASHEAR ANTES DE INSERTAR A LA DB
    userEntity.setPasswordHash(hashedPassword);
    userEntity.setIsActive(dto.getIsActive());
    userEntity.setProfilePictureUrl(dto.getProfilePictureUrl());

    //Marca la contraseña como expirada para forzar el cambio en el primer inicio de sesion
    userEntity.setPasswordExpired(true);

    //Guarda el usuario registrado en la DB
    UserEntity savedUser = userRepository.save(userEntity);

    //Envia la contraseña temporal por correo electronico
    String subject = "Credenciales de Acceso a Help Desk H2C";
    String body = "Hola " + dto.getName() + " tu cuenta ha sido creada exitosamente. Tu nombre de usuario es: " + dto.getUsername() + " , tu contraseña temporal es: " + randomPassword + " Por favor no compartas con nadie esta información, Saludos del equipo de H2C";
    emailService.sendEmail(dto.getEmail(), subject, body);

    return convertToUserDTO(savedUser);

}

    private boolean isValidDomain(String email){
        return email.endsWith("@gmail.com") || email.endsWith("@ricaldone.edu.sv");
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
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
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

    if(dto.getProfilePictureUrl() != null && !dto.getProfilePictureUrl().isBlank()){
        existingUser.setProfilePictureUrl(dto.getProfilePictureUrl());
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
            throw new ExceptionUserNotFound("Usuario con ID " + id + " no encontrado.");
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
    dto.setProfilePictureUrl(usuario.getProfilePictureUrl());
    dto.setRegistrationDate(usuario.getRegistrationDate());
    return dto;
}



    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    //Encontrar usuario por su username. -- Su uso es importante para la actualizacion de datos de cada usuario, en configuracion
    public Long getUserIdByUsername(String username) {
        return userRepository.findByUsername(username).map(UserEntity::getUserId).orElseThrow(() -> new IllegalArgumentException("El usuario " + username + " no existe"));
    }

    private static final Map<String, Long> CATEGORY_NAME_TO_ID_MAP = Map.of(
            "Soporte técnico", 1L,
            "Consultas", 2L,
            "Gestion de Usuarios", 3L, // NOTA: Si tu frontend usa 'Gestion de usuarios' vs 'Gestión de Usuarios', asegúrate que coincida aquí.
            "Redes", 4L,
            "Incidentes Críticos", 5L
    );

    //Metodo para obtener usuarios por rol
    public Page<UserDTO> getFilteredTechUsers(int page, int size, String term, String category, String period) {
        final Long roleIdValue = 2L;
        Pageable pageable = PageRequest.of(page, size);

        // 🚀 Traducción: Obtener el ID de la categoría (Long)
        Long categoryIdToFilter = null;

        if (category != null && !category.equalsIgnoreCase("all")) {
            // Busca el nombre en el mapa. Si no se encuentra, categoryIdToFilter será null.
            categoryIdToFilter = CATEGORY_NAME_TO_ID_MAP.get(category);
        }

        Page<UserEntity> userPage = userRepository.findTechUsersWithFilters(
                pageable,
                roleIdValue,
                term,
                categoryIdToFilter,
                period
        );
        return userPage.map(this::convertToUserDTO);



//        return userPage.map(this::convertToUserDTO);
//
//        return userRepository.findByRolId(roleIdValue, pageable);

    }

    public List<UserDTO> findByRole(Long roleId) {
        List<UserEntity> users = userRepository.findByRolId(roleId);

        return users.stream().map(user -> {
            UserDTO dto = new UserDTO();
            dto.setId(user.getUserId());
            dto.setName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setProfilePictureUrl(user.getProfilePictureUrl());

            UserRole role = UserRole.fromId(user.getRolId())
                    .orElseThrow(() -> new IllegalArgumentException("Rol inválido"));
            dto.setRol(new RolDTO(role));

            return dto;
        }).collect(Collectors.toList());
    }

    public UserDTO registerTechnicianPending(UserDTO userDTO, Long companyId) {
        // 1. Validar que la compañía exista
        CompanyEntity companyToAssign = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Compañía no encontrada. No se puede asignar el técnico."));

        // 2. Crear y configurar la entidad de usuario
        UserEntity userEntity = new UserEntity();
        userEntity.setFullName(userDTO.getName());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setPhone(userDTO.getPhone());
        userEntity.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        userEntity.setUsername(userDTO.getUsername());

        // Asignar los valores por defecto para un técnico pendiente
        userEntity.setIsActive(0); // 0 para pendiente, 1 para activo
        userEntity.setPasswordExpired(true); // La contraseña está expirada por defecto

        // Asignar el ID del rol directamente
        userEntity.setRolId(2L); // 2 es el ID del rol 'Técnico'

        // 3. Asignar la compañía
        userEntity.setCompany(companyToAssign);

        // Guardar la entidad en la base de datos
        // Hibernate automáticamente establecerá la fecha de registro gracias a @CreationTimestamp
        UserEntity savedUser = userRepository.save(userEntity);

        return convertToUserDTO(savedUser);
    }



    public UserDTO assignCategoryAndActivateTechnician(Long userId, Long categoryId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ExceptionUserNotFound("El usuario con ID " + userId + " no existe."));

        // 1. Validar que el usuario sea un técnico y que no tenga una categoría asignada
        if (!userEntity.getRolId().equals(UserRole.TECNICO.getId())) {
            throw new IllegalArgumentException("Solo se pueden asignar categorías a usuarios con rol de TÉCNICO.");
        }

        if (userEntity.getCategory() != null) {
            throw new IllegalArgumentException("El técnico con ID " + userId + " ya tiene una categoría asignada.");
        }

        // 2. Verificar que la categoría exista
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ExceptionCategoryNotFound("La categoría con ID " + categoryId + " no existe."));

        // 3. Asignar la categoría y activar al técnico
        userEntity.setCategory(categoryEntity);

        // 4. Generar y guardar la contraseña
        String randomPassword = generatedRandomPassword();
        String hashedPassword = passwordEncoder.encode(randomPassword);
        userEntity.setPasswordHash(hashedPassword);
        userEntity.setPasswordExpired(false); // La contraseña temporal ya es la inicial

        UserEntity updatedUser = userRepository.save(userEntity);

        // 5. Enviar correo electrónico
        String subject = "Credenciales de Acceso a Help Desk H2C";
        String body = "Hola " + updatedUser.getFullName() + " tu cuenta de técnico ha sido activada. Tu nombre de usuario es: " + updatedUser.getUsername() + " , tu contraseña temporal es: " + randomPassword + " Por favor no compartas con nadie esta información, Saludos del equipo de H2C";
        emailService.sendEmail(updatedUser.getEmail(), subject, body);

        return convertToUserDTO(updatedUser);
    }

    public UserDTO finalizeAdminSetup(Long userId) {
        UserEntity admin = userRepository.findById(userId)
                .orElseThrow(() -> new ExceptionUserNotFound("Usuario administrador no encontrado con ID: " + userId));

        // Generar la contraseña segura y aleatoria
        String randomPassword = generatedRandomPassword();

        // Hashear la contraseña antes de guardarla
        String hashedPassword = passwordEncoder.encode(randomPassword);

        // Guardar la contraseña hasheada y marcarla como expirada
        // para forzar el cambio en el primer inicio de sesión
        admin.setPasswordHash(hashedPassword);
        admin.setIsActive(1); // O el valor que uses para indicar que está activo
        admin.setPasswordExpired(true);

        // Guardar los cambios en la base de datos
        UserEntity savedAdmin = userRepository.save(admin);

        // Enviar las credenciales temporales por correo electrónico
        String subject = "Credenciales de Acceso a Help Desk H2C";
        String body = "Hola " + admin.getFullName() + " tu cuenta ha sido creada exitosamente. Tu nombre de usuario es: " + admin.getUsername() + " , tu contraseña temporal es: " + randomPassword + " Por favor no compartas con nadie esta información, Saludos del equipo de H2C";
        emailService.sendEmail(admin.getEmail(), subject, body);

        return convertToUserDTO(savedAdmin);
    }

    public UserDTO registerInitialAdmin(UserDTO dto) {
        // 1. Limpiar el caché para evitar problemas
        entityManager.clear();

        // 2. Validaciones: asegúrate de que el email, username, y teléfono no existan
        userRepository.findByEmailIgnoreCase(dto.getEmail()).ifPresent(user -> {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        });
        // Opcional: Genera el username si no está en el DTO para el caso de admin
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            String generatedUsername = generateUsername(dto.getName());
            dto.setUsername(generatedUsername);
        }

        userRepository.findByUsername(dto.getUsername()).ifPresent(user -> {
            throw new IllegalArgumentException(("El usuario '" + dto.getUsername() + "' ya esta registrado."));
        });

        userRepository.findByPhone(dto.getPhone()).ifPresent(user -> {
            throw new IllegalArgumentException(("El número ya está registrado."));
        });

        // 3. Generar la contraseña aleatoria y hashearla
        String randomPassword = generatedRandomPassword();
        String hashedPassword = passwordEncoder.encode(randomPassword);

        // 4. Crear la entidad de usuario
        UserEntity userEntity = new UserEntity();
        userEntity.setFullName(dto.getName());
        userEntity.setUsername(dto.getUsername());
        userEntity.setEmail(dto.getEmail());
        userEntity.setPhone(dto.getPhone());
        userEntity.setPasswordHash(hashedPassword);

        // 5. Asignar el rol de ADMINISTRADOR, isActive y PasswordExpired
        userEntity.setRolId(UserRole.ADMINISTRADOR.getId()); // Asigna el rol de administrador
        userEntity.setIsActive(0); // 0 para indicar que está pendiente de activación
        userEntity.setPasswordExpired(true); // Requiere cambio de contraseña en el primer login

        // 6. Asignar la primera compañía encontrada
        Long foundCompanyId = companyRepository.findFirstCompanyId()
                .orElseThrow(() -> new IllegalStateException("No se puede registrar el usuario: No hay compañías registradas."));
        CompanyEntity companyToAssign = companyRepository.findById(foundCompanyId)
                .orElseThrow(() -> new IllegalStateException("La primera compañía (ID: " + foundCompanyId + ") no fue encontrada."));
        userEntity.setCompany(companyToAssign);

        // 7. Guardar el usuario en la base de datos
        UserEntity savedUser = userRepository.save(userEntity);

        // 8. No enviar correo aquí. La lógica de envío está en finalizeAdminSetup
        return convertToUserDTO(savedUser);
    }

    private String generateUsername(String fullName) {
        String[] parts = fullName.split(" ");
        String firstName = parts[0].toLowerCase();
        String lastName = parts.length > 1 ? parts[parts.length - 1].toLowerCase() : "";
        return firstName + "." + lastName;
    }

    public UserDTO findUserById(Long id) {
        // Busca la entidad en la base de datos. orElseThrow lanza la excepción si no lo encuentra.
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ExceptionUserNotFound("Usuario con ID " + id + " no encontrado."));

        // Convierte la entidad a DTO para enviarla al frontend.
        return convertToUserDTO(userEntity);
    }
}