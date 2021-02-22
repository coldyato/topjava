package ru.javawebinar.topjava.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.exception.NotFoundException;

import java.time.LocalDate;
import java.time.Month;

import static java.time.LocalDateTime.of;
import static org.junit.Assert.assertThrows;
import static ru.javawebinar.topjava.MealTestData.*;
import static ru.javawebinar.topjava.UserTestData.ADMIN_ID;
import static ru.javawebinar.topjava.UserTestData.USER_ID;

@ContextConfiguration({
        "classpath:spring/spring-app.xml",
        "classpath:spring/spring-db.xml"
})
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:db/populateDB.sql", config = @SqlConfig(encoding = "UTF-8"))
public class MealServiceTest {

    static {
        // Only for postgres driver logging
        // It uses java.util.logging and logged via jul-to-slf4j bridge
        SLF4JBridgeHandler.install();
    }

    @Autowired
    private MealService mealService;

    // positive region
    @Test
    public void get() {
        Meal meal = mealService.get(MEAL_USER_ID, USER_ID);
        assertMatch(meal, MEAL1);
    }

    @Test
    public void delete() {
        mealService.delete(MEAL_USER_ID, USER_ID);
        assertMatch(mealService.getAll(USER_ID), MEAL6, MEAL5, MEAL4, MEAL3, MEAL2);
    }

    @Test
    public void getAll() {
        assertMatch(MEALS_USER, mealService.getAll(USER_ID));
        assertMatch(MEALS_ADMIN, mealService.getAll(ADMIN_ID));
    }

    @Test
    public void update() {
        Meal updated = new Meal(MEAL_USER_ID, MEAL1.getDateTime(), "updated", 4000);
        mealService.update(updated, USER_ID);
        assertMatch(mealService.get(MEAL_USER_ID, USER_ID), updated);
    }

    @Test
    public void create() {
        Meal meal = new Meal(null, of(2015, Month.JUNE, 1, 18, 0), "createdx", 300);
        Meal create = mealService.create(meal, USER_ID);
        meal.setId(create.getId());
        assertMatch(meal, create);
    }

    @Test
    public void getBetweenInclusive() {
        assertMatch(mealService.getBetweenInclusive(LocalDate.of(2015, Month.MAY, 27),
                LocalDate.of(2015, Month.MAY, 29), USER_ID), MEAL1);
    }
    // end region

    // region negative
    @Test
    public void createAsDuplicateDateTimeCreate() {
        Meal mealFirst = new Meal(null, of(2015, Month.JUNE, 1, 18, 0), "createdFirst", 300);
        Meal mealSecond = new Meal(null, of(2015, Month.JUNE, 1, 18, 0), "createdSecond", 300);
        Meal createFirst = mealService.create(mealFirst, USER_ID);
        mealFirst.setId(createFirst.getId());
        assertMatch(mealFirst, createFirst);

        assertThrows(DuplicateKeyException.class, () -> mealService.create(mealSecond, USER_ID));
    }

    @Test
    public void updateAsDuplicateDateTime() {
        Meal updated = new Meal(MEAL_USER_ID, MEAL2.getDateTime(), "duplicateUpdate", 4000);
        assertThrows(DuplicateKeyException.class, () -> mealService.update(updated, USER_ID));
    }

    @Test
    public void getAsNotFound() {
        assertThrows(NotFoundException.class, () -> mealService.get(32323123, USER_ID));
    }

    @Test
    public void deleteAsNotFoundBy() {
        assertThrows(NotFoundException.class, () -> mealService.delete(32323123, USER_ID));
    }

    @Test
    public void updateAsNotFound() {
        assertThrows(NotFoundException.class, () -> mealService.get(32323123, USER_ID));
    }

    @Test
    public void getAsByNotOwnerNotFound() {
        assertThrows(NotFoundException.class, () -> mealService.get(MEAL_USER_ID, ADMIN_ID));
    }

    @Test
    public void deleteByNotOwnerAsNotFound() {
        assertThrows(NotFoundException.class, () -> mealService.delete(MEAL_USER_ID, ADMIN_ID));
    }

    @Test
    public void updateByNotOwnerAsNotFound() {
        assertThrows(NotFoundException.class, () -> mealService.get(MEAL_USER_ID, ADMIN_ID));
    }
    // end region
}