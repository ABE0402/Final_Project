package com.example.hong.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSearchEvent is a Querydsl query type for SearchEvent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSearchEvent extends EntityPathBase<SearchEvent> {

    private static final long serialVersionUID = 1746058980L;

    public static final QSearchEvent searchEvent = new QSearchEvent("searchEvent");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> searchedAt = createDateTime("searchedAt", java.time.LocalDateTime.class);

    public final StringPath searchQuery = createString("searchQuery");

    public final EnumPath<SearchEvent.SearchType> searchType = createEnum("searchType", SearchEvent.SearchType.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QSearchEvent(String variable) {
        super(SearchEvent.class, forVariable(variable));
    }

    public QSearchEvent(Path<? extends SearchEvent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSearchEvent(PathMetadata metadata) {
        super(SearchEvent.class, metadata);
    }

}

