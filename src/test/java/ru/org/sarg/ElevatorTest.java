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
        elevator = new Elevator(Main.clock,1, 10, 10, 1000);
        elevator.floor = floor;
    }

    @Test
    public void initIdle() {
        idle(0);
        assertEquals(Elevator.State.IDLE, elevator.getState());
        assertEquals(0, elevator.getFloor());
    }

    @Test
    public void openDoorsFromIdle() {
        elevator.onPressCabinButton(elevator.getFloor());
        assertNextState(Elevator.State.OPENED);
        assertNextState(Elevator.State.CLOSED);
        assertNextState(Elevator.State.IDLE);
    }

    public void assertNextState(Elevator.State state) {
        elevator.updateState();
        assertEquals(state, elevator.getNextState());
    }

    @Test
    public void goTwoFloors() {
        elevator.onPressFloorButton(2);
        assertNextState(Elevator.State.FLOOR);
        assertNextState(Elevator.State.FLOOR);
        assertNextState(Elevator.State.OPENED);
        assertNextState(Elevator.State.CLOSED);
        assertNextState(Elevator.State.IDLE);
    }

    @Test
    public void continueDirection() {
        idle(4);
        elevator.onPressCabinButton(5);
        assertNextState(Elevator.State.FLOOR);
        assertNextState(Elevator.State.OPENED);
        elevator.onPressFloorButton(3);
        elevator.onPressCabinButton(8);
        assertNextState(Elevator.State.CLOSED);
        assertNextState(Elevator.State.FLOOR);
        assertEquals(Elevator.Direction.UP, elevator.getDirection());
    }

    @Test
    public void testCabinButtonPriority() {
        idle(5 );
        elevator.onPressFloorButton(3);
        elevator.onPressCabinButton(7);

        assertNextState(Elevator.State.FLOOR);
        assertEquals(Elevator.Direction.UP, elevator.getDirection());
    }

    @Test
    public void testCabinTimePriorityUp() {
        idle(5 );
        elevator.onPressCabinButton(7);
        elevator.onPressCabinButton(3);

        assertNextState(Elevator.State.FLOOR);
        assertEquals(Elevator.Direction.UP, elevator.getDirection());
    }

    @Test
    public void testCabinTimePriorityDown() {
        idle(5 );
        elevator.onPressCabinButton(3);
        elevator.onPressCabinButton(7);

        assertNextState(Elevator.State.FLOOR);
        assertEquals(Elevator.Direction.DOWN, elevator.getDirection());
    }

    @Test
    public void testFloorTimePriorityUp() {
        idle(5 );
        elevator.onPressFloorButton(7);
        elevator.onPressFloorButton(3);

        assertNextState(Elevator.State.FLOOR);
        assertEquals(Elevator.Direction.UP, elevator.getDirection());
    }

    @Test
    public void testFloorTimePriorityDown() {
        idle(5 );
        elevator.onPressFloorButton(3);
        elevator.onPressFloorButton(7);

        assertNextState(Elevator.State.FLOOR);
        assertEquals(Elevator.Direction.DOWN, elevator.getDirection());
    }
}