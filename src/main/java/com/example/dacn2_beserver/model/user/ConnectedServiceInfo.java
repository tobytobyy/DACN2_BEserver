package com.example.dacn2_beserver.model.user;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectedServiceInfo {
    private boolean connected;
    private Instant lastSyncAt;
}
