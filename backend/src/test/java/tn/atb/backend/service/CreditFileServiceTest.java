package tn.atb.backend.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.atb.backend.client.MlServiceClient;
import tn.atb.backend.dto.creditfile.CreditFileCreateRequest;
import tn.atb.backend.dto.creditfile.CreditFileUpdateRequest;
import tn.atb.backend.dto.ml.MlPredictionRequest;
import tn.atb.backend.dto.ml.MlPredictionResponse;
import tn.atb.backend.dto.ml.MlPredictionResponse.MlDecisionFactor;
import tn.atb.backend.entity.Client;
import tn.atb.backend.entity.CreditFile;
import tn.atb.backend.entity.enums.AuditAction;
import tn.atb.backend.entity.enums.EducationLevel;
import tn.atb.backend.entity.enums.EmploymentType;
import tn.atb.backend.entity.enums.Gender;
import tn.atb.backend.entity.enums.MaritalStatus;
import tn.atb.backend.entity.enums.PropertyArea;
import tn.atb.backend.exception.ResourceNotFoundException;
import tn.atb.backend.mapper.CreditFileMapper;
import tn.atb.backend.repository.ClientRepository;
import tn.atb.backend.repository.CreditFileRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies how a domain file is translated into the raw features the ML model was trained on.
 * This is where the loan-amount scale bug lived (the app stores TND, the dataset thousands), so
 * every mapping the model depends on is pinned here rather than trusted by eye.
 */
@ExtendWith(MockitoExtension.class)
class CreditFileServiceTest {

    @Mock private CreditFileRepository creditFileRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private CreditFileMapper creditFileMapper;
    @Mock private AuditLogService auditLogService;
    @Mock private MlServiceClient mlServiceClient;

    @InjectMocks private CreditFileService creditFileService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // --- Fixtures -----------------------------------------------------------------------------

