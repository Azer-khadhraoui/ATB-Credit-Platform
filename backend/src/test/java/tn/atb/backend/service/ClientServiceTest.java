package tn.atb.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.atb.backend.dto.client.ClientCreateRequest;
import tn.atb.backend.dto.client.ClientResponse;
import tn.atb.backend.entity.Client;
import tn.atb.backend.entity.enums.AuditAction;
import tn.atb.backend.exception.DuplicateResourceException;
import tn.atb.backend.exception.ResourceNotFoundException;
import tn.atb.backend.mapper.ClientMapper;
import tn.atb.backend.repository.ClientRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the client service — guard clauses and the audit trail, mocked away from Mongo.
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock private ClientRepository clientRepository;
    @Mock private ClientMapper clientMapper;
    @Mock private AuditLogService auditLogService;

    @InjectMocks private ClientService clientService;

    @Test
    void rejectsADuplicateCinBeforeSaving() {
        ClientCreateRequest request = new ClientCreateRequest();
        request.setCin("12345678");
        when(clientRepository.existsByCin("12345678")).thenReturn(true);

        assertThatThrownBy(() -> clientService.createClient(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(clientRepository, never()).save(any());
    }

    @Test
    void createsClientAndWritesAnAuditLog() {
        ClientCreateRequest request = new ClientCreateRequest();
        request.setCin("87654321");
        request.setFirstName("Sana");
        request.setLastName("Ben Ali");
        when(clientRepository.existsByCin("87654321")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> {
            Client c = inv.getArgument(0);
            c.setId("new-id");
            return c;
        });
        when(clientMapper.toResponse(any(Client.class))).thenReturn(ClientResponse.builder().build());

        clientService.createClient(request);

        verify(auditLogService).log(eq(AuditAction.CREATE), eq("Client"), eq("new-id"), any());
    }

    @Test
    void getByIdThrowsWhenClientMissing() {
        when(clientRepository.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getClientById("nope"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteThrowsWhenClientMissing() {
        when(clientRepository.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.deleteClient("nope"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(clientRepository, never()).delete(any());
    }

    @Test
    void deleteRemovesClientAndLogsIt() {
        Client existing = Client.builder().id("c-9").firstName("Omar").lastName("Trabelsi").build();
        when(clientRepository.findById("c-9")).thenReturn(Optional.of(existing));

        clientService.deleteClient("c-9");

        verify(clientRepository).delete(existing);
        verify(auditLogService).log(eq(AuditAction.DELETE), eq("Client"), eq("c-9"), any());
    }
}
