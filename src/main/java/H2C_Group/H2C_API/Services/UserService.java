package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.CategoryEntity;
import H2C_Group.H2C_API.Entities.CompanyEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Enums.Category;
import H2C_Group.H2C_API.Enums.UserRole;
import H2C_Group.H2C_API.Exceptions.ExceptionCategoryNotFound;
import H2C_Group.H2C_API.Exceptions.ExceptionUserNotFound;
import H2C_Group.H2C_API.Exceptions.ExceptionCategoryBadRequest;
import H2C_Group.H2C_API.Models.DTO.AllUsersDTO;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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

        //Notificación para cliente y técnico
        String notificationMessage = "Tu contraseña fue cambiada con éxito";
        String user = userEntity.getUsername();
        messagingTemplate.convertAndSendToUser(user, "/queue/notifications", notificationMessage);

        return convertToUserDTO(userEntity);
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

        //Notificación para el cliente
        String notificationMessage = "Tu cuenta ha sido creada exitosamente. Tu nombre de usuario es " + savedUser.getUsername() + ".";
        String username = savedUser.getUsername();
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notificationMessage);

        //Envia la contraseña temporal por correo electronico
        String subject = "Credenciales de Acceso a Help Desk H2C";
        String body = "Hola " + dto.getName() + " tu cuenta ha sido creada exitosamente. Tu nombre de usuario es: " + dto.getUsername() + " , tu contraseña temporal es: " + randomPassword + " Por favor no compartas con nadie esta información, Saludos del equipo de H2C";
        emailService.sendEmail(dto.getEmail(), subject, body);

        return convertToUserDTO(savedUser);

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

        //Notificación para el técnico
        String notificationMessage = "Tu cuenta ha sido creada exitosamente. Tu nombre de usuario es " + savedUser.getUsername() + ".";
        String username = savedUser.getUsername();
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notificationMessage);

        //Envia la contraseña temporal por correo electronico
        String subject = "Credenciales de Acceso a Help Desk H2C";
        String body = "Hola " + dto.getName() + " tu cuenta ha sido creada exitosamente. Tu nombre de usuario es: " + dto.getUsername() + " , tu contraseña temporal es: " + randomPassword + " Por favor no compartas con nadie esta información, Saludos del equipo de H2C";
        emailService.sendEmail(dto.getEmail(), subject, body);

        return convertToUserDTO(savedUser);

    }

    private boolean isValidDomain(String email){
        return email.endsWith("@gmail.com") || email.endsWith("@ricaldone.edu.sv");
    }

    // 💡 Inyección de dependencia de JdbcTemplate
    private final JdbcTemplate jdbcTemplate;

    /**
     * Obtiene una página de usuarios filtrados y paginados, incluyendo el estado de su último ticket.
     *
     * @param page         Número de página (basado en 0).
     * @param size         Cantidad de elementos por página.
     * @param searchTerm   Término de búsqueda para fullName, email o userId.
     * @param statusFilter Filtro por estado del ticket (ej. 'En Proceso', 'Cerrado', 'all').
     * @param periodFilter Filtro por período de registro del usuario (ej. 'today', 'week', 'month', 'all').
     * @return Una página de UserDTOs.
     */
    public Page<UserDTO> findAll(int page, int size, String searchTerm, String statusFilter, String periodFilter) {
        // --- Paso 1: Construir las cláusulas FROM y WHERE básicas para filtrar usuarios ---
        StringBuilder baseQueryBuilder = new StringBuilder();
        baseQueryBuilder.append("FROM tbUsers u ");
        baseQueryBuilder.append("WHERE 1=1 "); // Condición base para facilitar la adición de filtros

        List<Object> params = new ArrayList<>();

        // 1. Aplicar filtro de búsqueda por término
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String lowerSearchTerm = "%" + searchTerm.toLowerCase() + "%";
            baseQueryBuilder.append("AND (LOWER(u.fullName) LIKE ? OR LOWER(u.email) LIKE ? OR CAST(u.userId AS VARCHAR2(20)) LIKE ?) ");
            params.add(lowerSearchTerm);
            params.add(lowerSearchTerm);
            params.add(lowerSearchTerm);
        }

        // 2. Aplicar filtro por período de registro del usuario
        if (periodFilter != null && !periodFilter.equalsIgnoreCase("all")) {
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = LocalDateTime.now();

            switch (periodFilter.toLowerCase()) {
                case "today":
                    startDateTime = endDateTime.toLocalDate().atStartOfDay();
                    break;
                case "week":
                    startDateTime = endDateTime.minus(7, ChronoUnit.DAYS).toLocalDate().atStartOfDay();
                    break;
                case "month":
                    startDateTime = endDateTime.minus(30, ChronoUnit.DAYS).toLocalDate().atStartOfDay();
                    break;
            }
            if (startDateTime != null) {
                baseQueryBuilder.append("AND u.registrationDate BETWEEN ? AND ? ");
                params.add(Timestamp.valueOf(startDateTime));
                params.add(Timestamp.valueOf(endDateTime));
            }
        }

        // 3. Aplicar filtro por estado de ticket (si el usuario tiene tickets con ese estado)
        if (statusFilter != null && !statusFilter.equalsIgnoreCase("all")) {
            baseQueryBuilder.append("AND u.userId IN (SELECT tk.userId FROM tbTickets tk ");
            baseQueryBuilder.append("                 JOIN tbTicketStatus tts ON tk.ticketStatusId = tts.ticketStatusId ");
            baseQueryBuilder.append("                 WHERE tts.status = ?) ");
            params.add(statusFilter);
        }

        // --- Paso 2: Obtener el conteo total de elementos con los filtros aplicados (sin subconsulta compleja ni paginación) ---
        // Se usa el baseQueryBuilder para esta consulta de conteo.
        String countSql = "SELECT COUNT(u.userId) " + baseQueryBuilder.toString();
        Integer totalElements = jdbcTemplate.queryForObject(countSql, Integer.class, params.toArray());
        if (totalElements == null) totalElements = 0;


        // --- Paso 3: Construir la consulta principal para la recuperación de datos (con subconsulta compleja y paginación) ---
        StringBuilder dataQueryBuilder = new StringBuilder();
        dataQueryBuilder.append("SELECT u.userId, u.fullName, u.email, u.registrationDate, u.profilePictureUrl, ");
        dataQueryBuilder.append("    (SELECT ts.status FROM tbTickets tk ");
        dataQueryBuilder.append("     JOIN tbTicketStatus ts ON tk.ticketStatusId = ts.ticketStatusId ");
        dataQueryBuilder.append("     WHERE tk.userId = u.userId ");
        dataQueryBuilder.append("     ORDER BY tk.creationDate DESC "); // Obtener el último ticket creado
        dataQueryBuilder.append("     FETCH FIRST 1 ROW ONLY) AS latestTicketStatus ");
        dataQueryBuilder.append(baseQueryBuilder.toString()); // Añadir las cláusulas FROM y WHERE ya construidas

        // Añadir ORDER BY y paginación
        dataQueryBuilder.append(" ORDER BY u.registrationDate DESC "); // Orden por defecto para los resultados
        dataQueryBuilder.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add((long) page * size);
        params.add(size);

        List<UserDTO> users = jdbcTemplate.query(dataQueryBuilder.toString(), params.toArray(), (rs, rowNum) -> {
            UserDTO dto = new UserDTO();
            dto.setId(rs.getLong("userId"));
            dto.setName(rs.getString("fullName"));
            dto.setEmail(rs.getString("email"));

            Timestamp registrationTimestamp = rs.getTimestamp("registrationDate");
            if (registrationTimestamp != null) {
                dto.setRegistrationDate(registrationTimestamp.toLocalDateTime());
            }

            dto.setProfilePictureUrl(Optional.ofNullable(rs.getString("profilePictureUrl")).orElse("https://cdn-icons-png.flaticon.com/512/149/149071.png"));
            dto.setLatestTicketStatus(Optional.ofNullable(rs.getString("latestTicketStatus")).orElse("Sin solicitudes"));

            return dto;
        });

        return new PageImpl<>(users, PageRequest.of(page, size), totalElements);
    }

    /**
     * Obtiene los detalles de un ticket específico para el modal.
     * @param ticketId El ID del ticket.
     * @return Un AllUsersDTO con los detalles del ticket.
     */
    public AllUsersDTO getTicketDetailsForModal(Long ticketId) {
        String sql = "SELECT " +
                "    tk.ticketId AS id, " +
                "    tu_solicitante.fullName AS Solicitante, " +
                "    tr.rol AS Rol, " +
                "    tk.creationDate AS Creacion, " +
                "    tu_tecnico.fullName AS Tecnico_Encargado, " +
                "    tts.status AS Estado_de_Ticket " +
                "FROM " +
                "    tbTickets tk " +
                "JOIN " +
                "    tbUsers tu_solicitante ON tk.userId = tu_solicitante.userId " +
                "JOIN " +
                "    tbRol tr ON tu_solicitante.rolId = tr.rolId " +
                "LEFT JOIN " +
                "    tbUsers tu_tecnico ON tk.assignedTech = tu_tecnico.userId " +
                "LEFT JOIN " +
                "    tbTicketStatus tts ON tk.ticketStatusId = tts.ticketStatusId " +
                "WHERE " +
                "    tk.ticketId = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{ticketId}, (rs, rowNum) -> {
                AllUsersDTO dto = new AllUsersDTO();
                // Si agregas 'id' al AllUsersDTO
                // dto.setId(rs.getLong("id"));
                dto.setSolicitante(rs.getString("Solicitante"));
                dto.setRol(rs.getString("Rol"));

                Timestamp creationTimestamp = rs.getTimestamp("Creacion");
                if (creationTimestamp != null) {
                    dto.setRegistroDate(new Date(creationTimestamp.getTime()));
                } else {
                    dto.setRegistroDate(null);
                }

                dto.setTecnicoEncargado(Optional.ofNullable(rs.getString("Tecnico_Encargado")).orElse("No asignado"));
                dto.setEstadoDeTicket(rs.getString("Estado_de_Ticket"));
                return dto;
            });
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new ExceptionUserNotFound("Ticket con ID " + ticketId + " no encontrado.");
        }
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
        if (dto.getName() != null && !dto.getName().isBlank()) {
            existingUser.setFullName(dto.getName());
        }
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            //Notificación
            if (!existingUser.getUsername().equals(dto.getUsername())) {
                String notificationMessage = "Tu nombre de usuario ha sido cambiado de '" + existingUser.getUsername() + "' a '" + dto.getUsername() + "'.";
                String username = existingUser.getUsername();
                messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notificationMessage);
            }
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

    public UserDTO updateUserProfile(Long id, UserDTO dto) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del usuario a actualizar no puede ser nulo o no válido.");
        }

        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario con id" + id + " no existe"));

        // SOLO actualizar campos del perfil, IGNORAR rol y categoría
        if (dto.getName() != null && !dto.getName().isBlank()) {
            existingUser.setFullName(dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            existingUser.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            existingUser.setPhone(dto.getPhone());
        }
        if (dto.getProfilePictureUrl() != null && !dto.getProfilePictureUrl().isBlank()) {
            existingUser.setProfilePictureUrl(dto.getProfilePictureUrl());
        }

        // NO actualizar: username, password, rol, categoría

        UserEntity savedUser = userRepository.save(existingUser);
        return convertToUserDTO(savedUser);
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

    public UserDTO UpdateUser(Long id, Map<String, String> updates) throws ExceptionUserNotFound {
        // 1. Encontrar el usuario por su ID
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ExceptionUserNotFound("Usuario no encontrado con ID: " + id));

        // 2. Iterar sobre el Map de actualizaciones y aplicar los cambios
        updates.forEach((key, value) -> {
            switch (key) {
                case "Nombre": // Ahora coincide con el key "Nombre" del frontend
                    user.setFullName(value);
                    break;
                case "username":
                    user.setUsername(value);
                    break;
                case "Correo Electrónico": // Coincide con el key del frontend
                    user.setEmail(value);
                    break;
                case "Número de tel.": // Coincide con el key del frontend
                    user.setPhone(value);
                    break;
                case "password":
                    // 1. Hash de la nueva contraseña recibida
                    String hashedPassword = passwordEncoder.encode(value);
                    // 2. Usar el metodo correcto para la entidad: setPasswordHash
                    user.setPasswordHash(hashedPassword);
                    break;
                case "Foto": // Coincide con el key del frontend
                    user.setProfilePictureUrl(value);
                    break;
                // Agrega más casos para otros campos si es necesario
            }
        });

        // 3. Guardar el usuario actualizado en la base de datos
        UserEntity updatedUser = userRepository.save(user);

        // 4. Convertir la entidad a DTO y devolverla
        return convertToUserDTO(updatedUser);
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

        //Notificación para el técnico
        String notificationMessage = "Tu cuenta de técnico ha sido activada y se te ha asignado la categoría " + updatedUser.getCategory().getCategoryName() + ".";
        String username = updatedUser.getUsername();
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notificationMessage);

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

    public List<UserDTO> activatePendingTechnicians(Long companyId) {
        // 1. Buscar todos los técnicos pendientes de la compañía
        List<UserEntity> pendingTechnicians = userRepository.findByCompanyIdAndIsActive(companyId, 0);

        List<UserDTO> activatedTechnicians = new ArrayList<>();

        for (UserEntity technician : pendingTechnicians) {
            // 2. Solo procesar técnicos (rolId = 2)
            if (!technician.getRolId().equals(2L)) {
                continue;
            }

            // 3. Generar contraseña temporal
            String randomPassword = generatedRandomPassword();
            String hashedPassword = passwordEncoder.encode(randomPassword);

            // 4. Activar el técnico
            technician.setPasswordHash(hashedPassword);
            technician.setIsActive(1);
            technician.setPasswordExpired(true);

            UserEntity savedTechnician = userRepository.save(technician);

            // 5. Enviar correo
            String subject = "Credenciales de Acceso a Help Desk H2C";
            String body = "Hola " + savedTechnician.getFullName() +
                    " tu cuenta de técnico ha sido activada. Tu nombre de usuario es: " +
                    savedTechnician.getUsername() +
                    ", tu contraseña temporal es: " + randomPassword +
                    ". Por favor no compartas esta información. Saludos del equipo de H2C";

            emailService.sendEmail(savedTechnician.getEmail(), subject, body);

            activatedTechnicians.add(convertToUserDTO(savedTechnician));
        }

        return activatedTechnicians;
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

    public Map<String, Integer> getNewUsersCountsMap() {
        // 1. Obtener la lista de resultados de la consulta agregada
        List<Object[]> results = userRepository.countUsersByRegistrationMonthNative();

        // 2. Inicializar el mapa para mantener el orden de los meses
        Map<String, Integer> analyticsData = new LinkedHashMap<>();

        // 3. Mapear los resultados de la consulta
        for (Object[] result : results) {
            String monthKey = (String) result[0]; // La clave de mes (ej: "2023-09")

            // El resultado del COUNT() de SQL puede ser Long, BigInteger, etc.
            // Lo convertimos a Integer, que es lo que espera el frontend.
            Integer count = ((Number) result[1]).intValue();

            analyticsData.put(monthKey, count);
        }

        return analyticsData;
    }

    public UserDTO findUserByUsername(String username) throws ExceptionUserNotFound {
        // 1. Busca la entidad del usuario por su nombre de usuario.
        System.out.println("Iniciando busqueda de usuario en el servicio: {}" + username);
        // .orElseThrow() lanzará la excepción si no se encuentra el usuario.
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new ExceptionUserNotFound("No se encontró un usuario con el nombre de usuario: " + username));
        System.out.println("Usuario encontrado, ID: {}" + username + "y" + userEntity);
        // 2. Llama al método convertToUserDTO para convertir la entidad a un DTO y devolverlo.
        return this.convertToUserDTO(userEntity);
    }

    /**
     * Actualiza solo la URL de la foto de perfil para un usuario específico.
     * @param userId El ID del usuario.
     * @param imageUrl La nueva URL de la imagen.
     * @return El DTO del usuario actualizado.
     * @throws ExceptionUserNotFound Si el usuario no es encontrado.
     */
    public UserDTO updateUserProfilePicture(Long userId, String imageUrl) throws ExceptionUserNotFound {
        UserEntity existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ExceptionUserNotFound("Usuario con ID " + userId + " no encontrado."));

        existingUser.setProfilePictureUrl(imageUrl);

        UserEntity savedUser = userRepository.save(existingUser);

        return convertToUserDTO(savedUser); // Convierte la entidad a DTO y la devuelve
    }
}