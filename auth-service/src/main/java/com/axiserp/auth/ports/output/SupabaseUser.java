package com.axiserp.auth.ports.output;

import java.time.Instant;
import java.util.UUID;

public record SupabaseUser(UUID id, String email, Instant invitedAt) {}
