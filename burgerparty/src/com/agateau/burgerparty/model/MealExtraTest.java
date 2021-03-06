package com.agateau.burgerparty.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MealExtraTest {
    private static MealItem[] arrayFromMealExtra(MealExtra extra) {
        return extra.getItems().toArray(new MealItem[0]);
    }

    @Test
    public void testAdd() {
        MealItemDb db = TestUtils.createMealItemDb(
                            "<items>"
                            + "  <generic>"
                            + "    <item row='0' column='0' name='juice'    type='drink'/>"
                            + "    <item row='0' column='1' name='fries'    type='side-order'/>"
                            + "    <item row='0' column='2' name='potatoes' type='side-order'/>"
                            + "  </generic>"
                            + "</items>");
        MealItem juice = db.get("juice");
        MealItem fries = db.get("fries");
        MealItem potatoes = db.get("potatoes");

        MealExtra extra = new MealExtra();
        extra.addItem(fries);
        extra.addItem(juice);
        assertArrayEquals(new MealItem[] {fries, juice}, arrayFromMealExtra(extra));

        extra.addItem(fries);
        assertArrayEquals(new MealItem[] {fries, juice, fries}, arrayFromMealExtra(extra));

        extra.addItem(potatoes);
        assertArrayEquals(new MealItem[] {fries, juice, fries, potatoes}, arrayFromMealExtra(extra));
    }

    @Test
    public void testCompare() {
        MealItemDb db = TestUtils.createMealItemDb(
                            "<items>"
                            + "  <generic>"
                            + "    <item row='0' column='0' name='juice'    type='drink'/>"
                            + "    <item row='0' column='1' name='fries'    type='side-order'/>"
                            + "    <item row='0' column='2' name='potatoes' type='side-order'/>"
                            + "    <item row='0' column='3' name='soda'     type='drink'/>"
                            + "  </generic>"
                            + "</items>");
        MealItem juice = db.get("juice");
        MealItem fries = db.get("fries");
        MealItem potatoes = db.get("potatoes");
        MealItem soda = db.get("soda");

        MealExtra reference = new MealExtra();
        reference.addItem(fries);
        reference.addItem(juice);
        reference.addItem(potatoes);

        {
            MealExtra user = new MealExtra();
            user.addItem(fries);
            user.addItem(juice);
            assertEquals(MealExtra.CompareResult.SUBSET, user.compareTo(reference));
        }

        {
            // Add an item which is not in reference
            MealExtra user = new MealExtra();
            user.addItem(soda);
            assertEquals(MealExtra.CompareResult.DIFFERENT, user.compareTo(reference));
        }

        {
            // Reference has one fries, but user has two
            MealExtra user = new MealExtra();
            user.addItem(fries);
            user.addItem(fries);
            assertEquals(MealExtra.CompareResult.DIFFERENT, user.compareTo(reference));
        }

        {
            // Same as reference, but added in a different order
            MealExtra user = new MealExtra();
            user.addItem(juice);
            user.addItem(potatoes);
            user.addItem(fries);
            assertEquals(MealExtra.CompareResult.SAME, user.compareTo(reference));
        }
    }
}
