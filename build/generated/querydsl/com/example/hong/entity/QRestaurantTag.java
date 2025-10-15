package com.example.hong.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRestaurantTag is a Querydsl query type for RestaurantTag
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRestaurantTag extends EntityPathBase<RestaurantTag> {

    private static final long serialVersionUID = 1737307055L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRestaurantTag restaurantTag = new QRestaurantTag("restaurantTag");

    public final QRestaurantTag_RestaurantTagId id;

    public final QRestaurant restaurant;

    public final QTag tag;

    public QRestaurantTag(String variable) {
        this(RestaurantTag.class, forVariable(variable), INITS);
    }

    public QRestaurantTag(Path<? extends RestaurantTag> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRestaurantTag(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRestaurantTag(PathMetadata metadata, PathInits inits) {
        this(RestaurantTag.class, metadata, inits);
    }

    public QRestaurantTag(Class<? extends RestaurantTag> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QRestaurantTag_RestaurantTagId(forProperty("id")) : null;
        this.restaurant = inits.isInitialized("restaurant") ? new QRestaurant(forProperty("restaurant"), inits.get("restaurant")) : null;
        this.tag = inits.isInitialized("tag") ? new QTag(forProperty("tag")) : null;
    }

}

