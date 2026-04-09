package io.github.no1evil.protogen.base;

import com.google.protobuf.Timestamp;
import java.time.Instant;

/**
 * Base mapper for Google Well-Known Types.
 */
public final class TimestampMapper {

  public static final TimestampMapper INSTANCE = new TimestampMapper();

  private TimestampMapper() {}

  /** @return {@link Instant} */
  public Instant toDomain(Timestamp proto) {
    if (proto == null) {
      return null;
    }
    return Instant.ofEpochSecond(proto.getSeconds(), proto.getNanos());
  }

  /** @return {@link com.google.protobuf.Timestamp} */
  public Timestamp toProto(Instant domain) {
    if (domain == null) {
      return Timestamp.getDefaultInstance();
    }
    return Timestamp.newBuilder()
        .setSeconds(domain.getEpochSecond())
        .setNanos(domain.getNano())
        .build();
  }
}