package ru.org.sarg;

public class Elevator {

    enum State {
        FLOOR(" [:)] "),
        OPENING(" [:)] "), OPENED("[ :) ]"),
        CLOSING("[ :) ]"), CLOSED(" [:)] "),
        IDLE(" [  ] ");

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

        public static Direction forInc(int from, int to) {
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
    protected final int floors;

    int floor;
    Direction direction;
    State state;
    State nextState;

    public Elevator(double speed, double floorHeight, int floors, long openTime) {
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

    public boolean isIdle() {
        return state == State.IDLE && nextState == State.IDLE;
    }

    public State getState() {
        return state;
    }

    public void onPressCabinButton(int floor) {
        if (floor < 1 || floor > floors) {
            throw new IllegalArgumentException("Incorrect floor");
        }

        cabinButtons[floor-1] = Main.clock.now();
    }

    public void onPressFloorButton(int floor) {
        if (floor < 1 || floor > floors) {
            throw new IllegalArgumentException("Incorrect floor");
        }

        floorButtons[floor-1] = Main.clock.now();
    }

    // returns time in ms when to update state
    public long updateState() {
//        System.out.println("Update state: " + state + " to " + nextState);
//        System.out.println("Direction: " + direction);
        state = nextState;

        if (state == State.OPENING) {
            nextState = State.OPENED;
            return openTime;
        } else if (state == State.OPENED) {
            uncheckButtons();
            nextState = State.CLOSING;
            return delayTime;
        } else if (state == State.CLOSING) {
            nextState = State.CLOSED;
            return delayTime;
        }

        if (state == State.FLOOR) {
            floor += direction.inc;
        }

        if (needOpenDoors()) {
            nextState = State.OPENING;
            return 0;
        }

        direction = nextDirection();
        if (direction == Direction.NONE) {
            nextState = State.IDLE;
            return 0;
        } else {
            nextState = State.FLOOR;
            return floorTime;
        }
    }

    private boolean needOpenDoors() {
        return cabinButtons[floor] > 0 || floorButtons[floor] > 0;
    }

    private Direction nextDirection() {
        long minFloorPressTime = Long.MAX_VALUE;
        int minFloorButton = -1;

        int lowCabinButton = -1;
        int highCabinButton = -1;

        long minCabinPressTime = Long.MAX_VALUE;
        int minCabinButton = -1;

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
            if (direction == Direction.forInc(floor, lowCabinButton)
                    || direction == Direction.forInc(floor, highCabinButton)) {

                return direction;
            }
        }

        // go to earliest pressed cabin button
        if (minCabinButton != -1) {
            return Direction.forInc(floor, minCabinButton);
        }

        // go to earliest pressed floor button
        if (minFloorButton != -1) {
            return Direction.forInc(floor, minFloorButton);
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
        long now = Main.clock.now();
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
