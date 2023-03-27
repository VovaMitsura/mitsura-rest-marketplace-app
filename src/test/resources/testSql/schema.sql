drop table if exists Bonus;
drop table if exists User_Bonus;
drop table if exists Order_details;
drop table if exists Product;
drop table if exists Discount;
drop table if exists My_Order;
drop table if exists Category;
drop table if exists User;

create table if not exists Bonus(
                                    id int primary key auto_increment,
                                    name varchar(20) not null,
                                    amount int not null,
                                    date timestamp not null default now()
);

create table if not exists  User(
                                    id int primary key auto_increment,
                                    fullname varchar (50) not null,
                                    email varchar (50) unique not null,
                                    role varchar (15) not null,
                                    password varchar(100) not null
);

create table if not exists  User_Bonus(
                                          user_id int,
                                          bonus_id int,
                                          foreign key (user_id) references User (id)
                                              on delete cascade
                                              on update cascade,
                                          primary key (user_id, bonus_id)
);

create table if not exists  Discount(
                                        id int primary key auto_increment,
                                        name varchar(20),
                                        discount_percent int,
                                        check (discount_percent > 0 AND discount_percent < 101)
);

create table if not exists  Category (
                                         id int primary key auto_increment,
                                         name varchar (20),
                                         description varchar (100)
);

create table if not exists  Product(
                                       id int primary key auto_increment,
                                       name varchar (30),
                                       price int check (price > 0),
                                       discount_id int,
                                       category_id int,
                                       seller_id int,
                                       quantity int check (quantity > 0),
                                       foreign key (discount_id) references Discount (id)
                                           on delete cascade
                                           on update cascade,
                                       foreign key (category_id) references Category (id)
                                           on delete cascade
                                           on update cascade,
                                       foreign key (seller_id) references User (id)
                                           on delete cascade
                                           on update cascade
);

create table if not exists My_Order (
                                       id int primary key auto_increment,
                                       customer_id int,
                                       city varchar(20),
                                       street varchar (50),
                                       zip varchar (10),
                                       contact_phone varchar (12),
                                       total_amount int,
                                       date timestamp,
                                       foreign key (customer_id) references User (id)
                                           on delete cascade
                                           on update cascade
);

create table if not exists  Order_details (
                                              id int primary key auto_increment,
                                              product_id int,
                                              order_id int,
                                              quantity int,
                                              foreign key (product_id) references Product (id)
                                                  on delete cascade
                                                  on update cascade,
                                              foreign key (order_id) references My_Order (id)
                                                  on delete cascade
                                                  on update cascade
);
