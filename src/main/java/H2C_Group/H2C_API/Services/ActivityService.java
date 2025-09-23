package H2C_Group.H2C_API.Services;

import H2C_Group.H2C_API.Entities.ActivityEntity;
import H2C_Group.H2C_API.Entities.AuditTrailEntity;
import H2C_Group.H2C_API.Entities.CommentEntity;
import H2C_Group.H2C_API.Exceptions.ExceptionCommentNotFound;
import H2C_Group.H2C_API.Models.DTO.ActivityDTO;
import H2C_Group.H2C_API.Models.DTO.AuditTrailDTO;
import H2C_Group.H2C_API.Models.DTO.CommentDTO;
import H2C_Group.H2C_API.Repositories.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    public Page<ActivityDTO> getAllActivities(Pageable pageable) {
        Page<ActivityEntity> activity = activityRepository.findAll(pageable);
        return activity.map(this::convertToACtivityDTO);
    }

    public ActivityDTO createActivity(ActivityDTO dto){
        ActivityEntity entity = new ActivityEntity();

        entity.setActivityId(dto.getId());
        entity.setActivityTitle(dto.getActivityTitle());
        entity.setActivityDescription(dto.getActivityDescription());

        ActivityEntity activityEntity = activityRepository.save(entity);
        return convertToACtivityDTO(activityEntity);
    }

    public ActivityDTO updateActivity(Long id, ActivityDTO dto){
        // 1. Validar el ID del comentario a actualizar
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del comentario a actualizar no puede ser nulo o no válido.");
        }

        ActivityEntity existingActivity = activityRepository.findById(id).orElseThrow(() -> new ExceptionCommentNotFound("Comentario con ID " + id + " no encontrado para actualizar."));


        if(dto.getActivityTitle() != null && dto.getActivityDescription() != null){
            existingActivity.setActivityTitle(dto.getActivityTitle());
        }
        if(dto.getActivityDescription() != null){
            existingActivity.setActivityDescription(dto.getActivityDescription());
        }

        ActivityEntity activityEntity = activityRepository.save(existingActivity);
        return convertToACtivityDTO(activityEntity);
    }

    public void deleteActivity(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del comentario no puede ser nulo o no válido");
        }

        boolean exists = activityRepository.existsById(id);

        if (!exists) {
            throw new ExceptionCommentNotFound("Comentario con ID " + id + " no encontrado.");
        }

        activityRepository.deleteById(id);

    }



    private ActivityDTO convertToACtivityDTO(ActivityEntity activityEntity) {
        ActivityDTO dto = new ActivityDTO();
        dto.setId(activityEntity.getActivityId());
        dto.setActivityTitle(activityEntity.getActivityTitle());
        dto.setActivityDescription(activityEntity.getActivityDescription());

        return dto;
    }


}
