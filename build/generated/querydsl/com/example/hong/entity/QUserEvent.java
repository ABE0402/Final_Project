package com.example.hong.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserEvent is a Querydsl query type for UserEvent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserEvent extends EntityPathBase<UserEvent> {

    private static final long serialVersionUID = -1720011999L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserEvent userEvent = new QUserEvent("userEvent");

    public final EnumPath<com.example.hong.domain.EventAction> action = createEnum("action", com.example.hong.domain.EventAction.class);

    public final QCafe cafe;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> ratingValue = createNumber("ratingValue", Integer.class);

    public final QUser user;

    public QUserEvent(String variable) {
        this(UserEvent.class, forVariable(variable), INITS);
    }

    public QUserEvent(Path<? extends UserEvent> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserEvent(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserEvent(PathMetadata metadata, PathInits inits) {
        this(UserEvent.class, metadata, inits);
    }

    public QUserEvent(Class<? extends UserEvent> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.cafe = inits.isInitialized("cafe") ? new QCafe(forProperty("cafe"), inits.get("cafe")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

