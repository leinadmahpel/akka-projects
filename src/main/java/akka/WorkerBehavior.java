package akka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

    //private BigInteger prime; // Akka says don't use class-level state

    public static class Command implements Serializable {

        private static final long serialVersionUID = 1L;
        private String message;
        private ActorRef<ManagerBehavior.Command> sender;

        public Command(String message, ActorRef<ManagerBehavior.Command> sender) {
            this.message = message;
            this.sender = sender;
        }

        public String getMessage() {
            return message;
        }

        public ActorRef<ManagerBehavior.Command> getSender() {
            return sender;
        }
    }

    private WorkerBehavior(ActorContext<WorkerBehavior.Command> context) {
        super(context);
        //this.managerBehavior = managerBehavior;
    }

    public static Behavior<WorkerBehavior.Command> create() {
        return Behaviors.setup(context -> {
            return new WorkerBehavior(context);
        });
    }

    @Override
    public Receive<Command> createReceive() {
        return handleMessagesWhenWeDontHaveAPrimeNumber();
    }

    public Receive<WorkerBehavior.Command> handleMessagesWhenWeDontHaveAPrimeNumber() {
        return newReceiveBuilder()
                .onAnyMessage(command -> {
                    BigInteger prime = null;
//                    if(command.getMessage().equals("start")) {

                        BigInteger bigInt = new BigInteger(2000, new Random());
                        prime = bigInt.nextProbablePrime();

                        command.getSender().tell(new ManagerBehavior.ResultCommand(prime));
//                    }
                    return handleMessagesWhenWeAlreadyHaveAPrimeNumber(prime);
                    //return this; // return this fuctions the same as return Behaviors.same();
                })
                .build();
    }

    public Receive<Command> handleMessagesWhenWeAlreadyHaveAPrimeNumber(BigInteger prime) {
        return newReceiveBuilder()
                .onAnyMessage(command -> {
//                    if(command.getMessage().equals("start")) {
                        command.getSender().tell(new ManagerBehavior.ResultCommand(prime));
//                    }
                    return Behaviors.same();
                })
                .build();
    }
}
