package ru.org.sarg;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ElevatorTest {
    Main.Elevator elevator;
    Main.Clock clock = new Main.Clock();

    @Before
    public void init() {
        elevator = new Main.Elevator(1, 10, 10);
    }

    @Test
    public void initIdle() {
        assertEquals(Main.Elevator.State.Action.IDLE, elevator.getState().action);
        assertEquals(0, elevator.getState().floor);
    }

    /**
     * press /floor/
     * IDLE -> OPENING when same floor button pressed
     */
    @Test
    public void openDoorsFromIdle() {
        elevator.onPressCabinButton(0);
    }

    /**
     * press 2
     * IDLE -> GOING_UP -> /1/ -> GOING_UP -> /2/
     */
    @Test
    public void goTwoFloors() {
        elevator.onPressFloorButton(2);
        // refresh
    }

    /**
     * GOING_UP -> /target/ -> STAYING -> OPENING
     */
    @Test
    public void openDoorsOnTargetFloor() {
        int a[] = { -1, -1, -1 };
        int min = 0; int max = 0; int sum = 0;
        for (int i = 0; i < a.length; i++) {
            min = Math.min(min, sum);
            sum = sum + a[i];
            max = Math.max(sum-min, max);
        }
        System.out.println(min + " " + sum + " " + max);

    }



}