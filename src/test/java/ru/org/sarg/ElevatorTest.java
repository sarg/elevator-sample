package ru.org.sarg;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ElevatorTest {
    Elevator elevator;

    @Before
    public void init() {
        idle(0);
    }

    public void idle(int floor) {
        elevator = new Elevator(1, 10, 10, 1000);
        elevator.floor = floor;
    }

    @Test
    public void initIdle() {
        assertEquals(Elevator.State.IDLE, elevator.getState());
        assertEquals(0, elevator.floor);
    }

    @Test
    public void openDoorsFromIdle() {
        elevator.onPressCabinButton(elevator.floor);
    }

    public void assertNextState(Elevator.State state) {
        elevator.updateState();
        assertEquals(state, elevator.nextState);
    }

    @Test
    public void goTwoFloors() {
        elevator.onPressFloorButton(2);
        assertNextState(Elevator.State.FLOOR);
        assertNextState(Elevator.State.FLOOR);
        assertNextState(Elevator.State.OPENING);
    }

    @Test
    public void continueDirection() {
        idle(4);
        elevator.onPressCabinButton(5);
        assertNextState(Elevator.State.FLOOR);
        assertNextState(Elevator.State.OPENING);
        elevator.onPressFloorButton(3);
        elevator.onPressCabinButton(8);
        assertNextState(Elevator.State.CLOSING);
        assertNextState(Elevator.State.FLOOR);
        assertEquals(Elevator.Direction.UP, elevator.direction);
    }
}