package tn.atb.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.atb.backend.dto.client.ClientCreateRequest;
import tn.atb.backend.dto.client.ClientResponse;
import tn.atb.backend.dto.client.ClientUpdateRequest;
import tn.atb.backend.entity.Client;
import tn.atb.backend.entity.enums.AuditAction;
import tn.atb.backend.exception.DuplicateResourceException;
import tn.atb.backend.exception.ResourceNotFoundException;
import tn.atb.backend.mapper.ClientMapper;
import tn.atb.backend.repository.ClientRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final AuditLogService auditLogService;

    public ClientResponse createClient(ClientCreateRequest request) {
        if (clientRepository.existsByCin(request.getCin())) {
            throw new DuplicateResourceException("A client with this CIN already exists");
        }

        Client client = Client.builder()
                .cin(request.getCin())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .maritalStatus(request.getMaritalStatus())
                .dependents(request.getDependents())
                .educationLevel(request.getEducationLevel())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .city(request.getCity())
                .propertyArea(request.getPropertyArea())
                .profession(request.getProfession())
                .employer(request.getEmployer())
                .employmentType(request.getEmploymentType())
                .monthlyIncome(request.getMonthlyIncome())
                .monthlyExpenses(request.getMonthlyExpenses())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Client saved = clientRepository.save(client);

        auditLogService.log(AuditAction.CREATE, "Client", saved.getId(),
                "Création du client " + saved.getFirstName() + " " + saved.getLastName());

        return clientMapper.toResponse(saved);
    }

    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponse)
                .toList();
    }

    public ClientResponse getClientById(String id) {
        Client client = findClientOrThrow(id);
        return clientMapper.toResponse(client);
    }

    public ClientResponse updateClient(String id, ClientUpdateRequest request) {
        Client client = findClientOrThrow(id);

        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setBirthDate(request.getBirthDate());
        client.setGender(request.getGender());
        client.setMaritalStatus(request.getMaritalStatus());
        client.setDependents(request.getDependents());
        client.setEducationLevel(request.getEducationLevel());
        client.setPhone(request.getPhone());
        client.setEmail(request.getEmail());
        client.setAddress(request.getAddress());
        client.setCity(request.getCity());
        client.setPropertyArea(request.getPropertyArea());
        client.setProfession(request.getProfession());
        client.setEmployer(request.getEmployer());
        client.setEmploymentType(request.getEmploymentType());
        client.setMonthlyIncome(request.getMonthlyIncome());
        client.setMonthlyExpenses(request.getMonthlyExpenses());
        client.setUpdatedAt(LocalDateTime.now());

        Client saved = clientRepository.save(client);

        auditLogService.log(AuditAction.UPDATE, "Client", saved.getId(),
                "Modification du client " + saved.getFirstName() + " " + saved.getLastName());

        return clientMapper.toResponse(saved);
    }

    public void deleteClient(String id) {
        Client client = findClientOrThrow(id);
        String fullName = client.getFirstName() + " " + client.getLastName();
        clientRepository.delete(client);

        auditLogService.log(AuditAction.DELETE, "Client", id, "Suppression du client " + fullName);
    }

    private Client findClientOrThrow(String id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}
