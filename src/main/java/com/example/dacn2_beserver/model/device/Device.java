package com.example.dacn2_beserver.model.device;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "devices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    private String id;

    private String userId;
    private DeviceType type;
    private String name;
    private String identifier;

    private DevicePlatform platform;

    private Instant lastSyncAt;
    private Instant createdAt;

    private Map<String, Object> metadata;
}