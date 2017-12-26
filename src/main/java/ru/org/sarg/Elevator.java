package ru.org.sarg;

public class Elevator {

    private final Main.Clock clock;

    enum State {
        FLOOR  (" [:)] "),
        OPENED ("[ :) ]"),
        CLOSED (" [:)] "),
        IDLE   (" [  ] ");

        final String view;

        State(String view) {
            this.view = view;
        }
    }

    enum Direction {
        UP(1), DOWN(-1), NONE(0);

        final int inc;

        Direction(int inc) {
            this.inc = inc;
        }

        public static Direction fromTo(int from, int to) {
            if (from > to) {
                return Direction.DOWN;
            } else if (from < to) {
                return Direction.UP;
            } else {
                return Direction.NONE;
            }
        }
    }

    private final long[] cabinButtons;
    private final long[] floorButtons;

    private final long openTime;
    private final long delayTime;
    private final long floorTime;

    private final int floors;

    protected int floor;
    private Direction direction;
    private State state;
    private State nextState;

    public Elevator(Main.Clock clock, double speed, double floorHeight, int floors, long openTime) {
        this.clock = clock;
        this.floorTime = (long) (1000 * floorHeight / speed);
        this.openTime = (long) (openTime * 0.8);
        this.delayTime = (long) (openTime * 0.1);
        this.floors = floors;
        this.floorButtons = new long[floors];
        this.cabinButtons = new long[floors];
        this.state = State.IDLE;
        this.nextState = State.IDLE;
        this.direction = Direction.NONE;
        this.floor = 0;
    }

    public State getState() {
        return state;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getFloors() {
        return floors;
    }

    public State getNextState() {
        return nextState;
    }

    public int getFloor() {
        return floor;
    }

    public boolean isIdle() {
        return state == State.IDLE && nextState == State.IDLE;
    }

    public void onPressCabinButton(int floor) {
        if (floor < 0 || floor >= floors) {
            throw new IllegalArgumentException("Incorrect floor");
        }

        cabinButtons[floor] = clock.now();
    }

    public void onPressFloorButton(int floor) {
        if (floor < 0 || floor >= floors) {
            throw new IllegalArgumentException("Incorrect floor");
        }

        floorButtons[floor] = clock.now();
    }

    /**
     * Updates state and schedules next state update.
     * @return ms to wait until next update
     */
    public long updateState() {
        state = nextState;

        if (state == State.OPENED) {
            uncheckButtons();
            nextState = State.CLOSED;
            return openTime;
        }

        if (state == State.FLOOR) {
            floor += direction.inc;
        }

        if (needOpenDoors()) {
            nextState = State.OPENED;
            return delayTime;
        }

        direction = nextDirection();
        if (direction == Direction.NONE) {
            nextState = State.IDLE;
            return state == State.CLOSED ? delayTime : 0;
        } else {
            nextState = State.FLOOR;
            return floorTime;
        }
    }

    private boolean needOpenDoors() {
        return cabinButtons[floor] > 0 || floorButtons[floor] > 0;
    }

    /**
     * Strategy for selecting next direction.
     * @return new direction
     */
    private Direction nextDirection() {
        long minFloorPressTime = Long.MAX_VALUE;
        int minFloorButton = -1;

        long minCabinPressTime = Long.MAX_VALUE;
        int minCabinButton = -1;

        int lowCabinButton = -1;
        int highCabinButton = -1;

        for (int i = 0; i < floors; i++) {
            if (floorButtons[i] > 0) {
                if (minFloorPressTime > floorButtons[i]) {
                    minFloorPressTime = floorButtons[i];
                    minFloorButton = i;
                }
            }

            if (cabinButtons[i] > 0) {
                if (minCabinPressTime > cabinButtons[i]) {
                    minCabinPressTime = cabinButtons[i];
                    minCabinButton = i;
                }
            }

            if (cabinButtons[i] > 0) {
                if (lowCabinButton == -1) {
                    lowCabinButton = i;
                }

                highCabinButton = i;
            }
        }

        // priorities:
        // sustain direction
        if (direction != Direction.NONE && (lowCabinButton != -1 || highCabinButton != -1)) {
            if (direction == Direction.fromTo(floor, lowCabinButton)
                    || direction == Direction.fromTo(floor, highCabinButton)) {

                return direction;
            }
        }

        // go to earliest pressed cabin button
        if (minCabinButton != -1) {
            return Direction.fromTo(floor, minCabinButton);
        }

        // go to earliest pressed floor button
        if (minFloorButton != -1) {
            return Direction.fromTo(floor, minFloorButton);
        }

        // no commands
        return Direction.NONE;
    }

    private void uncheckButtons() {
        cabinButtons[floor] = 0;
        floorButtons[floor] = 0;
    }

    public void draw() {
        System.out.println("-= Elevator simulator =-");
        System.out.println("f c floor");
        long now = clock.now();
        for (int i = floors - 1; i >= 0; i--) {
            boolean floorPressed = floorButtons[i] > 0 && floorButtons[i] <= now;
            boolean cabinPressed = cabinButtons[i] > 0 && cabinButtons[i] <= now;

            System.out.println(String.format("%1s %1s %2d | %6s |",
                    floorPressed ? '*' : ' ',
                    cabinPressed ? '*' : ' ',
                    i+1,
                    floor == i ? state.view : ""
            ));
        }
    }

}
