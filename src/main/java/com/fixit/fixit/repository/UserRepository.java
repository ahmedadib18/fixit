package com.fixit.fixit.repository;

import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.AccountStatus;
import com.fixit.fixit.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    List<User> findByUserType(UserType userType);

    List<User> findByAccountStatus(AccountStatus accountStatus);
}