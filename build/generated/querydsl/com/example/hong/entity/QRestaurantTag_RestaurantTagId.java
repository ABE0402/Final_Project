package com.example.hong.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRestaurantTag_RestaurantTagId is a Querydsl query type for RestaurantTagId
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QRestaurantTag_RestaurantTagId extends BeanPath<RestaurantTag.RestaurantTagId> {

    private static final long serialVersionUID = -1343219527L;

    public static final QRestaurantTag_RestaurantTagId restaurantTagId = new QRestaurantTag_RestaurantTagId("restaurantTagId");

    public final NumberPath<Long> restaurantId = createNumber("restaurantId", Long.class);

    public final NumberPath<Integer> tagId = createNumber("tagId", Integer.class);

    public QRestaurantTag_RestaurantTagId(String variable) {
        super(RestaurantTag.RestaurantTagId.class, forVariable(variable));
    }

    public QRestaurantTag_RestaurantTagId(Path<? extends RestaurantTag.RestaurantTagId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRestaurantTag_RestaurantTagId(PathMetadata metadata) {
        super(RestaurantTag.RestaurantTagId.class, metadata);
    }

}

