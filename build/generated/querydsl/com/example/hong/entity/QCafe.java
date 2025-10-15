package com.example.hong.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCafe is a Querydsl query type for Cafe
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCafe extends EntityPathBase<Cafe> {

    private static final long serialVersionUID = 2123387083L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCafe cafe = new QCafe("cafe");

    public final StringPath addressRoad = createString("addressRoad");

    public final EnumPath<com.example.hong.domain.ApprovalStatus> approvalStatus = createEnum("approvalStatus", com.example.hong.domain.ApprovalStatus.class);

    public final NumberPath<java.math.BigDecimal> averageRating = createNumber("averageRating", java.math.BigDecimal.class);

    public final StringPath businessNumber = createString("businessNumber");

    public final ListPath<CafeTag, QCafeTag> cafeTags = this.<CafeTag, QCafeTag>createList("cafeTags", CafeTag.class, QCafeTag.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final NumberPath<Integer> favoritesCount = createNumber("favoritesCount", Integer.class);

    public final StringPath heroImageUrl = createString("heroImageUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isVisible = createBoolean("isVisible");

    public final NumberPath<java.math.BigDecimal> lat = createNumber("lat", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> lng = createNumber("lng", java.math.BigDecimal.class);

    public final StringPath menuImageUrl1 = createString("menuImageUrl1");

    public final StringPath menuImageUrl2 = createString("menuImageUrl2");

    public final StringPath menuImageUrl3 = createString("menuImageUrl3");

    public final StringPath menuImageUrl4 = createString("menuImageUrl4");

    public final StringPath menuImageUrl5 = createString("menuImageUrl5");

    public final ListPath<CafeMenu, QCafeMenu> menus = this.<CafeMenu, QCafeMenu>createList("menus", CafeMenu.class, QCafeMenu.class, PathInits.DIRECT2);

    public final StringPath menuText = createString("menuText");

    public final StringPath name = createString("name");

    public final StringPath operatingHours = createString("operatingHours");

    public final QUser owner;

    public final StringPath phone = createString("phone");

    public final StringPath postcode = createString("postcode");

    public final StringPath rejectionReason = createString("rejectionReason");

    public final NumberPath<Integer> reviewCount = createNumber("reviewCount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QCafe(String variable) {
        this(Cafe.class, forVariable(variable), INITS);
    }

    public QCafe(Path<? extends Cafe> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCafe(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCafe(PathMetadata metadata, PathInits inits) {
        this(Cafe.class, metadata, inits);
    }

    public QCafe(Class<? extends Cafe> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.owner = inits.isInitialized("owner") ? new QUser(forProperty("owner")) : null;
    }

}

