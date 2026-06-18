package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class User {

    private UUID          id;
    private String        email;
    private String        role;
    private boolean       enabled;
    private LocalDateTime suspendedAt;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;

    public boolean isSuspended() { return suspendedAt != null; }
    public boolean isDeleted()   { return deletedAt   != null; }
}