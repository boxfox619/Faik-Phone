package com.boxfox.support.routing;

import com.boxfox.support.utilities.Log;
import io.vertx.core.Handler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Routing {
	private static List<RESTResource> resourceList = new ArrayList<RESTResource>();
	
	@SuppressWarnings("unchecked")
	public static void route(Router router, String... packages) {
		for(String p: packages) {
			Reflections reflections = new Reflections(p);

			Set<Class<?>> routeAnnotatedClasses = reflections.getTypesAnnotatedWith(Route.class);

			for(Class<?> c : routeAnnotatedClasses) {
				Route routeAnno = c.getAnnotation(Route.class);
				try {
					router.route(routeAnno.method(), routeAnno.uri()).handler((Handler<RoutingContext>) c.newInstance());
					Log.routing(routeAnno.method() + " " + routeAnno.uri());
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
		Collections.sort(resourceList);
	}
}
