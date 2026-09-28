package com.gearshare.payment.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAuthorizationRepository extends JpaRepository<PaymentAuthorization, String> {
}
