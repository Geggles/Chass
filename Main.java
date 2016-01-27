/**
 * Starts the Game
 */
public class Main {
    /*
    * If the first parameter is 'gui', then the game will start with a GUI.
    * */
    public static void main(String[] args){
        Controller controller;
        if (args.length != 0 && args[0].equals("gui")) {
            controller = new GuiController();
        }else{
            controller = new CommandLineController();
        }
        controller.startGame();
    }
}
