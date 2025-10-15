package com.example.hong.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSearchEventSelection is a Querydsl query type for SearchEventSelection
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSearchEventSelection extends EntityPathBase<SearchEventSelection> {

    private static final long serialVersionUID = -1330228472L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSearchEventSelection searchEventSelection = new QSearchEventSelection("searchEventSelection");

    public final QSearchEvent searchEvent;

    public final QTag tag;

    public QSearchEventSelection(String variable) {
        this(SearchEventSelection.class, forVariable(variable), INITS);
    }

    public QSearchEventSelection(Path<? extends SearchEventSelection> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSearchEventSelection(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSearchEventSelection(PathMetadata metadata, PathInits inits) {
        this(SearchEventSelection.class, metadata, inits);
    }

    public QSearchEventSelection(Class<? extends SearchEventSelection> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.searchEvent = inits.isInitialized("searchEvent") ? new QSearchEvent(forProperty("searchEvent")) : null;
        this.tag = inits.isInitialized("tag") ? new QTag(forProperty("tag")) : null;
    }

}

