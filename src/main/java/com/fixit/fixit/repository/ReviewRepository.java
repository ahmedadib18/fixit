package com.fixit.fixit.repository;

import com.fixit.fixit.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByHelperIdOrderByCreatedAtDesc(Long helperId);

    Optional<Review> findBySessionId(String sessionId);

    List<Review> findByUserId(Long userId);

    boolean existsBySessionId(String sessionId);

    @Query("SELECT AVG(r.rating) FROM Review r " +
            "WHERE r.helper.id = :helperId")
    Double findAverageRatingByHelperId(@Param("helperId") Long helperId);
}