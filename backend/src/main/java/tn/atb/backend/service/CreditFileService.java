package tn.atb.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tn.atb.backend.dto.creditfile.CreditFileCreateRequest;
import tn.atb.backend.dto.creditfile.CreditFileResponse;
import tn.atb.backend.dto.creditfile.CreditFileUpdateRequest;
import tn.atb.backend.entity.Client;
import tn.atb.backend.entity.CreditFile;
import tn.atb.backend.entity.enums.AuditAction;
import tn.atb.backend.entity.enums.CreditStatus;
import tn.atb.backend.exception.ResourceNotFoundException;
import tn.atb.backend.mapper.CreditFileMapper;
import tn.atb.backend.repository.ClientRepository;
import tn.atb.backend.repository.CreditFileRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreditFileService {

    private final CreditFileRepository creditFileRepository;
    private final ClientRepository clientRepository;
    private final CreditFileMapper creditFileMapper;
    private final AuditLogService auditLogService;

    public CreditFileResponse createCreditFile(CreditFileCreateRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        CreditFile creditFile = CreditFile.builder()
                .clientId(request.getClientId())
                .createdBy(currentMatricule())
                .creditType(request.getCreditType())
                .loanAmount(request.getLoanAmount())
                .loanDurationMonths(request.getLoanDurationMonths())
                .loanPurpose(request.getLoanPurpose())
                .interestRate(request.getInterestRate())
                .downPayment(request.getDownPayment())
                .existingCredits(request.getExistingCredits())
                .monthlyInstallment(request.getMonthlyInstallment())
                .creditHistory(request.getCreditHistory())
                .guaranteeType(request.getGuaranteeType())
                .status(request.getStatus() != null ? request.getStatus() : CreditStatus.DRAFT)
                .agentDecision(request.getAgentDecision())
                .comments(request.getComments())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CreditFile saved = creditFileRepository.save(creditFile);

        auditLogService.log(AuditAction.CREATE, "CreditFile", saved.getId(),
                "Création du dossier de crédit pour " + client.getFirstName() + " " + client.getLastName());

        return creditFileMapper.toResponse(saved, client);
    }

    public List<CreditFileResponse> getAllCreditFiles() {
        List<CreditFile> creditFiles = creditFileRepository.findAll();

        Map<String, Client> clientsById = clientRepository
                .findAllById(creditFiles.stream().map(CreditFile::getClientId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Client::getId, c -> c));

        return creditFiles.stream()
                .map(creditFile -> creditFileMapper.toResponse(creditFile, clientsById.get(creditFile.getClientId())))
                .toList();
    }

    public CreditFileResponse getCreditFileById(String id) {
        CreditFile creditFile = findCreditFileOrThrow(id);
        Client client = clientRepository.findById(creditFile.getClientId()).orElse(null);
        return creditFileMapper.toResponse(creditFile, client);
    }

    public CreditFileResponse updateCreditFile(String id, CreditFileUpdateRequest request) {
        CreditFile creditFile = findCreditFileOrThrow(id);

        creditFile.setCreditType(request.getCreditType());
        creditFile.setLoanAmount(request.getLoanAmount());
        creditFile.setLoanDurationMonths(request.getLoanDurationMonths());
        creditFile.setLoanPurpose(request.getLoanPurpose());
        creditFile.setInterestRate(request.getInterestRate());
        creditFile.setDownPayment(request.getDownPayment());
        creditFile.setExistingCredits(request.getExistingCredits());
        creditFile.setMonthlyInstallment(request.getMonthlyInstallment());
        creditFile.setCreditHistory(request.getCreditHistory());
        creditFile.setGuaranteeType(request.getGuaranteeType());
        if (request.getStatus() != null) {
            creditFile.setStatus(request.getStatus());
        }
        creditFile.setAgentDecision(request.getAgentDecision());
        creditFile.setComments(request.getComments());
        creditFile.setUpdatedAt(LocalDateTime.now());

        CreditFile saved = creditFileRepository.save(creditFile);
        Client client = clientRepository.findById(saved.getClientId()).orElse(null);

        auditLogService.log(AuditAction.UPDATE, "CreditFile", saved.getId(),
                "Modification du dossier de crédit" + (client != null ? " de " + client.getFirstName() + " " + client.getLastName() : ""));

        return creditFileMapper.toResponse(saved, client);
    }

    public void deleteCreditFile(String id) {
        CreditFile creditFile = findCreditFileOrThrow(id);
        creditFileRepository.delete(creditFile);

        auditLogService.log(AuditAction.DELETE, "CreditFile", id, "Suppression du dossier de crédit");
    }

    private CreditFile findCreditFileOrThrow(String id) {
        return creditFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit file not found with id: " + id));
    }

    private String currentMatricule() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
