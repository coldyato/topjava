package ru.javawebinar.topjava.web.meal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.service.MealService;
import ru.javawebinar.topjava.to.MealTo;
import ru.javawebinar.topjava.util.MealsUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static ru.javawebinar.topjava.util.ValidationUtil.assureIdConsistent;
import static ru.javawebinar.topjava.util.ValidationUtil.checkNew;
import static ru.javawebinar.topjava.web.SecurityUtil.authUserCaloriesPerDay;
import static ru.javawebinar.topjava.web.SecurityUtil.authUserId;

@Controller
public class MealRestController {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MealService service;

    public MealRestController(MealService service) {
        this.service = service;
    }

    public List<MealTo> getAll() {
        log.info("getAll meals of user id={}", authUserId());
        return service.getAll(authUserId(), authUserCaloriesPerDay());
    }

    public Meal get(int mealId) {
        log.info("get meal with id={}", mealId);
        return service.get(authUserId(), mealId);
    }

    public Meal create(Meal meal) {
        log.info("create meal {}", meal);
        checkNew(meal);
        return service.save(authUserId(), meal);
    }

    public void delete(int mealId) {
        log.info("delete meal with id={}", mealId);
        service.delete(authUserId(), mealId);
    }

    public void update(Meal meal, int mealId) {
        log.info("update meal {}", meal);
        assureIdConsistent(meal, mealId);
        service.update(authUserId(), meal);
    }

    public List<MealTo> getBetween(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
        log.info("filter meals from startDate {} to endDate {}", startDate, endDate);
        List<Meal> mealsFiltered = service.getBetween(authUserId(), startDate, endDate);
        return MealsUtil.getFilteredTos(mealsFiltered, authUserCaloriesPerDay(), startTime, endTime);
    }
}