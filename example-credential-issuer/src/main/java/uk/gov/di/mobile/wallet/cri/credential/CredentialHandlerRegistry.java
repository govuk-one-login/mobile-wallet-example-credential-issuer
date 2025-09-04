package uk.gov.di.mobile.wallet.cri.credential;

import java.util.List;

public class CredentialHandlerRegistry {
  private final List<CredentialHandler> handlers;

  public CredentialHandlerRegistry(List<CredentialHandler> handlers) {
    this.handlers = handlers;
  }

  public CredentialHandler getHandler(String vcType) {
    return handlers.stream()
            .filter(h -> h.supports(vcType))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                    "Unsupported credential type: " + vcType));
  }
}