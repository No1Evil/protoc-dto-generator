package io.github.no1evil.protogen.base.service;

public interface StreamSession<T> {
  /** Send a message to a tunnel */
  void send(T message);

  /** Close the tunnel from the client side */
  void complete();

  /** Report the error and close the connection */
  void error(Throwable t);
}
