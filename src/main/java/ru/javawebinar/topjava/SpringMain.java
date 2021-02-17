package ru.javawebinar.topjava;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.javawebinar.topjava.model.Role;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.web.meal.MealRestController;
import ru.javawebinar.topjava.web.user.AdminRestController;

import java.util.Arrays;

public class SpringMain {
    public static void main(String[] args) {
        // java 7 automatic resource management (ARM)
        try (ConfigurableApplicationContext appCtx = new ClassPathXmlApplicationContext("spring/spring-app.xml")) {
            System.out.println(Arrays.toString(appCtx.getBeanDefinitionNames()));
            AdminRestController adminRestController = appCtx.getBean(AdminRestController.class);
            System.out.println(adminRestController
                    .create(new User(null, "username", "email", "password", Role.ADMIN)) + "\n");
            MealRestController controller = appCtx.getBean(MealRestController.class);
            controller.getAll().forEach(System.out::println);
        }
    }
}
