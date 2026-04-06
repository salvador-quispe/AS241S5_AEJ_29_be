package SalvadorQuispe_GenVideo.demo.repository;

import SalvadorQuispe_GenVideo.demo.entity.ApiRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApiRequestRepository extends JpaRepository<ApiRequest, Long> {
    Optional<ApiRequest> findByRequestUuid(String requestUuid);
    List<ApiRequest> findByTypeOrderByCreatedAtDesc(String type);
}
