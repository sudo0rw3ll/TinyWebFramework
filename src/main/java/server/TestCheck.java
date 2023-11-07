package server;

import framework.annotations.DI.Bean;
import framework.annotations.DI.Qualifier;
import framework.annotations.DI.Service;
import framework.annotations.DI.scopes.BeanScope;

@Bean
@Qualifier(value = "testIntfImpl")
public class TestCheck implements TestIntf {

    @Override
    public void hello() {
        System.out.println("HENLO");
    }
}
