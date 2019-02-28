package com.example.demo;

import java.util.Collections;

import org.axonframework.axonserver.connector.command.AxonServerCommandBus;
import org.axonframework.axonserver.connector.query.AxonServerQueryBus;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.queryhandling.QueryBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
public class AxonMessageInterceptersConfig {

  @Autowired
  public void registerInterceptors(CommandBus commandBus, QueryBus queryBus) {
    Assert.notNull(commandBus, "Invalid configuration, commandBus is null!");
    Assert.notNull(queryBus, "Invalid configuration, queryBus is null!");

    if (AxonServerCommandBus.class.isAssignableFrom(commandBus.getClass())) {
      AxonServerCommandBus.class.cast(commandBus).registerDispatchInterceptor(authorizationDispatchInterceptor());
      AxonServerCommandBus.class.cast(commandBus).registerHandlerInterceptor(authorizationHandlerInterceptor());
    }
    if (AxonServerQueryBus.class.isAssignableFrom(queryBus.getClass())) {
      AxonServerQueryBus.class.cast(queryBus).registerDispatchInterceptor(authorizationDispatchInterceptor());
      AxonServerQueryBus.class.cast(queryBus).registerHandlerInterceptor(authorizationHandlerInterceptor());
    }
  }

  private MessageDispatchInterceptor<? super Message<?>> authorizationDispatchInterceptor() {
    return list -> {
      // NOTE: this is where we propagate the authentication information from the dispatch thread
      // to the handler thread...If this intercepter is not invoked, the handler will throw a SecurityException
      return (index, message) -> message.andMetaData(Collections.singletonMap("USER_INFO",
          "I am a place holder for a user informatin object that will come from the security context."));
    };
  }

  private MessageHandlerInterceptor<? super Message<?>> authorizationHandlerInterceptor() {
    return (unitOfWork, interceptorChain) -> {
      Object userInfo = unitOfWork.getMessage().getMetaData().get("USER_INFO");
      if (userInfo == null) {
        // NOTE: This is where the subscription query bug is exposed. If the dispatch intercepter had been invoked
        // then userInfo would not be null...
        // This will obviously prevent the query handler from being invoked. However, the problem is that this
        // exception is swallowed and the initial result is simply no result because the query is never invoked.
        // This makes this issue very hard to debug.
        
        // TODO: ensure again that this is not being delivered via the onError of the initial result mono...
        System.out.println("I am about to throw a SecurityException because the user information is not present.");
        throw new SecurityException("User information not available!");
      }
      return interceptorChain.proceed();
    };
  }
}
