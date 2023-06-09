drop table if exists Order_details;
drop table if exists Product;
drop table if exists Discount;
drop table if exists Category;
drop table if exists Bonus;
drop table if exists Verification_Token;
drop table if exists My_Order;
drop table if exists User;

create table if not exists Bonus
(
    id     int primary key auto_increment,
    name   varchar(20) not null,
    amount int         not null,
    date   timestamp   not null default now()
);

create table if not exists User
(
    id             int primary key auto_increment,
    fullname       varchar(50)        not null,
    email          varchar(50) unique not null,
    role           varchar(15)        not null,
    bonuses_amount int default 0,
    password       varchar(100)       not null
);

create table if not exists Discount
(
    id               int primary key auto_increment,
    name             varchar(20),
    discount_percent int,
    check (discount_percent > 0 AND discount_percent < 101)
);

create table if not exists Category
(
    id          int primary key auto_increment,
    name        varchar(20) unique,
    description varchar(300)
);

create table if not exists Product
(
    id          int primary key auto_increment,
    name        varchar(30),
    price       int check (price > 0),
    discount_id int,
    category_id int,
    seller_id   int,
    bonus_id    int,
    quantity    int check (quantity > 0),
    foreign key (discount_id) references Discount (id)
        on delete cascade
        on update cascade,
    foreign key (category_id) references Category (id)
        on delete cascade
        on update cascade,
    foreign key (seller_id) references User (id)
        on delete cascade
        on update cascade,
    foreign key (bonus_id) references Bonus (id)
        on delete cascade
        on update cascade
);

create table if not exists My_Order
(
    id           int primary key auto_increment,
    customer_id  int,
    total_amount int,
    date         timestamp,
    status       varchar(7),
    foreign key (customer_id) references User (id)
        on delete cascade
        on update cascade
);

create table if not exists Order_details
(
    id         int primary key auto_increment,
    product_id int,
    order_id   int,
    quantity   int,
    foreign key (product_id) references Product (id)
        on delete cascade
        on update cascade,
    foreign key (order_id) references My_Order (id)
        on delete cascade
        on update cascade
);

create table if not exists Verification_Token
(
    id          int primary key auto_increment,
    user_id     int,
    token       varchar(255),
    expiry_date timestamp,
    foreign key (user_id) references User (id)
        on delete cascade
        on update cascade,
    constraint unique (id, user_id)
);