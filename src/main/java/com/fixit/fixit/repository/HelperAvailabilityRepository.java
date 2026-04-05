package com.fixit.fixit.repository;

import com.fixit.fixit.entity.HelperAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HelperAvailabilityRepository extends JpaRepository<HelperAvailability, Long> {

    List<HelperAvailability> findByHelperId(Long helperId);

    List<HelperAvailability> findByHelperIdAndIsActiveTrue(Long helperId);
}