package by.mluvse.userService.repository;

import by.mluvse.userService.entity.PaymentCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentCardRepository extends JpaRepository<PaymentCard,Long>, JpaSpecificationExecutor<PaymentCard> {
    List<PaymentCard> findByUserId(Long userId);
    Optional<PaymentCard> findById(Long id);
    boolean existsByNumber(String number);
    long countByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update payment_cards  set active = :active, updated_at = now() where id=: id", nativeQuery = true)
    int setActive(@Param("id") Long id, @Param("active") boolean active);

}
