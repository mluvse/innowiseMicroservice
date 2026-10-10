package by.mluvse.innowiseMicroservice.repository;

import by.mluvse.innowiseMicroservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard,Long> {
    List<PaymentCard> findByUserId(Long userId);
}
