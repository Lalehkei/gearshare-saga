package com.gearshare.payment.web;

import com.gearshare.payment.domain.PaymentAuthorization;
import com.gearshare.payment.service.PaymentSagaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentSagaService paymentSagaService;

    @GetMapping("/{rentalId}")
    public ResponseEntity<PaymentAuthorization> get(@PathVariable String rentalId) {
        return paymentSagaService.find(rentalId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
