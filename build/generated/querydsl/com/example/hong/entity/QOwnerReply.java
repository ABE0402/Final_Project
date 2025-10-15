package com.example.hong.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOwnerReply is a Querydsl query type for OwnerReply
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOwnerReply extends EntityPathBase<OwnerReply> {

    private static final long serialVersionUID = 1866278053L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOwnerReply ownerReply = new QOwnerReply("ownerReply");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QUser owner;

    public final QReview review;

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QOwnerReply(String variable) {
        this(OwnerReply.class, forVariable(variable), INITS);
    }

    public QOwnerReply(Path<? extends OwnerReply> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOwnerReply(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOwnerReply(PathMetadata metadata, PathInits inits) {
        this(OwnerReply.class, metadata, inits);
    }

    public QOwnerReply(Class<? extends OwnerReply> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.owner = inits.isInitialized("owner") ? new QUser(forProperty("owner")) : null;
        this.review = inits.isInitialized("review") ? new QReview(forProperty("review"), inits.get("review")) : null;
    }

}

