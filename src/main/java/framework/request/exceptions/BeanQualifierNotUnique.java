package framework.request.exceptions;

public class BeanQualifierNotUnique extends Exception{

    public BeanQualifierNotUnique(){
        super("[!] Bean qualifier is not unique!");
    }
}
