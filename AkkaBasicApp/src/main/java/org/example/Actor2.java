package org.example;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Actor2 extends AbstractBehavior<Actor2.Respond> {

    public static class Respond {
        public final String message;
        public Respond(String message) {
            this.message = message;
        }
    }

    public static Behavior<Respond> create() {
        return Behaviors.setup(Actor2::new);
    }

    private Actor2(ActorContext<Respond> context) {
        super(context);
    }

    @Override
    public Receive<Respond> createReceive() {
        return newReceiveBuilder()
                .onMessage(Respond.class, this::onRespond)
                .build();
    }

    private Behavior<Respond> onRespond(Respond message) {
        System.out.println("Actor2 received message: " + message.message);
        return this;
    }
}
