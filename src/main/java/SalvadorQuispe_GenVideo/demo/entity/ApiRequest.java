package SalvadorQuispe_GenVideo.demo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("api_requests")
public class ApiRequest {

    @Id
    private Long id;

    @Column("request_uuid")
    private String requestUuid;

    private String type;

    @Column("input_data")
    private String inputData;

    @Column("result_data")
    private String resultData;

    private String status = "pending";

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("completed_at")
    private LocalDateTime completedAt;
}
