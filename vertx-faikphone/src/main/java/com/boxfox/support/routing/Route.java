package com.boxfox.support.routing;

import io.vertx.core.http.HttpMethod;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
public @interface Route {
	String uri();
	HttpMethod method();
}
