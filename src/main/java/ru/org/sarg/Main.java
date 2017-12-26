package ru.org.sarg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Main {
    public static class Clock {
        public long now() {
            return System.nanoTime();
        }
    }

    private static Clock clock = new Clock();
    private static Elevator elevator;

    private static final String ANSI_CLEAR_SCREEN = "\u001B[0J";
    private static final String ANSI_ERASE_LINE = "\u001B[2K";
    private static final String ANSI_SAVE_CURSOR_POSITION = "\u001B7";
    private static final String ANSI_RESTORE_CURSOR_POSITION = "\u001B8";

    static volatile boolean exit = false;

    public static class InputHandler implements Runnable {
        private final BufferedReader r;

        public InputHandler() {
            r = new BufferedReader(new InputStreamReader(System.in));
            prompt();
        }

        private Optional<Integer> getFloor(String s) {
            try {
                int i = Integer.parseInt(s);

                if (i > 0 && i <= elevator.getFloors()) {
                    return Optional.of(i-1);
                }
            } catch (NumberFormatException e) {
            }

            return Optional.empty();
        }

        @Override
        public void run() {
            while (!exit) {
                try {
                    String s = r.readLine();
                    prompt();

                    if (s == null) { // handle C-d
                        continue;
                    }

                    exit = s.startsWith("q");

                    if (s.startsWith("c")) {
                        Optional<Integer> floor = getFloor(s.substring(1));
                        if (floor.isPresent()) {
                            elevator.onPressCabinButton(floor.get());
                        }
                    }

                    Optional<Integer> floor = getFloor(s);
                    if (floor.isPresent()) {
                        elevator.onPressFloorButton(floor.get());
                    }

                    synchronized (elevator) {
                        elevator.notify();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void prompt() {
        ansiGotoLine(2);
        System.out.print(ANSI_ERASE_LINE + "> ");
    }

    public static void ansiGotoLine(int l) {
        System.out.print("\u001B[" + l + ";1H");
    }

    public static void main(String[] args) throws InterruptedException {
        elevator = buildFromArgs(args);
        if (elevator == null) {
            help();
            System.exit(1);
        }

        ansiGotoLine(1);
        System.out.print(ANSI_CLEAR_SCREEN);
        System.out.println("<digit> for floor buttons, c<digit> for cabin buttons, q for exit");

        new Thread(new InputHandler()).start();

        long nextStateToggle = 0;
        while (!exit) {
            System.out.print(ANSI_SAVE_CURSOR_POSITION);

            ansiGotoLine(3);
            System.out.print(ANSI_CLEAR_SCREEN);

            long now = clock.now();
            long nextStateDelay;
            if (nextStateToggle <= now) {
                do {
                    nextStateDelay = elevator.updateState();
                } while (nextStateDelay == 0 && !elevator.isIdle());
                nextStateToggle = now + TimeUnit.MILLISECONDS.toNanos(nextStateDelay);
            } else {
                nextStateDelay = TimeUnit.NANOSECONDS.toMillis(nextStateToggle - now);
            }

            elevator.draw();

            System.out.println(String.format("%s -> %s in %d ms", elevator.getState(), elevator.getNextState(), nextStateDelay));
            System.out.print(ANSI_RESTORE_CURSOR_POSITION);

            synchronized (elevator) {
                elevator.wait(nextStateDelay);
            }
        }

        ansiGotoLine(1);
        System.out.print(ANSI_CLEAR_SCREEN);
    }

    private static void help() {
        System.out.println("args: floors(5-20) floorHeight(>0) speed(>0) openTime");
    }

    private static Elevator buildFromArgs(String[] args) {
        if (args.length == 4) {
            try {
                int floors = Integer.parseUnsignedInt(args[0]);
                double floorHeight = Double.parseDouble(args[1]);
                double speed = Double.parseDouble(args[2]);
                int openTime = Integer.parseUnsignedInt(args[3]);

                if (floors < 5 || floors > 20
                        || !(floorHeight > 0)
                        || !(speed > 0)) {

                    throw new IllegalArgumentException("Incorrect input");
                }

                return new Elevator(clock, speed, floorHeight, floors, TimeUnit.SECONDS.toMillis(openTime));
            } catch (NumberFormatException e) {
                System.out.println("Can't parse input " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Incorrect input, please check constraints");
            }
        }

        return null;
    }
}
