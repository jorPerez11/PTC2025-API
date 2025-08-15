package H2C_Group.H2C_API.Services;


import H2C_Group.H2C_API.Entities.SurveyEntity;
import H2C_Group.H2C_API.Entities.TicketEntity;
import H2C_Group.H2C_API.Entities.UserEntity;
import H2C_Group.H2C_API.Models.DTO.SurveyDTO;
import H2C_Group.H2C_API.Repositories.SurveyRepository;
import H2C_Group.H2C_API.Repositories.TicketRepository;
import H2C_Group.H2C_API.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<SurveyDTO> getAllSurveys(Pageable pageable) {
        Page<SurveyEntity> surveys = surveyRepository.findAll(pageable);
        return surveys.map(this::convertToSurveyDTO);
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


    private SurveyDTO convertToSurveyDTO(SurveyEntity surveyEntity) {
        SurveyDTO surveyDTO = new SurveyDTO();

        surveyDTO.setSurveyId(surveyEntity.getSurveyId());

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
