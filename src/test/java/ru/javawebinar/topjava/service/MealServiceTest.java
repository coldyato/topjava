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
import ru.javawebinar.topjava.util.Util;
import ru.javawebinar.topjava.util.exception.NotFoundException;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.of;
import static org.junit.Assert.assertThrows;
import static ru.javawebinar.topjava.MealTestData.*;
import static ru.javawebinar.topjava.UserTestData.ADMIN_ID;
import static ru.javawebinar.topjava.UserTestData.USER_ID;

@ContextConfiguration({
        "classpath:spring/spring-app.xml",
        "classpath:spring/spring-db.xml",
        "classpath:spring/spring-rep.xml"
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

    // region get all meals
    @Test
    public void getAllUserMeals() {
        assertMatch(userMeals, mealService.getAll(USER_ID));
        assertMatch(adminMeals, mealService.getAll(ADMIN_ID));
    }
    // endregion

    // region get meal
    @Test
    public void get() {
        Meal meal = mealService.get(MEAL_USER_ID, USER_ID);
        assertMatch(meal, meal1);
    }

    @Test
    public void getByNotOwnerAsNotFound() {
        assertThrows(NotFoundException.class, () -> mealService.get(MEAL_USER_ID, ADMIN_ID));
    }

    @Test
    public void getAsNotFound() {
        assertThrows(NotFoundException.class, () -> mealService.get(NOT_FOUND, USER_ID));
    }
    // endregion

    // region delete meal
    @Test
    public void delete() {
        mealService.delete(MEAL_USER_ID, USER_ID);
        assertMatch(mealService.getAll(USER_ID), meal7, meal6, meal5, meal4, meal3, meal2);
    }

    @Test
    public void deleteAsNotFoundBy() {
        assertThrows(NotFoundException.class, () -> mealService.delete(NOT_FOUND, USER_ID));
    }

    @Test
    public void deleteByNotOwnerAsNotFound() {
        assertThrows(NotFoundException.class, () -> mealService.delete(MEAL_USER_ID, ADMIN_ID));
    }

    @Test
    public void deleteAfterDeletedMealAsNotFound() {
        mealService.delete(MEAL_USER_ID, USER_ID);
        assertMatch(mealService.getAll(USER_ID), meal7, meal6, meal5, meal4, meal3, meal2);
        assertThrows(NotFoundException.class, () -> mealService.delete(MEAL_USER_ID, USER_ID));
    }
    // endregion

    // region update meal
    @Test
    public void update() {
        Meal updated = new Meal(MEAL_USER_ID, of(2015, Month.APRIL, 30, 13, 0), "updated", 4000);
        mealService.update(updated, USER_ID);
        assertMatch(mealService.get(MEAL_USER_ID, USER_ID), updated);
    }

    @Test
    public void updateAsDuplicateDateTime() {
        Meal updated = new Meal(MEAL_USER_ID, meal2.getDateTime(), "duplicateUpdate", 4000);
        assertThrows(DuplicateKeyException.class, () -> mealService.update(updated, USER_ID));
    }

    @Test
    public void updateAsNotFound() {
        Meal updated = new Meal(NOT_FOUND, of(2015, Month.APRIL, 24, 13, 0), "duplicateUpdate", 4000);
        assertThrows(NotFoundException.class, () -> mealService.update(updated, USER_ID));
    }

    @Test
    public void updateByNotOwnerAsNotFound() {
        assertThrows(NotFoundException.class, () -> mealService.update(meal1, ADMIN_ID));
    }
    // endregion

    // region create meal
    @Test
    public void create() {
        Meal meal = new Meal(null, of(2015, Month.JUNE, 1, 18, 0), "created", 300);
        Meal create = mealService.create(meal, USER_ID);
        meal.setId(create.getId());
        assertMatch(mealService.get(create.getId(), USER_ID), meal);
    }

    @Test
    public void createAsDuplicateDateTimeCreate() {
        Meal meal = new Meal(null, meal1.getDateTime(), "duplicate", 300);
        assertThrows(DuplicateKeyException.class, () -> mealService.create(meal, USER_ID));
    }
    // endregion

    // region between
    @Test
    public void getBetweenInclusive() {
        assertMatch(mealService.getBetweenInclusive(LocalDate.of(2015, Month.MAY, 27),
                LocalDate.of(2015, Month.MAY, 29), USER_ID), meal1);
    }

    @Test
    public void sortBetweenInclusive() {
        assertMatch(mealService.getBetweenInclusive(LocalDate.of(2015, Month.MAY, 1),
                LocalDate.of(2015, Month.JUNE, 30), USER_ID), userMeals);
    }

    @Test
    public void getBetweenInclusiveDefault() {
        assertMatch(mealService.getBetweenInclusive(null, null, USER_ID), userMeals);
    }

    @Test
    public void getBetweenInclusiveInScope() {
        LocalDate startDate = LocalDate.of(2015, Month.MAY, 20);
        LocalDate endDate = LocalDate.of(2015, Month.MAY, 30);
        List<Meal> meals = userMeals.stream()
                .filter(meal -> Util.isBetweenHalfOpen(meal.getDate(), startDate, endDate.plus(1, ChronoUnit.DAYS)))
                .collect(Collectors.toList());
        assertMatch(mealService.getBetweenInclusive(startDate, endDate, USER_ID), meals);
    }
    // endregion
}