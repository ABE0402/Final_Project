package com.example.hong.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCafeSegmentScore_Id is a Querydsl query type for Id
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QCafeSegmentScore_Id extends BeanPath<CafeSegmentScore.Id> {

    private static final long serialVersionUID = 1260686879L;

    public static final QCafeSegmentScore_Id id = new QCafeSegmentScore_Id("id");

    public final NumberPath<Long> cafeId = createNumber("cafeId", Long.class);

    public final EnumPath<com.example.hong.domain.SegmentType> segmentType = createEnum("segmentType", com.example.hong.domain.SegmentType.class);

    public final StringPath segmentValue = createString("segmentValue");

    public QCafeSegmentScore_Id(String variable) {
        super(CafeSegmentScore.Id.class, forVariable(variable));
    }

    public QCafeSegmentScore_Id(Path<? extends CafeSegmentScore.Id> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCafeSegmentScore_Id(PathMetadata metadata) {
        super(CafeSegmentScore.Id.class, metadata);
    }

}

