package traderalchemy.analyst.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserOpinionDto {
    
    @NotBlank
    String strategyClassName;
    
    @NotBlank
    String opinion;
}
