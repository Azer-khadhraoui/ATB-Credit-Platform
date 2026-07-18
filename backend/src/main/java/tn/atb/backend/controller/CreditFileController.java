package tn.atb.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.atb.backend.dto.creditfile.CreditFileCreateRequest;
import tn.atb.backend.dto.creditfile.CreditFileResponse;
import tn.atb.backend.dto.creditfile.CreditFileUpdateRequest;
import tn.atb.backend.service.CreditFileService;

import java.util.List;

@RestController
@RequestMapping("/api/credit-files")
@RequiredArgsConstructor
public class CreditFileController {

    private final CreditFileService creditFileService;

    @PostMapping
    public ResponseEntity<CreditFileResponse> createCreditFile(@Valid @RequestBody CreditFileCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(creditFileService.createCreditFile(request));
    }

    @GetMapping
    public ResponseEntity<List<CreditFileResponse>> getAllCreditFiles() {
        return ResponseEntity.ok(creditFileService.getAllCreditFiles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditFileResponse> getCreditFileById(@PathVariable String id) {
        return ResponseEntity.ok(creditFileService.getCreditFileById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreditFileResponse> updateCreditFile(@PathVariable String id, @Valid @RequestBody CreditFileUpdateRequest request) {
        return ResponseEntity.ok(creditFileService.updateCreditFile(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCreditFile(@PathVariable String id) {
        creditFileService.deleteCreditFile(id);
        return ResponseEntity.noContent().build();
    }
}
