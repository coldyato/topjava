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
        mealService.update(getUpdated(), USER_ID);
        assertMatch(mealService.get(MEAL_USER_ID, USER_ID), getUpdated());
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
        Meal meal = new Meal(meal1.getId(), meal1.getDateTime(), meal1.getDescription(), meal1.getCalories());
        assertThrows(NotFoundException.class, () -> mealService.update(meal, ADMIN_ID));
    }
    // endregion

    // region create meal
    @Test
    public void create() {
        Meal meal = getCreated();
        Meal create = mealService.create(getCreated(), USER_ID);
        meal.setId(create.getId());
        assertMatch(create, meal);
        assertMatch(mealService.get(create.getId(), USER_ID), meal);
        assertMatch(mealService.getAll(USER_ID), meal, meal7, meal6, meal5, meal4, meal3, meal2, meal1);
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
        assertMatch(mealService.getBetweenInclusive(startDate, endDate, USER_ID), meal3, meal2, meal1);
    }
    // endregion
}