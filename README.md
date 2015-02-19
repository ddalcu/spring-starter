Simple starter app for Spring MVC (using spring boot)

I could not find any simple barebone starter apps that had a working authentication system, orm, and ready to go. 

Every example out there is bloated with a lot of unnecessary code.

To run, just clone it then..

    mvn spring-boot:run   (or run Application.java within eclipse)



Uses thymeleaf for templating, I find this template engine very easy to use and outputs clean html with error checking.
H2 or Mysql for DB, configure it via application.properties


Features:

* User Registration
* User Activation via e-mail link
* Password Reset via e-mail link
* User Admin when ROLE_ADMIN
* Edit Profile
* Upload Profile Picture
* Admins can login as other users using "Login As" link in the user editor.
