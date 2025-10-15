package com.example.hong.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReviewAspectScore is a Querydsl query type for ReviewAspectScore
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReviewAspectScore extends EntityPathBase<ReviewAspectScore> {

    private static final long serialVersionUID = -862904780L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReviewAspectScore reviewAspectScore = new QReviewAspectScore("reviewAspectScore");

    public final StringPath aspect = createString("aspect");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QReview review;

    public final NumberPath<java.math.BigDecimal> score = createNumber("score", java.math.BigDecimal.class);

    public QReviewAspectScore(String variable) {
        this(ReviewAspectScore.class, forVariable(variable), INITS);
    }

    public QReviewAspectScore(Path<? extends ReviewAspectScore> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReviewAspectScore(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReviewAspectScore(PathMetadata metadata, PathInits inits) {
        this(ReviewAspectScore.class, metadata, inits);
    }

    public QReviewAspectScore(Class<? extends ReviewAspectScore> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.review = inits.isInitialized("review") ? new QReview(forProperty("review"), inits.get("review")) : null;
    }

}

