package akka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


public class FirstSimpleBehavior extends AbstractBehavior<String> {

    private FirstSimpleBehavior(ActorContext<String> context) {
        super(context);
    }

    public static Behavior<String> create() {
        return Behaviors.setup(context -> {
            return new FirstSimpleBehavior(context);
        });
    }

    public Receive<String> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals("say hello", () -> {
                    System.out.println("Hello there!");
                    return this;
                })
                .onMessageEquals("who are you", () -> {
                    System.out.println("My path is " + getContext().getSelf().path());
                    return this;
                })
                .onMessageEquals("create a child", () -> {
                    ActorRef<String> secondActor = getContext().spawn(FirstSimpleBehavior.create(), "SecondActor");
                    secondActor.tell("who are you");
                    return this;
                })
                .onAnyMessage(message -> {
                    System.out.println("I received the message: " + message);
                    return this;
                })
                .build();
    }
}
