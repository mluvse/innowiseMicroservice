package by.mluvse.userService.repository;

import by.mluvse.userService.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {
    List<PaymentCard> findByUserId(Long userId);
    boolean existsByNumber(String number);
    long countByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update payment_cards  set active = :active, updated_at = now() where id=: id", nativeQuery = true)
    void setActive(@Param("id") Long id, @Param("active") boolean active);

}
