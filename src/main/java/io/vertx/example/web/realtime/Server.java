package io.vertx.example.web.realtime;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.example.util.Runner;
import io.vertx.example.web.realtime.util.CustomMessage;
import io.vertx.example.web.realtime.util.CustomMessageCodec;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class Server extends AbstractVerticle {

	// Convenience method so you can run it in your IDE
	public static void main(String[] args) {
		Runner.runExample(Server.class);
	}

	@Override
	public void start() throws Exception {
		EventBus eventBus = getVertx().eventBus();

		// Register codec for custom message
		eventBus.registerDefaultCodec(CustomMessage.class, new CustomMessageCodec());

		// Custom message
		CustomMessage customMessage = new CustomMessage(200, "a00000001", "Message sent from publisher!");

		Router router = Router.router(vertx);

		// Allow outbound traffic to the news-feed address

		BridgeOptions options = new BridgeOptions().addOutboundPermitted(new PermittedOptions().setAddress("news-feed"));

		router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options));

		// Serve the static resources
		router.route().handler(StaticHandler.create().setWebRoot("webroot"));

		vertx.createHttpServer().requestHandler(router::accept).listen(8080);

		// Publish a message to the address "news-feed" every second
		vertx.setPeriodic(1000, t -> vertx.eventBus().publish("news-feed", customMessage));
	}
}
