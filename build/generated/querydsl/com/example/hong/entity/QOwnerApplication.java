package com.example.hong.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOwnerApplication is a Querydsl query type for OwnerApplication
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOwnerApplication extends EntityPathBase<OwnerApplication> {

    private static final long serialVersionUID = -1035493685L;

    public static final QOwnerApplication ownerApplication = new QOwnerApplication("ownerApplication");

    public final StringPath businessNumber = createString("businessNumber");

    public final StringPath contactPhone = createString("contactPhone");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath ownerRealName = createString("ownerRealName");

    public final StringPath rejectionReason = createString("rejectionReason");

    public final DateTimePath<java.time.LocalDateTime> reviewedAt = createDateTime("reviewedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> reviewedBy = createNumber("reviewedBy", Long.class);

    public final EnumPath<com.example.hong.domain.ApprovalStatus> status = createEnum("status", com.example.hong.domain.ApprovalStatus.class);

    public final StringPath storeAddress = createString("storeAddress");

    public final StringPath storeName = createString("storeName");

    public final EnumPath<com.example.hong.domain.StoreType> storeType = createEnum("storeType", com.example.hong.domain.StoreType.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QOwnerApplication(String variable) {
        super(OwnerApplication.class, forVariable(variable));
    }

    public QOwnerApplication(Path<? extends OwnerApplication> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOwnerApplication(PathMetadata metadata) {
        super(OwnerApplication.class, metadata);
    }

}

