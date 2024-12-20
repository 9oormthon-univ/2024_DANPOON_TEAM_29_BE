package com.globalnest.be.petition.application;

import com.globalnest.be.petition.domain.Agreement;
import com.globalnest.be.petition.domain.Petition;
import com.globalnest.be.petition.domain.type.PetitionType;
import com.globalnest.be.petition.dto.request.PetitionSortRequest;
import com.globalnest.be.petition.dto.request.PetitionUploadRequest;
import com.globalnest.be.petition.dto.response.PetitionDetailResponse;
import com.globalnest.be.petition.dto.response.PetitionResponse;
import com.globalnest.be.petition.dto.response.PetitionResponseList;
import com.globalnest.be.petition.exception.AgreementDuplicateException;
import com.globalnest.be.petition.exception.PetitionExpiredException;
import com.globalnest.be.petition.exception.PetitionNotFoundException;
import com.globalnest.be.petition.exception.errorcode.AgreementErrorCode;
import com.globalnest.be.petition.exception.errorcode.PetitionErrorCode;
import com.globalnest.be.petition.repository.AgreementRepository;
import com.globalnest.be.petition.repository.PetitionRepository;
import com.globalnest.be.user.domain.User;
import com.globalnest.be.user.application.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PetitionService {

    private final PetitionRepository petitionRepository;
    private final UserService userService;
    private final AgreementRepository agreementRepository;

    public PetitionResponseList findPetitionResponseList(
        int page, int size, Boolean includeExpired,
        PetitionType petitionType, Boolean sortByAgreementCount
    ) {
        List<PetitionResponse> petitionResponseList =
            petitionRepository.getPetitionResponses(includeExpired, petitionType,
                sortByAgreementCount, page, size);

        boolean hasNext = petitionResponseList.size() == size + 1;

        if (hasNext) {
            petitionResponseList = petitionResponseList.subList(0, petitionResponseList.size() - 1);
        }

        return PetitionResponseList.of(hasNext, page, size, includeExpired,
            petitionType, sortByAgreementCount, petitionResponseList);
    }

    public PetitionDetailResponse findPetitionDetail(Long petitionId, Long userId) {
        return petitionRepository.getPetitionDetail(petitionId, userId);
    }

    @Transactional
    public void uploadPetition(Long userId, PetitionUploadRequest petitionUploadRequest) {
        User user = userService.findUserById(userId);

        Petition newPetition = petitionRepository.save(petitionUploadRequest.toEntity(user));

        Agreement agreement = Agreement.of(user, newPetition);

        agreementRepository.save(agreement);
    }

    @Transactional
    public void markingAgreement(Long petitionId, Long userId) {
        Petition petition = findById(petitionId);
        User user = userService.findUserById(userId);

        // 청원 날짜가 지나면 Agreement를 할 수 없도록 예외 처리
        if (petition.getAgreementDeadline().isBefore(LocalDate.now())) {
            throw new PetitionExpiredException(AgreementErrorCode.PETITION_EXPIRED);
        }

        // 이미 해당 사용자가 동의한 경우 예외 처리
        if (agreementRepository.existsByPetitionAndUser(petition, user)) {
            throw new AgreementDuplicateException(AgreementErrorCode.AGREEMENT_DUPLICATE);
        }

        agreementRepository.save(Agreement.of(user, petition));
    }

    public Petition findById(Long petitionId) {
        return petitionRepository.findById(petitionId)
            .orElseThrow(() -> new PetitionNotFoundException(PetitionErrorCode.PETITION_NOT_FOUND));
    }
}
