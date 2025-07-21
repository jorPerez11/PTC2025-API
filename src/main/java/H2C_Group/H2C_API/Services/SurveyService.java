package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.SurveyEntity;
import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Exceptions.SolutionExceptions;
import H2C_Group.H2C_API.Exceptions.SurveyExceptions;
import H2C_Group.H2C_API.Models.DTO.SurveyDTO;
import H2C_Group.H2C_API.Repositories.SurveyRepository;
import H2C_Group.H2C_API.Repositories.TicketRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SurveyService {
    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    public List<SurveyDTO> getAllSurveys() {
        List<SurveyEntity> surveys = surveyRepository.findAll();
        return surveys.stream().map(this::convertToSurveyDTO).collect(Collectors.toList());
    }

    public SurveyDTO createSurvey(SurveyDTO surveyDTO) {
        //Validaciones
        UserEntity existingUser = userRepository.findById(surveyDTO.getUserId()).orElseThrow(() -> new IllegalArgumentException("El usuario con id" + surveyDTO.getUserId() + " no existe"));


        TicketEntity existingTicket = ticketRepository.findById(surveyDTO.getTicketId()).orElseThrow(() -> new IllegalArgumentException("La ticket con id" + surveyDTO.getTicketId() + " no existe"));

        SurveyEntity surveyEntity = new SurveyEntity();

        surveyEntity.setUser(existingUser);
        surveyEntity.setTicket(existingTicket);

        surveyEntity.setScore(surveyDTO.getScore());
        surveyEntity.setCommentSurvey(surveyDTO.getCommentSurvey());

        SurveyEntity savedSurveyEntity = surveyRepository.save(surveyEntity);
        return convertToSurveyDTO(savedSurveyEntity);

    }


    public SurveyDTO updateSurvey(Long id, SurveyDTO surveyDTO) {
        //Validaciones
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de la encuesta a actualizar no puede ser nulo o no válido.");
        }

        SurveyEntity existingSurvey = surveyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("La encuesta con id " + id + " no existe"));

        UserEntity existingUser = userRepository.findById(surveyDTO.getUserId()).orElseThrow(() -> new IllegalArgumentException("El usuario con id " + surveyDTO.getUserId() + " no existe"));
        existingSurvey.setUser(existingUser);

        TicketEntity existingTicket = ticketRepository.findById(surveyDTO.getTicketId()).orElseThrow(() -> new IllegalArgumentException("El ticket con id " + id + " no existe"));
        existingSurvey.setTicket(existingTicket);

        if (surveyDTO.getScore() != null) {
            existingSurvey.setScore(surveyDTO.getScore());
        }
        if (surveyDTO.getCommentSurvey() != null) {
            existingSurvey.setCommentSurvey(surveyDTO.getCommentSurvey());
        }

        SurveyEntity savedSurveyEntity = surveyRepository.save(existingSurvey);
        return convertToSurveyDTO(savedSurveyEntity);

    }

    public  void deleteSurvey(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de la solucion no puede ser nulo o no válido");
        }

        boolean exists = surveyRepository.existsById(id);

        if (!exists) {
            throw new SurveyExceptions.SurveyNotFoundException("Solucion con ID " + id + " no encontrado.");
        }

        surveyRepository.deleteById(id);
    }


    private SurveyDTO convertToSurveyDTO(SurveyEntity surveyEntity) {
        SurveyDTO surveyDTO = new SurveyDTO();

        if  (surveyEntity.getUser() != null) {
            surveyDTO.setUserId(surveyEntity.getUser().getUserId());
        }else {
            throw new IllegalArgumentException("El ID del user no puede ser nulo.");
        }

        if  (surveyEntity.getTicket() != null) {
            surveyDTO.setTicketId(surveyEntity.getTicket().getTicketId());
        }else {
            throw new IllegalArgumentException("El ID del ticket no puede ser nulo.");
        }

        surveyDTO.setScore(surveyEntity.getScore());
        surveyDTO.setCommentSurvey(surveyEntity.getCommentSurvey());
        return surveyDTO;

    }


}
