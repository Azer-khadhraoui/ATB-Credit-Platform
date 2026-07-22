package tn.atb.backend.mapper;

import org.junit.jupiter.api.Test;
import tn.atb.backend.dto.creditfile.CreditFileResponse;
import tn.atb.backend.entity.Client;
import tn.atb.backend.entity.CreditFile;
import tn.atb.backend.entity.enums.CreditStatus;

import static org.assertj.core.api.Assertions.assertThat;

class CreditFileMapperTest {

    private final CreditFileMapper mapper = new CreditFileMapper();

    private CreditFile baseFile() {
        return CreditFile.builder()
                .id("f-1")
                .clientId("c-1")
                .createdBy("agent01")
                .creditType("Crédit auto")
                .loanAmount(20000.0)
                .loanDurationMonths(48)
                .loanPurpose("Voiture")
                .status(CreditStatus.ANALYZED)
                .build();
    }

    @Test
    void fillsClientIdentityWhenClientIsPresent() {
        Client client = Client.builder().firstName("Omar").lastName("Trabelsi").cin("87654321").build();

        CreditFileResponse response = mapper.toResponse(baseFile(), client);

        assertThat(response.getId()).isEqualTo("f-1");
        assertThat(response.getClientFullName()).isEqualTo("Omar Trabelsi");
        assertThat(response.getClientCin()).isEqualTo("87654321");
        assertThat(response.getStatus()).isEqualTo(CreditStatus.ANALYZED);
    }

    @Test
    void leavesClientIdentityNullWhenClientIsMissing() {
        CreditFileResponse response = mapper.toResponse(baseFile(), null);

        assertThat(response.getClientFullName()).isNull();
        assertThat(response.getClientCin()).isNull();
        assertThat(response.getClientId()).isEqualTo("c-1");
    }
}
