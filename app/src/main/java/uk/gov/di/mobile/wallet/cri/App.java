package uk.gov.di.mobile.wallet.cri;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;

public class App extends Application<AppConfiguration>{
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new App().getGreeting());
        new App().run(args);
    }

    public void run(AppConfiguration configuration, Environment environment) {
    }
}
