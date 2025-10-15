package com.example.hong.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCafeSegmentScore is a Querydsl query type for CafeSegmentScore
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCafeSegmentScore extends EntityPathBase<CafeSegmentScore> {

    private static final long serialVersionUID = 1668809610L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCafeSegmentScore cafeSegmentScore = new QCafeSegmentScore("cafeSegmentScore");

    public final QCafeSegmentScore_Id id;

    public final NumberPath<Double> score30d = createNumber("score30d", Double.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QCafeSegmentScore(String variable) {
        this(CafeSegmentScore.class, forVariable(variable), INITS);
    }

    public QCafeSegmentScore(Path<? extends CafeSegmentScore> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCafeSegmentScore(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCafeSegmentScore(PathMetadata metadata, PathInits inits) {
        this(CafeSegmentScore.class, metadata, inits);
    }

    public QCafeSegmentScore(Class<? extends CafeSegmentScore> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QCafeSegmentScore_Id(forProperty("id")) : null;
    }

}

