Simple starter app for Spring MVC (using spring boot)

I could not find any simple barebone starter apps that had a working authentication system, orm, and ready to go. 

Every example out there is bloated with a lot of unnecessary code.

To run, just clone it then..

    mvn install
    java -jar target/app-0.0.1-SNAPSHOT.jar   (or run Application.java within eclipse)



Uses thymeleaf for templating, I find this template engine very easy to use and outputs clean html with error checking.
H2 or Mysql for DB, configure it via application.properties


TODO:

* Setup logging
* User edit capability via user list
* Remember me token
* Secure app url's
* Password reset - email sent message
* Make activation and password reset code uniform, urls, views and java code
