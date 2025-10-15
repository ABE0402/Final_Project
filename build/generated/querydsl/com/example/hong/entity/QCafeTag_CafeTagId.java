package com.example.hong.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCafeTag_CafeTagId is a Querydsl query type for CafeTagId
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QCafeTag_CafeTagId extends BeanPath<CafeTag.CafeTagId> {

    private static final long serialVersionUID = -17689031L;

    public static final QCafeTag_CafeTagId cafeTagId = new QCafeTag_CafeTagId("cafeTagId");

    public final NumberPath<Long> cafeId = createNumber("cafeId", Long.class);

    public final NumberPath<Integer> tagId = createNumber("tagId", Integer.class);

    public QCafeTag_CafeTagId(String variable) {
        super(CafeTag.CafeTagId.class, forVariable(variable));
    }

    public QCafeTag_CafeTagId(Path<? extends CafeTag.CafeTagId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCafeTag_CafeTagId(PathMetadata metadata) {
        super(CafeTag.CafeTagId.class, metadata);
    }

}

