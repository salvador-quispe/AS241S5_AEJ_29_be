package SalvadorQuispe_GenVideo.demo.repository;

import SalvadorQuispe_GenVideo.demo.entity.ApiRequest;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ApiRequestRepository extends ReactiveCrudRepository<ApiRequest, Long> {
    Mono<ApiRequest> findByRequestUuid(String requestUuid);
    Flux<ApiRequest> findByTypeOrderByCreatedAtDesc(String type);
}
