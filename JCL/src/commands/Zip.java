package commands;//package functions;


import interfaces.Commandable;

public class Zip implements Commandable {

    private final String commandName = "z";


    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void run(String[] args) {
        System.out.println(args);
    }

    public boolean demoMethod(String [] args){
        System.out.println("Parameter passed: ");
        for (String a: args){
            System.out.println(a);
        }

        return Zip.class.isInstance(Commandable.class);
    }
}
