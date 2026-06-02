package pk.jl.pasir_lebda_jan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupNotificationDTO {
    private String type;
    private Long groupId;
    private String groupName;
    private String title;
    private Double amount;
    private Double userShare;
    private String createdByEmail;
    private String message;
}
