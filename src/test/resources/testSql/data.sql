insert into User (fullname, email, role, password)
values ('John Smith', 'john@mail.com', 'CUSTOMER',
        '$2a$10$8zXeu9h2RKWSh8nqSz4MfuURChXZLbnTzVGHZwPYwn6gOR7PBfiMW'),
       ('Jack John', 'jack@mail.com', 'ADMIN',
        '$2a$10$8zXeu9h2RKWSh8nqSz4MfuURChXZLbnTzVGHZwPYwn6gOR7PBfiMW'),
       ('Dan Samuel', 'Samuel@mail.com', 'CUSTOMER', '123456'),
       ('Will Taylor', 'will@mail.com', 'CUSTOMER', '123456'),
       ('Tanya Smith', 'tanya@mail.com', 'SELLER',
        '$2a$10$8zXeu9h2RKWSh8nqSz4MfuURChXZLbnTzVGHZwPYwn6gOR7PBfiMW');

insert into Bonus (name, date, amount)
values ('action', now(), 200),
       ('prize', now(), 400),
       ('complete', now(), 150);

insert into User_Bonus(user_id, bonus_id)
values (1, 1),
       (1, 2),
       (1, 3),
       (2, 2),
       (2, 3),
       (3, 1),
       (3, 3);

insert into category (name, description)
values ('smartphone', 'smart gadgets for teenagers'),
       ('laptop', 'A laptop computer or notebook computer, also known as a laptop or notebook
for short, is a small, portable personal computer (PC).');

insert into product (name, price, discount_id, category_id, seller_id, quantity)
values ('Samsung m53', 320, null, 1, 5, 15),
       ('Samsung a54', 370, null, 1, 5, 20),
       ('Honor h2', 225, null, 1, 5, 30),
       ('Asus vivoBook', 380, null, 2, 5, 10);

insert into my_order (customer_id, total_amount, date, status)
values (1, 8500, now(), 'CREATED');

insert into order_details (id, product_id, order_id, quantity)
values (1, 3, 1, 1);