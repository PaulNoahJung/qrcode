public class Control
{

    static Generator gen;

    String err = "";

    public Control(String text, int typ, String name){

        if(text.length() > 17){
            System.out.println("Du hast die maximale Textlänge überschritten. Die maximale Texlänge liegt bei 17");
            return;
        }else if(text.isEmpty()){
            System.out.println("Bitte gib einen Text ein, es können nicht keine Zeichen codiert werden.");
            return;
        }

        if(name.isEmpty()){
            System.out.println("Du musst einen Namen für deine Datei angeben.");
            return;
        }

        else{
            gen = new Generator(text, typ, name);
        }

        //gen = new Generator(text, typ, "");
    }

    public static void main(String[] args) {

    }
}

// {205, 158, 144, 26, 245, 155, 41, 175, 34, 182, 69, 115, 240}