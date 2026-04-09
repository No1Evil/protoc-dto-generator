# Features
## DTO/models auto generation

```proto
syntax = "proto3";

package io.github.no1evil.proto.v1.party;

option java_multiple_files = true;
option java_package = "io.github.no1evil.api.v1.party";
option java_outer_classname = "PartyCommandsProto";

import "v1/common/wrappers.proto";
import "v1/party/enums.proto";

message DisbandPartyRequest {
  io.github.no1evil.proto.v1.common.PartyId party_id = 1;
  io.github.no1evil.proto.v1.common.PlayerId actor_id = 2;
}
```

```java 
// Example generated class
package io.github.no1evil.api.v1.party.models;

import io.github.no1evil.api.v1.common.models.PartyId;
import io.github.no1evil.api.v1.common.models.PlayerId;

public record DisbandPartyRequest(PartyId partyId, PlayerId actorId) {
}

```

## Example OneOf model
```java
package io.github.no1evil.api.v1.player.models;

public record StatisticScope(Payload payload) {

    public sealed interface Payload {

        record GlobalValue(GlobalScope event) implements Payload {
        }

        record ServerGroupValue(ServerGroupScope event) implements Payload {
        }

        record ServerValue(ServerScope event) implements Payload {
        }
    }
}
```

## Enum generation

```proto
syntax = "proto3";

package  io.github.no1evil.api.v1.match;

option java_multiple_files = true;
option java_package = "io.github.no1evil.api.v1.match";
option java_outer_classname = "MatchEnumsProto";

enum MatchState {
  MATCH_STATE_UNSPECIFIED = 0;
  ENDED = 1;
  CANCELLED = 2;
}
```

```java
// Example generated enum
package io.github.no1evil.api.v1.match.models;

public enum MatchState {

    MATCH_STATE_UNSPECIFIED, ENDED, CANCELLED
}
```

## Mappers

```java
// Example Mapper
package io.github.no1evil.api.v1.party.mapper;

import io.github.no1evil.api.v1.common.models.PartyId;
import io.github.no1evil.api.v1.common.models.PlayerId;
import io.github.no1evil.api.v1.common.mapper.PartyIdMapper;
import io.github.no1evil.api.v1.common.mapper.PlayerIdMapper;

public final class DisbandPartyRequestMapper {

    public static final DisbandPartyRequestMapper INSTANCE = new DisbandPartyRequestMapper();

    private DisbandPartyRequestMapper() {
    }

    public io.github.no1evil.api.v1.party.models.DisbandPartyRequest toDomain(io.github.no1evil.api.v1.party.DisbandPartyRequest proto) {
        var partyId = proto.hasPartyId() ? io.github.no1evil.api.v1.common.mapper.PartyIdMapper.INSTANCE.toDomain(proto.getPartyId()) : null;
        var actorId = proto.hasActorId() ? io.github.no1evil.api.v1.common.mapper.PlayerIdMapper.INSTANCE.toDomain(proto.getActorId()) : null;
        return new io.github.no1evil.api.v1.party.models.DisbandPartyRequest(partyId, actorId);
    }

    public io.github.no1evil.api.v1.DisbandPartyRequest toProto(ru.beastmine.api.v1.party.models.DisbandPartyRequest domain) {
        var builder = io.github.no1evil.api.v1.DisbandPartyRequest.newBuilder();
        if (domain.partyId() != null)
            builder.setPartyId(io.github.no1evil.api.v1.common.mapper.PartyIdMapper.INSTANCE.toProto(domain.partyId()));
        if (domain.actorId() != null)
            builder.setActorId(io.github.no1evil.api.v1.common.mapper.PlayerIdMapper.INSTANCE.toProto(domain.actorId()));
        return builder.build();
    }
}
```

## Example OneOf mapper
```java
package io.github.no1evil.api.v1.player.mapper;

import io.github.no1evil.api.v1.player.models.ServerScope;
import io.github.no1evil.api.v1.player.models.GlobalScope;
import io.github.no1evil.api.v1.player.models.ServerGroupScope;

public final class StatisticScopeMapper {

  public static final StatisticScopeMapper INSTANCE = new StatisticScopeMapper();

  private StatisticScopeMapper() {
  }

  public io.github.no1evil.api.v1.player.models.StatisticScope toDomain(ru.beastmine.api.v1.player.StatisticScope proto) {
    var payload = switch(proto.getPayloadCase()) {
      case GLOBAL ->
          new io.github.no1evil.api.v1.player.models.StatisticScope.Payload.GlobalValue(proto.hasGlobal() ? ru.beastmine.api.v1.player.mapper.GlobalScopeMapper.INSTANCE.toDomain(proto.getGlobal()) : null);
      case SERVER_GROUP ->
          new io.github.no1evil.api.v1.player.models.StatisticScope.Payload.ServerGroupValue(proto.hasServerGroup() ? ru.beastmine.api.v1.player.mapper.ServerGroupScopeMapper.INSTANCE.toDomain(proto.getServerGroup()) : null);
      case SERVER ->
          new io.github.no1evil.api.v1.player.models.StatisticScope.Payload.ServerValue(proto.hasServer() ? ru.beastmine.api.v1.player.mapper.ServerScopeMapper.INSTANCE.toDomain(proto.getServer()) : null);
      case PAYLOAD_NOT_SET ->
          null;
      default ->
          null;
    };
    return new io.github.no1evil.api.v1.player.models.StatisticScope(payload);
  }

  public io.github.no1evil.api.v1.player.StatisticScope toProto(ru.beastmine.api.v1.player.models.StatisticScope domain) {
    var builder = io.github.no1evil.api.v1.player.StatisticScope.newBuilder();
    if (domain.payload() != null) {
      switch(domain.payload()) {
        case io.github.no1evil.api.v1.player.models.StatisticScope.Payload.GlobalValue p ->
            builder.setGlobal(io.github.no1evil.api.v1.player.mapper.GlobalScopeMapper.INSTANCE.toProto(p.event()));
        case io.github.no1evil.api.v1.player.models.StatisticScope.Payload.ServerGroupValue p ->
            builder.setServerGroup(io.github.no1evil.api.v1.player.mapper.ServerGroupScopeMapper.INSTANCE.toProto(p.event()));
        case io.github.no1evil.api.v1.player.models.StatisticScope.Payload.ServerValue p ->
            builder.setServer(io.github.no1evil.api.v1.player.mapper.ServerScopeMapper.INSTANCE.toProto(p.event()));
      }
    }
    return builder.build();
  }
}
```

## Service interface example
```java
package io.github.no1evil.api.v1.player.service;

import io.github.no1evil.api.v1.player.models.GetAchievementsResponse;
import io.github.no1evil.api.v1.player.models.UpdateAchievementProgressRequest;
import java.util.concurrent.CompletableFuture;
import io.github.no1evil.api.v1.player.models.GetAchievementsRequest;
import io.github.no1evil.api.v1.player.models.GrantAchievementRequest;

public interface AchievementsService {

    public CompletableFuture<GetAchievementsResponse> getAchievements(GetAchievementsRequest request);

    public CompletableFuture<Void> grantAchievement(GrantAchievementRequest request);

    public CompletableFuture<Void> updateAchievementProgress(UpdateAchievementProgressRequest request);
}


// Double tunneling
public interface OrchestratorService {

  public StreamSession<UpstreamServer> FabricTunnel(Consumer<DownstreamServer> responseObserver);
}

```