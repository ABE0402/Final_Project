package com.example.hong.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRestaurant is a Querydsl query type for Restaurant
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRestaurant extends EntityPathBase<Restaurant> {

    private static final long serialVersionUID = -451049493L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRestaurant restaurant = new QRestaurant("restaurant");

    public final StringPath addressRoad = createString("addressRoad");

    public final EnumPath<com.example.hong.domain.ApprovalStatus> approvalStatus = createEnum("approvalStatus", com.example.hong.domain.ApprovalStatus.class);

    public final NumberPath<java.math.BigDecimal> averageRating = createNumber("averageRating", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final NumberPath<Integer> favoritesCount = createNumber("favoritesCount", Integer.class);

    public final StringPath heroImageUrl = createString("heroImageUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isVisible = createBoolean("isVisible");

    public final NumberPath<java.math.BigDecimal> lat = createNumber("lat", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> lng = createNumber("lng", java.math.BigDecimal.class);

    public final StringPath name = createString("name");

    public final QUser owner;

    public final StringPath phone = createString("phone");

    public final StringPath postcode = createString("postcode");

    public final StringPath rejectionReason = createString("rejectionReason");

    public final ListPath<RestaurantTag, QRestaurantTag> restaurantTags = this.<RestaurantTag, QRestaurantTag>createList("restaurantTags", RestaurantTag.class, QRestaurantTag.class, PathInits.DIRECT2);

    public final NumberPath<Integer> reviewCount = createNumber("reviewCount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QRestaurant(String variable) {
        this(Restaurant.class, forVariable(variable), INITS);
    }

    public QRestaurant(Path<? extends Restaurant> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRestaurant(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRestaurant(PathMetadata metadata, PathInits inits) {
        this(Restaurant.class, metadata, inits);
    }

    public QRestaurant(Class<? extends Restaurant> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.owner = inits.isInitialized("owner") ? new QUser(forProperty("owner")) : null;
    }

}

