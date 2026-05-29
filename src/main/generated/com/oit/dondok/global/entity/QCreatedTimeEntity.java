package com.oit.dondok.global.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import javax.annotation.processing.Generated;

/** QCreatedTimeEntity is a Querydsl query type for CreatedTimeEntity */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QCreatedTimeEntity extends EntityPathBase<CreatedTimeEntity> {

  private static final long serialVersionUID = 242153704L;

  public static final QCreatedTimeEntity createdTimeEntity =
      new QCreatedTimeEntity("createdTimeEntity");

  public final DateTimePath<java.time.LocalDateTime> createdAt =
      createDateTime("createdAt", java.time.LocalDateTime.class);

  public QCreatedTimeEntity(String variable) {
    super(CreatedTimeEntity.class, forVariable(variable));
  }

  public QCreatedTimeEntity(Path<? extends CreatedTimeEntity> path) {
    super(path.getType(), path.getMetadata());
  }

  public QCreatedTimeEntity(PathMetadata metadata) {
    super(CreatedTimeEntity.class, metadata);
  }
}
