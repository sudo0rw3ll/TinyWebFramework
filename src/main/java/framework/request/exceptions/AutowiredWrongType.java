package framework.request.exceptions;

public class AutowiredWrongType extends Exception {

    public AutowiredWrongType(){
        super("@Autowired is used on a field which is not @Bean (@Service or @Component)");
    }
}
