package io.github.cainlara.pwdsaver.account;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

  /**
   * Explicit JPQL rather than a derived query name: Spring Data's method-name
   * parser otherwise reads "UsernameOrEmail" as the boolean OR of two separate
   * properties ("username", "email") instead of the single usernameOrEmail field.
   */
  @Query("SELECT u FROM UserAccount u WHERE u.usernameOrEmail = :usernameOrEmail")
  Optional<UserAccount> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
}
