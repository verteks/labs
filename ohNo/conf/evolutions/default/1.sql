# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table note (
  id                        bigint not null,
  name                      varchar(255),
  cell_phone                varchar(255),
  home_phone                varchar(255),
  constraint pk_note primary key (id))
;

create sequence note_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists note;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists note_seq;

