package chapter07.akka_racingsimulation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.util.Random;

public class Racer extends AbstractBehavior<Racer.Command> {

    public interface Command extends Serializable {}

    public static class StartCommand implements Command {
        private static final long serialVersionUID = 1L;
        private int raceLength;

        public StartCommand(int raceLength) {
            this.raceLength = raceLength;
        }

        public int getRaceLength() {
            return raceLength;
        }
    }

    public static class PositionCommand implements Command {
        private static final long serialVersionUID = 1L;
        private ActorRef<RaceController.Command> controller;

        public PositionCommand(ActorRef<RaceController.Command> controller) {
            this.controller = controller;
        }

        public ActorRef<RaceController.Command> getController() {
            return controller;
        }
    }

    private Racer(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(context -> {
            return new Racer(context);
        });
    }

    private final double defaultAverageSpeed = 48.2;
    private int averageSpeedAdjustmentFactor;
    private Random random;

    private double currentSpeed = 0;

    private double getMaxSpeed() {
        return defaultAverageSpeed * (1+((double)averageSpeedAdjustmentFactor / 100));
    }

    private double getDistanceMovedPerSecond() {
        return currentSpeed * 1000 / 3600;
    }

    private void determineNextSpeed(int currentPosition, int raceLength) {
        if (currentPosition < (raceLength / 4)) {
            currentSpeed = currentSpeed  + (((getMaxSpeed() - currentSpeed) / 10) * random.nextDouble());
        }
        else {
            currentSpeed = currentSpeed * (0.5 + random.nextDouble());
        }

        if (currentSpeed > getMaxSpeed())
            currentSpeed = getMaxSpeed();

        if (currentSpeed < 5)
            currentSpeed = 5;

        if (currentPosition > (raceLength / 2) && currentSpeed < getMaxSpeed() / 2) {
            currentSpeed = getMaxSpeed() / 2;
        }
    }

    //private double currentPosition = 0;
    //private int raceLength;

    @Override
    public Receive<Command> createReceive() {
        return notYetStarted();
    }


    public Receive<Command> notYetStarted() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, message -> {
                    int raceLength = message.getRaceLength();
                    this.random = new Random();
                    this.averageSpeedAdjustmentFactor = random.nextInt(30) - 10;
                    return running(0, raceLength);
                })
                .onMessage(PositionCommand.class, message -> {
                    message.getController().tell(new RaceController.RacerUpdateCommand(getContext().getSelf(), 0));
                    return Behaviors.same();
                })
                .build();
    }

    public Receive<Command> running(int currentPos, int raceLength) {
        return newReceiveBuilder()
                .onMessage(PositionCommand.class, message -> {
                    int currentPosition = currentPos;

                    if(currentPosition >= raceLength) {
                        message.getController().tell(new RaceController.RacerUpdateCommand(getContext().getSelf(), currentPosition));
                        return completed(currentPosition);
                    }

                    determineNextSpeed(currentPosition, raceLength);
                    currentPosition += getDistanceMovedPerSecond();
                    if (currentPosition > raceLength )
                        currentPosition  = raceLength;
                    // tell controller our current position
                    message.getController().tell(
                            new RaceController.RacerUpdateCommand(getContext().getSelf(),
                                    (int)currentPosition)
                    );
                    return running(currentPosition, raceLength);
                })
                .build();
    }

    public Receive<Command> completed(int currentPos) {
        return newReceiveBuilder()
                .onMessage(PositionCommand.class, message -> {
                    message.getController().tell(new RaceController.RacerUpdateCommand(getContext().getSelf(), currentPos));
                    message.getController().tell(new RaceController.RacerFinishedCommand(getContext().getSelf()));
                    //return Behaviors.ignore(); // this behavior will ignore all future messages (it wont put it in a deadletter queue, it just ignores) sits there in an idle state and any new messages gets discarded
                    return waitingToStop();
                })
                .build();
    }

    public Receive<Command> waitingToStop() {
        return newReceiveBuilder()
                .onAnyMessage(message -> {
                    return Behaviors.same();
                })
                .onSignal(PostStop.class, signal -> { /** there are other signal types like PostStop.class */
                    // in this lambda block, close down any open resources gracefully before the parent of this actors shuts it down
                    System.out.println("I'm about to terminate! closing down resources that I may have open");
                    return Behaviors.same();
                })
                .build();
    }

}
