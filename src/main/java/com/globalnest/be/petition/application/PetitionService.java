package com.globalnest.be.petition.application;

import com.globalnest.be.petition.domain.Agreement;
import com.globalnest.be.petition.domain.Petition;
import com.globalnest.be.petition.dto.request.PetitionSortRequest;
import com.globalnest.be.petition.dto.request.PetitionUploadRequest;
import com.globalnest.be.petition.dto.response.PetitionResponse;
import com.globalnest.be.petition.dto.response.PetitionResponseList;
import com.globalnest.be.petition.repository.AgreementRepository;
import com.globalnest.be.petition.repository.PetitionRepository;
import com.globalnest.be.user.domain.User;
import com.globalnest.be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PetitionService {

    private final PetitionRepository petitionRepository;
    private final UserService userService;
    private final AgreementRepository agreementRepository;

    public PetitionResponseList findPetitionResponseList(int page, int size, PetitionSortRequest petitionSortRequest) {
        List<PetitionResponse> petitionResponseList = petitionRepository.getPetitionResponses(petitionSortRequest, page, size);

        boolean hasNext = petitionResponseList.size() == size + 1;

        // 마지막 원소를 제외한 서브 리스트 생성
        if (hasNext) {
            petitionResponseList = petitionResponseList.subList(0, petitionResponseList.size() - 1);
        }

        return PetitionResponseList.of(hasNext, page, size, petitionSortRequest, petitionResponseList);
    }

    @Transactional
    public void uploadPetition(Long userId, PetitionUploadRequest petitionUploadRequest){
        User user = userService.findUserById(userId);

        Petition petition = petitionUploadRequest.toEntity(user);
        Petition newPetition = petitionRepository.save(petition);

        Agreement agreement = Agreement.builder()
            .user(user)
            .petition(newPetition)
            .build();
        agreementRepository.save(agreement);
    }
}