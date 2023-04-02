insert into User (fullname, email, role, password)
values ('John Smith', 'john@mail.com', 'CUSTOMER', '123456'),
       ('Jack John', 'jack@mail.com', 'ADMIN', '123456'),
       ('Dan Samuel', 'Samuel@mail.com', 'CUSTOMER', '123456'),
       ('Will Taylor', 'will@mail.com', 'CUSTOMER', '123456'),
       ('Tanya Smith', 'tanya@mail.com', 'SELLER', '123456');

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
       ('laptop','A laptop computer or notebook computer, also known as a laptop or notebook
for short, is a small, portable personal computer (PC).');

insert into product (name, price, discount_id, category_id, seller_id, quantity)
values  ('Samsung m53', 17500, null, 1, 4, 15),
        ('Samsung a54', 22000, null, 1, 4, 20),
        ('Honor h2', 8500, null, 1, 4, 30),
        ('Asus vivoBook', 22000, null, 2, 4, 10);