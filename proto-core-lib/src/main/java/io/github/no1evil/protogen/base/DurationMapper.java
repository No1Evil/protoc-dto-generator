package io.github.no1evil.protogen.base;

import java.time.Duration;

/**
 * Base mapper for Google Well-Known Types.
 */
public final class DurationMapper {

  public static final DurationMapper INSTANCE = new DurationMapper();

  private DurationMapper() {
  }

  /** @return {@link java.time.Duration} */
  public Duration toDomain(com.google.protobuf.Duration proto) {
    if (proto == null) {
      return null;
    }
    return Duration.ofSeconds(proto.getSeconds(), proto.getNanos());
  }

  /** @return {@link com.google.protobuf.Duration} */
  public com.google.protobuf.Duration toProto(Duration domain) {
    if (domain == null) {
      return com.google.protobuf.Duration.getDefaultInstance();
    }
    return com.google.protobuf.Duration.newBuilder()
        .setSeconds(domain.getSeconds())
        .setNanos(domain.getNano())
        .build();
  }
}
