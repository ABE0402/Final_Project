package com.example.hong.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCafeTag is a Querydsl query type for CafeTag
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCafeTag extends EntityPathBase<CafeTag> {

    private static final long serialVersionUID = 1546337999L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCafeTag cafeTag = new QCafeTag("cafeTag");

    public final QCafe cafe;

    public final QCafeTag_CafeTagId id;

    public final QTag tag;

    public QCafeTag(String variable) {
        this(CafeTag.class, forVariable(variable), INITS);
    }

    public QCafeTag(Path<? extends CafeTag> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCafeTag(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCafeTag(PathMetadata metadata, PathInits inits) {
        this(CafeTag.class, metadata, inits);
    }

    public QCafeTag(Class<? extends CafeTag> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.cafe = inits.isInitialized("cafe") ? new QCafe(forProperty("cafe"), inits.get("cafe")) : null;
        this.id = inits.isInitialized("id") ? new QCafeTag_CafeTagId(forProperty("id")) : null;
        this.tag = inits.isInitialized("tag") ? new QTag(forProperty("tag")) : null;
    }

}

