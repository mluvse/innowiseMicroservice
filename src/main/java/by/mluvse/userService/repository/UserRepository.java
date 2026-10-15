package by.mluvse.userService.repository;

import by.mluvse.userService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {

    @Modifying(clearAutomatically = true)
    @Query(value = "update users set active = :active, updated_at= now() where id =:id", nativeQuery = true)
    void setActive(@Param("id") Long id, @Param("active") boolean active);

    boolean existsByEmail( String email);
}
