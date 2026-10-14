package by.mluvse.userService.repository;

import by.mluvse.userService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Modifying(clearAutomatically = true)
    @Query(value = "update users set active = :active, updated_at= now() where id =:id", nativeQuery = true)
    int setActive(@Param("id") Long id, @Param("active") boolean active);
}
