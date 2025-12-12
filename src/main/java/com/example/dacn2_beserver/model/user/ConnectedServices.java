package com.example.dacn2_beserver.model.user;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectedServices {
    private ConnectedServiceInfo googleFit;
    private ConnectedServiceInfo appleHealth;
}
