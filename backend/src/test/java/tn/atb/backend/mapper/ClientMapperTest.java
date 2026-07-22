package tn.atb.backend.mapper;

import org.junit.jupiter.api.Test;
import tn.atb.backend.dto.client.ClientResponse;
import tn.atb.backend.entity.Client;
import tn.atb.backend.entity.enums.Gender;
import tn.atb.backend.entity.enums.MaritalStatus;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ClientMapperTest {

    private final ClientMapper mapper = new ClientMapper();

    @Test
    void copiesEveryFieldOntoTheResponse() {
        Client client = Client.builder()
                .id("c-1")
                .cin("12345678")
                .firstName("Sana")
                .lastName("Ben Ali")
                .birthDate(LocalDate.of(1990, 5, 20))
                .gender(Gender.FEMALE)
                .maritalStatus(MaritalStatus.MARRIED)
                .dependents(2)
                .monthlyIncome(3200.0)
                .build();

        ClientResponse response = mapper.toResponse(client);

        assertThat(response.getId()).isEqualTo("c-1");
        assertThat(response.getCin()).isEqualTo("12345678");
        assertThat(response.getFirstName()).isEqualTo("Sana");
        assertThat(response.getLastName()).isEqualTo("Ben Ali");
        assertThat(response.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(response.getMaritalStatus()).isEqualTo(MaritalStatus.MARRIED);
        assertThat(response.getDependents()).isEqualTo(2);
        assertThat(response.getMonthlyIncome()).isEqualTo(3200.0);
    }
}
