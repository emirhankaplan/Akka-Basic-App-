package org.example;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class Main extends AllDirectives {

    private final ActorSystem<Actor1.Greet> actor1System;
    private final ActorSystem<Actor2.Respond> actor2System;

    public Main(ActorSystem<Actor1.Greet> actor1System, ActorSystem<Actor2.Respond> actor2System) {
        this.actor1System = actor1System;
        this.actor2System = actor2System;
    }

    public static void main(String[] args) {
        ActorSystem<Actor1.Greet> actor1System = ActorSystem.create(Actor1.create(), "actor1System");
        ActorSystem<Actor2.Respond> actor2System = ActorSystem.create(Actor2.create(), "actor2System");

        Main main = new Main(actor1System, actor2System);

        Route route = main.createRoute();

        Http.get(actor1System).newServerAt("localhost", 8080).bind(route);
        Http.get(actor2System).newServerAt("localhost", 8081).bind(route);

        actor1System.tell(new Actor1.Greet("Hi from Actor1"));
        actor2System.tell(new Actor2.Respond("Hi from Actor2"));
    }

    private Route createRoute() {
        Duration timeout = Duration.ofSeconds(5);
        return concat(
                path("actor1", () ->
                        completeWithFuture(
                                AskPattern.ask(
                                        actor2System.systemActorOf(Actor2.create(), "actor2Actor", Props.empty()),
                                        replyTo -> new Actor2.Respond("Hi from Actor1"),
                                        timeout,
                                        actor2System.scheduler()
                                ).thenApply(response -> {
                                    Actor2.Respond respond = (Actor2.Respond) response;
                                    return HttpResponse.create().withEntity(respond.message);
                                })
                        )
                ),
                path("actor2", () ->
                        completeWithFuture(
                                AskPattern.ask(
                                        actor1System.systemActorOf(Actor1.create(), "actor1Actor", Props.empty()),
                                        replyTo -> new Actor1.Greet("Hi from Actor2"),
                                        timeout,
                                        actor1System.scheduler()
                                ).thenApply(response -> {
                                    Actor1.Greet greet = (Actor1.Greet) response;
                                    return HttpResponse.create().withEntity(greet.message);
                                })
                        )
                )
        );
    }
}
