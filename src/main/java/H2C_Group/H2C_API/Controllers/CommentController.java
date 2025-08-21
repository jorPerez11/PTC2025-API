package H2C_Group.H2C_API.Controllers;


import H2C_Group.H2C_API.Exceptions.ExceptionCommentBadRequest;
import H2C_Group.H2C_API.Exceptions.ExceptionCommentNotFound;
import H2C_Group.H2C_API.Models.DTO.CommentDTO;
import H2C_Group.H2C_API.Repositories.CommentRepository;
import H2C_Group.H2C_API.Services.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService acceso;

    @GetMapping("/GetComments")
    public ResponseEntity<Page<CommentDTO>> getComments(
            @PageableDefault(page = 0, size = 10)
            Pageable pageable) {
        Page<CommentDTO> comments = acceso.getAllComments(pageable);
        return new ResponseEntity<>(comments,  HttpStatus.OK);
    }


    @PostMapping("/PostComment")
    public ResponseEntity<?> createComment(@RequestBody @Valid CommentDTO commentDTO) {
        try{
            CommentDTO newComment = acceso.createComment(commentDTO);
            return new ResponseEntity<>(newComment, HttpStatus.CREATED);
        }catch(ExceptionCommentBadRequest e){
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST); // Código 400
        }catch(ExceptionCommentNotFound e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ocurrió un error interno del servidor al crear el comentario.");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500
        }
    }

    @PatchMapping("/UpdateComment/{id}")
    public ResponseEntity<?> updateComment(@PathVariable Long id, @Valid @RequestBody CommentDTO commentDTO) {
        try{
            CommentDTO updatedComment = acceso.updateComment(id, commentDTO);
            return  new ResponseEntity<>(updatedComment, HttpStatus.OK);
        }catch (ExceptionCommentBadRequest e){
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }catch (ExceptionCommentNotFound e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ocurrió un error interno del servidor al actualizar el comentario.");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/DeleteComment/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        try{
            acceso.deleteComment(id);
            return  new ResponseEntity<>(HttpStatus.OK);
        }catch (ExceptionCommentBadRequest e){
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }catch(ExceptionCommentNotFound e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ocurrió un error interno del servidor al eliminar el comentario.");
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
