# RestMarketPlaceApp

[![build](https://github.com/Wectro20/intellistart-java-2022-propositum/actions/workflows/maven.yml/badge.svg)](https://github.com/Wectro20/intellistart-java-2022-propositum/actions/workflows/maven.yml)

### About project
The Marketplace API is a robust RESTful API designed to power a dynamic online marketplace. With its comprehensive
functionality, users can seamlessly create accounts, log in to the system, browse and search for goods, make secure
purchases using the Stripe payment gateway, and conveniently view their purchase history.
### Key features
- Account Creation: Easily create an account to access all the marketplace features.
- Authentication: Securely log in to the system to enjoy personalized services.
- Product Search: Effortlessly find desired goods using powerful search capabilities.
- Seamless Purchases: Make purchases using the trusted Stripe payment gateway.
- Purchase History: Keep track of your transactions with a convenient purchase history feature.
- Bonus System: Benefit from a sophisticated bonus system to enhance user engagement.
### User Roles
The marketplace supports four distinct user roles:
- Unregistered User: Visitors who have not created an account yet.
- Customer: Registered users who can browse and purchase goods.
- Seller: Verified users who can list and manage their products.
- Admin: Administrators with elevated privileges for managing the marketplace.
### Getting Started
To get started with the Marketplace API, follow these steps:
- Register for an account on the platform.
- Log in using your credentials to receive a JWT token.
- Use the JWT token to access the API and enjoy the full range of marketplace features.

#### How to Run manually

This application is packaged as a jar which has Tomcat 8 embedded. No Tomcat or JBoss installation is necessary.</br> You run it using the ```java -jar``` command.

* Clone this repository
* Make sure you are using JDK 1.11 and Maven 3.x
* Make sure you are using MySQL 8.0
* Create Mysql database
``` 
create database "your_database_name"
```
*  Change mysql username, password and datasource as per your installation
    - open `src/main/resources/application.properties`
    - change `spring.datasource.username` , `spring.datasource.password` and `spring.datasource.url` as per your mysql installation
    - add your own jwt in `jwt:` must be 30 or more characters (lower/uppercase) without using special symbols

* You can build the project and run the tests by running ```mvn clean package```
* Once successfully built, you can run the project by this method:
```
java -jar target/interview-planning-0.0.1-SNAPSHOT.jar
```
The app will start running at http://localhost:8080.

### Sql diagram
![img.png](https://github.com/VovaMitsura/mitsura-rest-marketplace-app/assets/95585344/d1dbf269-a04d-4275-93b2-544929d39835)