    private Client.ClientBuilder baseClient() {
        return Client.builder()
                .id("client-1")
                .firstName("Amine")
                .lastName("Test")
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.MARRIED)
                .educationLevel(EducationLevel.GRADUATE)
                .employmentType(EmploymentType.PERMANENT)
                .dependents(1)
                .monthlyIncome(3000.0)
                .propertyArea(PropertyArea.URBAN);
    }

    private CreditFile.CreditFileBuilder baseFile() {
        return CreditFile.builder()
                .id("file-1")
                .clientId("client-1")
                .loanAmount(128_000.0)
                .loanDurationMonths(360)
                .coapplicantIncome(1500.0)
                .creditHistory("GOOD");
    }

    private MlPredictionResponse okResponse() {
        MlPredictionResponse response = new MlPredictionResponse();
        response.setRiskScore(9.6);
        response.setRiskLevel("LOW");
        response.setAiDecision("ACCEPTABLE");
        response.setApproved(true);
        return response;
    }

    /** Runs analyze() with the given client/file and returns the request sent to the ML service. */
    private MlPredictionRequest captureRequest(Client client, CreditFile file) {
        when(creditFileRepository.findById("file-1")).thenReturn(Optional.of(file));
        when(clientRepository.findById("client-1")).thenReturn(Optional.of(client));
        when(creditFileRepository.save(any(CreditFile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mlServiceClient.predict(any(MlPredictionRequest.class))).thenReturn(okResponse());

        creditFileService.analyzeCreditFile("file-1");

        ArgumentCaptor<MlPredictionRequest> captor = ArgumentCaptor.forClass(MlPredictionRequest.class);
        org.mockito.Mockito.verify(mlServiceClient).predict(captor.capture());
        return captor.getValue();
    }

    // --- Loan amount scale (the regression that caused every file to read "Défavorable") -------

    @Test
    void convertsLoanAmountFromTndToThousandsForTheModel() {
        MlPredictionRequest request = captureRequest(baseClient().build(), baseFile().build());

        // 128 000 TND is expressed to the model as 128 (the dataset's unit).
        assertThat(request.getLoanAmount()).isEqualTo(128.0);
    }

    // --- Credit history mapping ---------------------------------------------------------------

    @Test
    void mapsBadCreditHistoryToZero() {
        MlPredictionRequest request = captureRequest(baseClient().build(), baseFile().creditHistory("BAD").build());
        assertThat(request.getCreditHistory()).isZero();
    }

    @Test
    void mapsGoodCreditHistoryToOne() {
        MlPredictionRequest request = captureRequest(baseClient().build(), baseFile().creditHistory("GOOD").build());
        assertThat(request.getCreditHistory()).isEqualTo(1);
    }

    @Test
    void defaultsNullCreditHistoryToOne() {
        MlPredictionRequest request = captureRequest(baseClient().build(), baseFile().creditHistory(null).build());
        assertThat(request.getCreditHistory()).isEqualTo(1);
    }

    // --- Property area mapping ----------------------------------------------------------------

    @Test
    void mapsPropertyAreaToTheDatasetSpelling() {
        MlPredictionRequest rural = captureRequest(baseClient().propertyArea(PropertyArea.RURAL).build(), baseFile().build());
        assertThat(rural.getPropertyArea()).isEqualTo("Rural");
    }

    @Test
    void defaultsNullPropertyAreaToUrban() {
        MlPredictionRequest request = captureRequest(baseClient().propertyArea(null).build(), baseFile().build());
        assertThat(request.getPropertyArea()).isEqualTo("Urban");
    }

    // --- Dependents clamp ---------------------------------------------------------------------

    @Test
    void clampsDependentsToTheModelsMaximum() {
        MlPredictionRequest request = captureRequest(baseClient().dependents(7).build(), baseFile().build());
        assertThat(request.getDependents()).isEqualTo(3);
    }

    @Test
    void defaultsNullDependentsToZero() {
        MlPredictionRequest request = captureRequest(baseClient().dependents(null).build(), baseFile().build());
        assertThat(request.getDependents()).isZero();
    }

    // --- Categorical spellings the model expects ----------------------------------------------

    @Test
    void mapsCategoricalFeaturesToTrainedLabels() {
        Client client = baseClient()
                .gender(Gender.FEMALE)
                .maritalStatus(MaritalStatus.SINGLE)
                .educationLevel(EducationLevel.NOT_GRADUATE)
                .employmentType(EmploymentType.SELF_EMPLOYED)
                .build();

        MlPredictionRequest request = captureRequest(client, baseFile().build());

        assertThat(request.getGender()).isEqualTo("Female");
        assertThat(request.getMarried()).isEqualTo("No");
        assertThat(request.getEducation()).isEqualTo("Not Graduate");
        assertThat(request.getSelfEmployed()).isEqualTo("Yes");
    }

    // --- Persisted decision factors -----------------------------------------------------------

    @Test
    void keepsAtMostSixDecisionFactors() {
        MlPredictionResponse response = okResponse();
        response.setFactors(IntStream.range(0, 10)
                .mapToObj(i -> {
                    MlDecisionFactor f = new MlDecisionFactor();
                    f.setFeature("F" + i);
                    f.setImpact(1.0 / (i + 1));
                    f.setReducesRisk(i % 2 == 0);
                    return f;
                })
                .toList());

        when(creditFileRepository.findById("file-1")).thenReturn(Optional.of(baseFile().build()));
        when(clientRepository.findById("client-1")).thenReturn(Optional.of(baseClient().build()));
        when(mlServiceClient.predict(any(MlPredictionRequest.class))).thenReturn(response);

        ArgumentCaptor<CreditFile> saved = ArgumentCaptor.forClass(CreditFile.class);
        when(creditFileRepository.save(saved.capture())).thenAnswer(inv -> inv.getArgument(0));

        creditFileService.analyzeCreditFile("file-1");

        assertThat(saved.getValue().getDecisionFactors()).hasSize(6);
    }

    @Test
    void handlesAResponseWithNoFactors() {
        MlPredictionResponse response = okResponse();
        response.setFactors(null);

        when(creditFileRepository.findById("file-1")).thenReturn(Optional.of(baseFile().build()));
        when(clientRepository.findById("client-1")).thenReturn(Optional.of(baseClient().build()));
        when(mlServiceClient.predict(any(MlPredictionRequest.class))).thenReturn(response);

        ArgumentCaptor<CreditFile> saved = ArgumentCaptor.forClass(CreditFile.class);
        when(creditFileRepository.save(saved.capture())).thenAnswer(inv -> inv.getArgument(0));

        creditFileService.analyzeCreditFile("file-1");

        assertThat(saved.getValue().getDecisionFactors()).isEmpty();
    }

    // --- Guard clauses ------------------------------------------------------------------------

    @Test
    void analyzeThrowsWhenFileMissing() {
        when(creditFileRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> creditFileService.analyzeCreditFile("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void analyzeThrowsWhenClientMissing() {
        when(creditFileRepository.findById("file-1")).thenReturn(Optional.of(baseFile().build()));
        when(clientRepository.findById("client-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> creditFileService.analyzeCreditFile("file-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- CRUD paths ---------------------------------------------------------------------------

    @Test
    void createPersistsTheFileAndLogsIt() {
        // createCreditFile stamps createdBy from the security context.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("123456", null));
        when(clientRepository.findById("client-1")).thenReturn(Optional.of(baseClient().build()));
        when(creditFileRepository.save(any(CreditFile.class))).thenAnswer(inv -> {
            CreditFile f = inv.getArgument(0);
            f.setId("file-1");
            return f;
        });

        CreditFileCreateRequest request = new CreditFileCreateRequest();
        request.setClientId("client-1");
        request.setCreditType("Crédit auto");
        request.setLoanAmount(20000.0);
        request.setLoanDurationMonths(48);
        request.setLoanPurpose("Voiture");

        creditFileService.createCreditFile(request);

        verify(auditLogService).log(eq(AuditAction.CREATE), eq("CreditFile"), eq("file-1"), any());
    }

    @Test
    void createThrowsWhenClientMissing() {
        when(clientRepository.findById("nope")).thenReturn(Optional.empty());

        CreditFileCreateRequest request = new CreditFileCreateRequest();
        request.setClientId("nope");

        assertThatThrownBy(() -> creditFileService.createCreditFile(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAppliesChangesAndLogsIt() {
        when(creditFileRepository.findById("file-1")).thenReturn(Optional.of(baseFile().build()));
        when(creditFileRepository.save(any(CreditFile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(clientRepository.findById("client-1")).thenReturn(Optional.of(baseClient().build()));

        CreditFileUpdateRequest request = new CreditFileUpdateRequest();
        request.setCreditType("Crédit immobilier");
        request.setLoanAmount(50000.0);
        request.setLoanDurationMonths(120);
        request.setLoanPurpose("Maison");

        creditFileService.updateCreditFile("file-1", request);

        verify(auditLogService).log(eq(AuditAction.UPDATE), eq("CreditFile"), eq("file-1"), any());
    }

    @Test
    void getAllMapsEveryFileWithItsClient() {
        when(creditFileRepository.findAll()).thenReturn(List.of(baseFile().build()));
        when(clientRepository.findAllById(any())).thenReturn(List.of(baseClient().build()));

        assertThat(creditFileService.getAllCreditFiles()).hasSize(1);
    }
}
