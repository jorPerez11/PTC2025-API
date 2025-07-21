package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Entities.CommentEntity;
import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Exceptions.CommentExceptions;
import H2C_Group.H2C_API.Models.DTO.CommentDTO;
import H2C_Group.H2C_API.Repositories.CommentRepository;
import H2C_Group.H2C_API.Repositories.TicketRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    public List<CommentDTO> getAllComments() {
        List<CommentEntity> comments = commentRepository.findAll();
        return comments.stream().map(this::convertToCommentDTO).collect(Collectors.toList());
    }

    public CommentDTO createComment(CommentDTO dto) {
        //Validaciones
        UserEntity existingUser = userRepository.findById(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("El usuario con id" + dto.getUserId() + " no existe"));

        TicketEntity existingTicket = ticketRepository.findById(dto.getTicketId()).orElseThrow(() -> new IllegalArgumentException("La ticket con id" + dto.getTicketId() + " no existe"));

        CommentEntity comment = new CommentEntity();
        if (dto.getMessage() == null){
            throw new IllegalArgumentException("El comentario no puede ser nulo");
        }

        TicketEntity ticket = ticketRepository.findById(dto.getTicketId()).orElseThrow(() -> new IllegalArgumentException("Ticket con ID " + dto.getTicketId() + " no encontrado."));
        comment.setTicket(ticket);

        UserEntity user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("Usuario con ID " + dto.getUserId() + " no encontrado."));
        comment.setUser(user);

        comment.setMessage(dto.getMessage());

        CommentEntity savedComment =  commentRepository.save(comment);
        return convertToCommentDTO(savedComment);
    }


    public CommentDTO updateComment(Long id, CommentDTO dto) {
        //Validaciones
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del comentario a actualizar no puede ser nulo o no válido.");
        }

        CommentEntity existingComment = commentRepository.findById(id).orElseThrow(() -> new CommentExceptions.CommentNotFoundException("Comentario con ID " + id + " no encontrado para actualizar."));

        TicketEntity existingTicket = ticketRepository.findById(dto.getTicketId()).orElseThrow(() -> new IllegalArgumentException("El ticket con id" + dto.getTicketId() + " no existe"));

        // 3. Validar y actualizar userId si viene en el DTO
        if (dto.getUserId() != null) {
            UserEntity user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("El usuario con ID " + dto.getUserId() + " no existe."));
            existingComment.setUser(user);
        }else{
            throw new IllegalArgumentException("El id del usuario no puede ser nulo.");
        }

        // 4. Validar y actualizar ticketId si viene en el DTO
        if (dto.getTicketId() != null) {
            ticketRepository.findById(dto.getTicketId()).orElseThrow(() -> new IllegalArgumentException("El ticket con ID " + dto.getTicketId() + " no existe."));
            existingTicket.setTicketId(dto.getTicketId());
        }

        // 5. Validar y actualizar el mensaje
        if (dto.getMessage() != null) { // Si el mensaje viene, lo actualizamos
            if (dto.getMessage().isBlank()) {
                existingComment.setMessage(dto.getMessage().trim());
            } else if (dto.getMessage().length() > 1000) {
                throw new IllegalArgumentException("El texto del comentario actualizado excede la longitud máxima permitida (1000 caracteres).");
            } else {
                existingComment.setMessage(dto.getMessage().trim());
            }
        }

        CommentEntity updatedComment = commentRepository.save(existingComment);
        return convertToCommentDTO(updatedComment);
    }

    public void deleteComment(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del comentario no puede ser nulo o no válido");
        }

        boolean exists = commentRepository.existsById(id);

        if (!exists) {
            throw new CommentExceptions.CommentNotFoundException("Comentario con ID " + id + " no encontrado.");
        }

        commentRepository.deleteById(id);

    }


    private CommentDTO convertToCommentDTO(CommentEntity commentEntity) {
        CommentDTO  commentDTO = new CommentDTO();
        commentDTO.setCommentId(commentEntity.getCommentId());

        if (commentEntity.getTicket() != null) {
            commentDTO.setTicketId(commentEntity.getTicket().getTicketId());
        }else {
            throw new IllegalArgumentException("El ID del ticket no puede ser nulo.");
        }

        if  (commentEntity.getUser() != null) {
            commentDTO.setUserId(commentEntity.getUser().getUserId());
        }else {
            throw new IllegalArgumentException("El ID del user no puede ser nulo.");
        }

        commentDTO.setMessage(commentEntity.getMessage());
        return commentDTO;
    }

}
