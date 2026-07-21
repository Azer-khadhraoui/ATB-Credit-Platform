package tn.atb.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tn.atb.backend.client.MlServiceClient;
import tn.atb.backend.dto.creditfile.CreditFileCreateRequest;
import tn.atb.backend.dto.creditfile.CreditFileResponse;
import tn.atb.backend.dto.creditfile.CreditFileUpdateRequest;
import tn.atb.backend.dto.ml.MlPredictionRequest;
import tn.atb.backend.dto.ml.MlPredictionResponse;
import tn.atb.backend.entity.Client;
import tn.atb.backend.entity.CreditFile;
import tn.atb.backend.entity.DecisionFactor;
import tn.atb.backend.entity.enums.AIDecision;
import tn.atb.backend.entity.enums.AuditAction;
import tn.atb.backend.entity.enums.CreditStatus;
import tn.atb.backend.entity.enums.EducationLevel;
import tn.atb.backend.entity.enums.EmploymentType;
import tn.atb.backend.entity.enums.Gender;
import tn.atb.backend.entity.enums.MaritalStatus;
import tn.atb.backend.entity.enums.PropertyArea;
import tn.atb.backend.entity.enums.RiskLevel;
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
    private final MlServiceClient mlServiceClient;

    public CreditFileResponse createCreditFile(CreditFileCreateRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        CreditFile creditFile = CreditFile.builder()
                .clientId(request.getClientId())
                .createdBy(currentMatricule())
                .creditType(request.getCreditType())
                .loanAmount(request.getLoanAmount())
                .coapplicantIncome(request.getCoapplicantIncome())
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
        creditFile.setCoapplicantIncome(request.getCoapplicantIncome());
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

    public CreditFileResponse analyzeCreditFile(String id) {
        CreditFile creditFile = findCreditFileOrThrow(id);
        Client client = clientRepository.findById(creditFile.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + creditFile.getClientId()));

        MlPredictionRequest request = buildPredictionRequest(client, creditFile);
        MlPredictionResponse response = mlServiceClient.predict(request);

        creditFile.setRiskScore(response.getRiskScore());
        creditFile.setRiskLevel(RiskLevel.valueOf(response.getRiskLevel()));
        creditFile.setAiDecision(AIDecision.valueOf(response.getAiDecision()));
        creditFile.setDecisionFactors(toDecisionFactors(response));
        creditFile.setUpdatedAt(LocalDateTime.now());

        CreditFile saved = creditFileRepository.save(creditFile);

        auditLogService.log(AuditAction.ANALYZE_WITH_AI, "CreditFile", saved.getId(),
                "Analyse IA du dossier de " + client.getFirstName() + " " + client.getLastName()
                        + " — score " + response.getRiskScore() + ", niveau " + response.getRiskLevel());

        return creditFileMapper.toResponse(saved, client);
    }

    // Only the factors that carry real weight are kept: the tail is dominated by
    // near-zero contributions that would add noise to the agent's screen without
    // telling them anything.
    private static final int MAX_STORED_FACTORS = 6;

    private List<DecisionFactor> toDecisionFactors(MlPredictionResponse response) {
        if (response.getFactors() == null) {
            return List.of();
        }
        return response.getFactors().stream()
                .limit(MAX_STORED_FACTORS)
                .map(factor -> DecisionFactor.builder()
                        .feature(factor.getFeature())
                        .impact(factor.getImpact())
                        .reducesRisk(factor.isReducesRisk())
                        .build())
                .toList();
    }

    // The Loan Prediction dataset expresses LoanAmount in thousands (a value of 128 means 128,000).
    // Our platform stores the loan amount in raw TND, so we divide by this factor before sending it
    // to the model, keeping the app and the training data on the same scale.
    private static final double LOAN_AMOUNT_SCALE = 1000.0;

    // The model's Dependents feature is capped at 3 in the training data ("3+" was
    // collapsed into a single category), so values above it are clamped rather than
    // sent as an unseen level.
    private static final int MAX_DEPENDENTS = 3;

    /**
     * Maps our domain model to the raw features expected by the FastAPI service, trained on the
     * public Loan Prediction dataset. Every feature now comes from data the agent actually
     * captures — earlier versions sent constants for dependents, education, co-applicant income
     * and property area, which meant the model scored those four inputs identically for every
     * applicant.
     */
    private MlPredictionRequest buildPredictionRequest(Client client, CreditFile creditFile) {
        boolean married = client.getMaritalStatus() == MaritalStatus.MARRIED;
        boolean selfEmployed = client.getEmploymentType() == EmploymentType.SELF_EMPLOYED;

        int creditHistory = mapCreditHistory(creditFile.getCreditHistory());

        double loanAmountTnd = creditFile.getLoanAmount() != null ? creditFile.getLoanAmount() : 0;
        double loanAmountScaled = loanAmountTnd / LOAN_AMOUNT_SCALE;

        int dependents = client.getDependents() != null
                ? Math.min(client.getDependents(), MAX_DEPENDENTS)
                : 0;

        return MlPredictionRequest.builder()
                .gender(client.getGender() == Gender.MALE ? "Male" : "Female")
                .married(married ? "Yes" : "No")
                .dependents(dependents)
                .education(client.getEducationLevel() == EducationLevel.NOT_GRADUATE ? "Not Graduate" : "Graduate")
                .selfEmployed(selfEmployed ? "Yes" : "No")
                .applicantIncome(client.getMonthlyIncome() != null ? client.getMonthlyIncome() : 0)
                .coapplicantIncome(creditFile.getCoapplicantIncome() != null ? creditFile.getCoapplicantIncome() : 0)
                .loanAmount(loanAmountScaled)
                .loanAmountTerm(creditFile.getLoanDurationMonths() != null ? creditFile.getLoanDurationMonths() : 0)
                .creditHistory(creditHistory)
                .propertyArea(mapPropertyArea(client.getPropertyArea()))
                .build();
    }

    /** Converts our enum to the exact spelling the model was trained on. */
    private String mapPropertyArea(PropertyArea area) {
        if (area == null) {
            return "Urban";
        }
        return switch (area) {
            case SEMIURBAN -> "Semiurban";
            case RURAL -> "Rural";
            case URBAN -> "Urban";
        };
    }

    /**
     * Maps the agent-provided credit history to the dataset's binary Credit_History feature
     * (1 = meets guidelines / clean record, 0 = otherwise). GOOD/AVERAGE keep the loan eligible;
     * BAD flags a poor repayment record. Unknown/legacy values default to 1 (benefit of the doubt).
     */
    private int mapCreditHistory(String history) {
        if (history == null) {
            return 1;
        }
        return switch (history) {
            case "BAD" -> 0;
            default -> 1; // GOOD, AVERAGE, or legacy free-text
        };
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
