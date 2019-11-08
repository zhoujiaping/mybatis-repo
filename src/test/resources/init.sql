create database test;
use test;
create table t_demo(
    demo_id bigint primary key auto_increment,
    version int not null default 1,
    creator varchar(36),
    create_time datetime not null default now(),
    modifier varchar(36),
    update_time datetime not null default now()
);